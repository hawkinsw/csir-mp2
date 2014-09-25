package analyzer;

import structures.Post;
import analyzer.WordFrequency;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import structures.Post;
import analyzer.Tools;

public class PostAnalyzer
{
	public PostAnalyzer(Post p) {
		m_ID = p.getID();
		tokenizePost(p);
	}

	private void tokenizePost(Post p) {
		SnowballStemmer stemmer = Tools.getStemmer();
		String tokens[] = null;
		ArrayList<String> nonBlankNormalizedTokens = new ArrayList<String>();
		Tokenizer tokenizer = null;

		m_tokens = new String[0];

		tokenizer = Tools.getTokenizer();

		if (tokenizer == null) {
			System.out.println("No tokenizer.");
			System.exit(1);
		}

		/*
		 * tokenize.
		 */
		tokens = tokenizer.tokenize(p.getTitle() + " " + p.getContent());

		/*
		 * normalize.
		 */
		for (int i = 0; i<tokens.length; i++) {
			if (tokens[i].equals(".")) {
				nonBlankNormalizedTokens.add(".");
				continue;
			}

			String candidateToken = tokens[i]
				.replaceAll("\\p{Punct}", "")
				.toLowerCase();

			if (candidateToken.equals("")) {
				continue;
			}

			stemmer.setCurrent(candidateToken);
			stemmer.stem();

			nonBlankNormalizedTokens.add(stemmer.getCurrent());
		}
		m_tokens = nonBlankNormalizedTokens.toArray(m_tokens);
	}

	private String joinNGram(String delim, String nGram[]) {
		String result = null;
		for (int i = 0; i<nGram.length; i++) {
			if (result == null)
				result = nGram[i];
			else
				result += delim + nGram[i];
		}
		return result;
	}

	public List<WordFrequency> getNGramFrequency(int n) {
		LinkedList<WordFrequency> frequencies = new LinkedList<WordFrequency>();

		if (m_tokens == null)
			return null;

		/*
		 * Make an n-gram
		 *
		 * Check if it's in frequencies.
		 *  Increase count, if yes.
		 *  Add, if no.
		 */
		int nGramCounter = 0;
		for (int i = 0; i<(m_tokens.length-n); i++) {
			String nGram[] = new String[n];
			String nGramString = null;
			boolean invalidNGram = false;
			for (int j = 0; j<n; j++) {
				nGram[j] = m_tokens[i+j];
				if (m_tokens[i+j].equals(".")) {
					invalidNGram = true;
					break;
				}
			}
			if (invalidNGram) {
				continue;
			}

			if (n>1)
				nGramString = joinNGram("-", nGram);
			else
				nGramString = nGram[0];

			WordFrequency wf = new WordFrequency(nGramString, 1);
			int existingFrequency = frequencies.indexOf(wf);
			if (existingFrequency != -1)
				frequencies.get(existingFrequency).merge(wf);
			else
				frequencies.add(wf);	
		}
		return frequencies;
	}

	public List<WordFrequency> getFrequency() {
		return getNGramFrequency(1);
/*
		LinkedList<WordFrequency> frequencies = new LinkedList<WordFrequency>();
		if (m_tokens == null)
			return null;

		int i = 0;
		String stemmedTokens[] = Arrays.copyOf(m_tokens, 
			m_tokens.length);
		for (i = 0; i<stemmedTokens.length; i++) {
			String searchWord = stemmedTokens[i];
			if (searchWord == null) {
				continue;
			}
			int frequency = 0;
			int j = 0;
			for (j = i; j<stemmedTokens.length; j++) {
				if (searchWord.equals(m_tokens[j])) {
					stemmedTokens[j] = null;
					frequency++;
				}
			}
			frequencies.add(new WordFrequency(searchWord, frequency));
		}
		return frequencies;
		*/
	}

	public boolean equals(Object t) {
		if (t instanceof PostAnalyzer) {
			PostAnalyzer oppositePost = (PostAnalyzer)t;
			return m_ID.equals(oppositePost.getID());
		} else {
			return false;
		}
	}

	public int hashCode() {
		return m_ID.hashCode();
	}

	public String getID() {
		return m_ID;
	}

	private String[] m_tokens;
	private String m_ID;
	private LinkedList<WordFrequency> m_frequency;
}
