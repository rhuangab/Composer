package Phonetic;


import java.util.List;

public class WordStruct implements java.io.Serializable {
	public String word;
	public String stress; // 0 is non-stress, 1 is primary stress and 2 is second stress
	public String cv; //this is a string where each letter stands for whether the phonones is vowel or consonant."v" stands for vowel and "c" stands for consonants


	WordStruct(String word, String stress, String cv)
	{
		this.word = word;
		this.stress = stress;
		this.cv = cv;
		
	}
	
	

}
