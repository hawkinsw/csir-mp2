package analyzer;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class Tools {
	private static SnowballStemmer m_stemmer = null;
	private static Tokenizer m_tokenizer = null;

	public static Tokenizer getTokenizer() {
		if (m_tokenizer == null) {
			try {
				m_tokenizer = new TokenizerME(
					new TokenizerModel(
						new FileInputStream("./data/Model/en-token.bin")));
			} catch (FileNotFoundException fnfe) {
				System.out.println("Tokenizer model file not found.");
			} catch (IOException ioe) {
				System.out.println("Tokenizer model threw exception.");
			}
		}
		if (m_tokenizer != null) 
			return m_tokenizer;

		return null;
	}

	public static SnowballStemmer getStemmer() {
		if (m_stemmer == null) {
			m_stemmer = new englishStemmer();
		}
		return m_stemmer;
	}
}
