package analyzer;

public class WordFrequency {
	public WordFrequency(String word, int frequency) {
		m_frequency = frequency;
		m_word = word;
	}

	public String toString() {
		return m_word + "," + m_frequency;
	}

	public String getWord() {
		return m_word;
	}

	public int getFrequency() {
		return m_frequency;
	}

	public boolean equals(Object t) {
		if (!(t instanceof WordFrequency)) {
			return false;
		}
		WordFrequency opp = (WordFrequency)t;
		return m_word.equals(opp.getWord());
	}

	public int hashCode() {
		return m_word.hashCode();
	}

	public void merge(WordFrequency wf) {
		if (this.equals(wf)) {
			m_frequency += wf.getFrequency();
		}
	}

	private String m_word;
	private int m_frequency;
}
