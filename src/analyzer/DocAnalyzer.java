/**
 * 
 */
package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import structures.Post;
import analyzer.PostAnalyzer;

/**
 * @author hongning
 * Sample codes for demonstrating OpenNLP package usage 
 */
public class DocAnalyzer {
	
	String m_threadURL;
	String m_threadTitle;
	ArrayList<PostAnalyzer> m_posts;
	boolean m_isNormalized;	
	boolean m_debug;	
	SimpleDateFormat m_dateFormatter;
	HashMap<WordFrequency,WordFrequency> m_mergedFrequencies;
	
	public DocAnalyzer() {
		m_debug = false;
		m_dateFormatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss Z");//standard date format for this project
		m_mergedFrequencies = null;
	}
	
	public void AnalyzePostsFromDiscussion(JSONObject json) {		
		try {
			json.getString("title");
			json.getString("URL");
			
			JSONArray jarray = json.getJSONArray("thread");
			for(int i=0; i<jarray.length(); i++) {
				Post p = new Post(jarray.getJSONObject(i));
				if (checkPostFormat(p)) {
					PostAnalyzer aPost = new PostAnalyzer(p);
					m_posts.add(aPost);
				} else {
					System.out.println("Skipping a post because it was malformed.");
				}
			}
		} catch (JSONException e) {
			System.out.println("Skipping a thread because it was malformed.");
		}
	}
	
	private boolean checkPostFormat(Post p) {
		if (p.getID() == null) {
			System.err.println("[Error]Missing postID!");
			return false;
		} else if (p.getAuthor() == null) {
			System.err.format("[Error]Missing author name in %s!\n", p.getID());
			return false;
		} else if (p.getAuthorID() == null) {
			System.err.format("[Error]Missing author ID in %s!\n", p.getID());
			return false;
		} else if (p.getDate() == null) {
			System.err.format("[Error]Missing post date in %s!\n", p.getID());
			return false;
		} else if (p.getContent() == null) {
			System.err.format("[Error]Missing post content in %s!\n", p.getID());
			return false;
		} else {
			try {
				m_dateFormatter.parse(p.getDate());
			} catch (ParseException e) {
				System.err.format("[Error]Wrong date format '%s' in %s\n!", p.getDate(), p.getID());
				return false;
			}
		}
		return true;
	}
	
	//sample code for loading the json file
	private JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;
			
			while((line=reader.readLine())!=null) {
				buffer.append(line);
			}
			reader.close();
			
			return new JSONObject(buffer.toString());
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.format("[Error]Failed to parse json file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	public void LoadThreadsInDirectory(String folder) {
		File dir = new File(folder);
		int count = 0;

		m_posts = new ArrayList<PostAnalyzer>();

		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".json")){
				count++;
				AnalyzePostsFromDiscussion(LoadJson(f.getAbsolutePath()));
			}
			else if (f.isDirectory())
				LoadThreadsInDirectory(f.getAbsolutePath());
		}
		mergeFrequencies();
		System.out.println("Loaded " + count + " json files from " + folder);
	}

	public void printFrequencies() {
		if (m_debug) {
			System.out.println("Individual Post Frequencies: ");
			for (PostAnalyzer aPost : m_posts) {
				System.out.println(aPost.getID());
				for (WordFrequency f : aPost.getFrequency()) {
					System.out.println(f);
				}
			}
		}
	}

	public void mergeFrequencies() {
		for (PostAnalyzer aPost : m_posts) {
			for (WordFrequency wf : aPost.getNGramFrequency(2)) {
				if (m_mergedFrequencies.containsKey(wf)) {
					WordFrequency existing = m_mergedFrequencies.get(wf);
					existing.merge(wf);
					m_mergedFrequencies.remove(wf);
					wf = existing;
				}
				m_mergedFrequencies.put(wf, wf);
			}
		}
	}
	
	public void printMergedFrequencies() {
		System.out.println("Merged frequencies: ");
		for (WordFrequency wf : m_mergedFrequencies.keySet()) {
			System.out.println(wf);
		}
	}

	public void analyze(String directory) {
		m_mergedFrequencies = new HashMap<WordFrequency,WordFrequency>();
		LoadThreadsInDirectory(directory);
		printMergedFrequencies();
	}
	
	public static void main(String[] args) {
		DocAnalyzer analyzer = new DocAnalyzer();

		if (args.length != 1) {
			System.out.println("Requires an argument: Thread directory");
			System.exit(1);
		}
		analyzer.analyze(args[0]);
	}
}
