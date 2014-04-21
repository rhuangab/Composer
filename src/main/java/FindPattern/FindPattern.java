package FindPattern;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class FindPattern {

	private static String inputPath = "/Users/jenny/Desktop/output/"; 
	private static String singlePatternPath = "/Users/jenny/Desktop/singlePatternRecord/";
	private static String newSinglePatternPath = "/Users/jenny/Desktop/newSinglePatternRecord/";
	private static File[] listOfFiles; 
	private static String filename;  //the file that currently dealing with
	public  static HashMap<Integer, HashSet<SequencePair> > allPatternOne; //[key patternSize : value pattern]
	public  static HashMap<Integer, HashSet<SequencePair> > allPatternTwo;
	private static HashMap<SequencePair, Integer> AllcountOne;//all the patterns for one file
	private static HashMap<SequencePair, Integer> AllcountTwo;//all the patterns for one file
	private static HashMap<SequencePair, Integer> countOne;//all the patterns for one file
	private static HashMap<SequencePair, Integer> countTwo;//all the patterns for one file
	private static HashMap<SequencePair,Integer> hashOne; //current generated patterns for a file of size n pattern;
    private static HashMap<SequencePair,Integer> hashTwo; //current generated patterns for a file of size n pattern;
    
	private static ArrayList<Integer> lrcSeq;//lrc sequence for current file
	private static ArrayList<Integer> meloSeq;//melo sequence for current file 
	private static ArrayList<Integer> durSeq;//duration sequence for current file
	
	
	static int MIN = 2;
	static int MAX = 10;
	static int threshold = 1;
	static int allThreshold = 10;
	
	public static void main(String[] args) throws IOException {
		//System.out.println("constructor of FindPattern");
		allPatternOne = new HashMap<Integer, HashSet<SequencePair> >();
		allPatternTwo = new HashMap<Integer, HashSet<SequencePair> >();
		
		
		AllcountOne = new HashMap<SequencePair, Integer>();
		AllcountTwo = new HashMap<SequencePair, Integer>();
		
		File folder = new File(inputPath);
		listOfFiles = folder.listFiles();
		
		
		//initialize allPattern
		for(int i = MIN; i <=MAX; ++i) {
			allPatternOne.put(new Integer(i), new HashSet<SequencePair>());
			allPatternTwo.put(new Integer(i), new HashSet<SequencePair>());
		}
		
		
		
		for(int i = 1 ; i < listOfFiles.length;++i) {
			filename = listOfFiles[i].getName();
			System.out.println(filename);
			if(filename.equals(".DS_Store")) continue;
			
			//BufferedWriter out = new BufferedWriter(new FileWriter(singlePatternPath+ filename+ ".txt"));


			countOne = new HashMap<SequencePair, Integer>();
			countTwo = new HashMap<SequencePair, Integer>();
			
			for(int j =MIN; j <= MAX; ++j) {
				
				
				//generate current hashOne and hashTwo
				generateSequence();
				getPatterns(j);//generate hashOne and hashTwo for this size

				

				Iterator iter = hashOne.entrySet().iterator();
				while(iter.hasNext()) {
					Map.Entry entry = (Entry) iter.next();
					SequencePair key = (SequencePair) entry.getKey();
					Integer value = (Integer) entry.getValue();
					
					if(countOne.containsKey(key)) {
						countOne.put(key, countOne.get(key) + value);
					}
					else
						countOne.put(key, value);
				}
				
				iter = hashTwo.entrySet().iterator();
				while(iter.hasNext()) {
					Map.Entry entry = (Entry) iter.next();
					SequencePair key = (SequencePair) entry.getKey();
					Integer value = (Integer) entry.getValue();
					
					if(countTwo.containsKey(key)) {
						countTwo.put(key, countTwo.get(key) + value);
					}
					else countTwo.put(key, value);
				}
				//choose patterns which appears at least three times
				HashMap<SequencePair, Integer>  countOnetemp = new HashMap<SequencePair, Integer>();
				iter = countOne.entrySet().iterator();
				while(iter.hasNext()) {
					Map.Entry entry = (Entry) iter.next();
					SequencePair key = (SequencePair) entry.getKey();
					Integer value = (Integer) entry.getValue();
					if(value > threshold) {
						countOnetemp.put(key, value);
						if(AllcountOne.containsKey(key)) {
							AllcountOne.put(key, AllcountOne.get(key)+1);
						}
						else AllcountOne.put(key,1);
					}
						
				}
				countOne = countOnetemp;
				
				HashMap<SequencePair, Integer>  countTwotemp = new HashMap<SequencePair, Integer>();
				iter = countTwo.entrySet().iterator();
				while(iter.hasNext()) {
					Map.Entry entry = (Entry) iter.next();
					SequencePair key = (SequencePair) entry.getKey();
					Integer value = (Integer) entry.getValue();
					if(value > threshold) {
						countOnetemp.put(key, value);
						if(AllcountTwo.containsKey(key)) {
							AllcountTwo.put(key, AllcountTwo.get(key)+1);
						}
						else AllcountTwo.put(key,1);
					}
						
				}
				countTwo = countOnetemp;
			}
			
			//log for this file
			generateForSingleFile();
			
		}
		
		Iterator iter = AllcountOne.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			Integer value = (Integer) entry.getValue();
			SequencePair key = (SequencePair) entry.getKey();//System.out.println("current" + "[" + key.firstSeq +":"+key.secondSeq +"]" + " ;  "+ value);
			
			if(value > allThreshold) {
				allPatternOne.get(key.firstSeq.size()).add(key);
			}
		
		}
		
		iter = AllcountTwo.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			Integer value = (Integer) entry.getValue();
			SequencePair key = (SequencePair) entry.getKey();//System.out.println("current" + "[" + key.firstSeq +":"+key.secondSeq +"]" + " ;  "+ value);
			
			if(value > allThreshold) {
				allPatternTwo.get(key.firstSeq.size()).add(key);
			}
		
		}
		LogOverall();
	}

	private static void generateSequence() throws FileNotFoundException {
		// TODO Auto-generated method stub
        Scanner s = new Scanner(new BufferedReader(new FileReader(inputPath + filename)));
		lrcSeq = new ArrayList<Integer>();
		meloSeq = new ArrayList<Integer>();
		durSeq = new ArrayList<Integer>();
		
		String line = null;
		int wordStress;
		int pitch;
		int melStress;
		int stress;
		int duration;
		
		while(s.hasNextLine()) {
			line = s.nextLine();
			String[] temp = line.split(",");
			
			wordStress = Integer.parseInt(temp[1]);
			pitch = Integer.parseInt(temp[2]);
			melStress = Integer.parseInt(temp[3]);
			duration = Integer.parseInt(temp[4]);
			

			//combine word level stress and sentence level stress
			stress = wordStress * 3 + melStress;
			/*if(stress < 0 || stress > 9) {
				System.out.println("Stress range error");
			}*/
			lrcSeq.add(stress);
			meloSeq.add(pitch);
			durSeq.add(duration);
		}
		
		//calculate relative value
		for(int i = 0;i < lrcSeq.size() -1;++i) {
			lrcSeq.set(i, lrcSeq.get(i+1)-lrcSeq.get(i));
			meloSeq.set(i, meloSeq.get(i+1) - meloSeq.get(i));
			if(durSeq.get(i+1) / durSeq.get(i)>=1)
				durSeq.set(i, durSeq.get(i+1) / durSeq.get(i));
			else 
				durSeq.set(i,durSeq.get(i) / durSeq.get(i+1) * (-1));
		}
		lrcSeq.remove(lrcSeq.size()-1);
		meloSeq.remove(meloSeq.size()-1);
		durSeq.remove(durSeq.size()-1);
		
	}

	private static void LogOverall() throws IOException {
		
	
		// TODO Auto-generated method stub
		BufferedWriter out;
		Iterator entries = allPatternOne.entrySet().iterator();
		
		 out = new BufferedWriter(new FileWriter(singlePatternPath+"allPatternOne.txt"));
		    int count1 = 0;
		    entries = allPatternOne.entrySet().iterator();
			while (entries.hasNext()) {
			    Map.Entry entry = (Map.Entry) entries.next();
			    Integer value = (Integer)entry.getKey();
			    HashSet<SequencePair> set = (HashSet<SequencePair>) entry.getValue();
			    count1+=set.size();
			    out.write("!size " + value + " " + set.size() + "\n");
			    Iterator itr = set.iterator();
			    while(itr.hasNext()) {
			    	SequencePair current = (SequencePair) itr.next();
			    	//System.out.println("[" + current.firstSeq +":"+current.secondSeq +"]");
			        out.write("[" + current.firstSeq +":"+current.secondSeq +"]"+"\n");
			    }
			}
			out.write("! total " + count1);
			//System.out.println(count2);
			if(out!=null)
				out.close();
		
	
		
		
	    out = new BufferedWriter(new FileWriter(newSinglePatternPath+"allPatternTwo.txt"));
	    int count2 = 0;
	    entries = allPatternTwo.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry entry = (Map.Entry) entries.next();
		    Integer value = (Integer)entry.getKey();
		    HashSet<SequencePair> set = (HashSet<SequencePair>) entry.getValue();
		    count2+=set.size();
		    out.write("!size " + value + " " + set.size() + "\n");
		    Iterator itr = set.iterator();
		    while(itr.hasNext()) {
		    	SequencePair current = (SequencePair) itr.next();
		    	
		        out.write("[" + current.firstSeq +":"+current.secondSeq +"]"+"\n");
		    }
		}
		out.write("! total " + count2);
		//System.out.println(count2);
		if(out!=null)
			out.close();
		
		
	}

	private static void generateForSingleFile() throws IOException {
		// TODO Auto-generated method stub
		BufferedWriter out = new BufferedWriter(new FileWriter(singlePatternPath+ filename));
		
		Iterator iter = countOne.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			Integer value = (Integer) entry.getValue();
			SequencePair key = (SequencePair) entry.getKey();//System.out.println("current" + "[" + key.firstSeq +":"+key.secondSeq +"]" + " ;  "+ value);
			out.write("[" + key.firstSeq + ":" + key.secondSeq + "]" +":" +value + "\n");
		}
		if(out!=null)
			out.close();
		
		
		BufferedWriter out1 = new BufferedWriter(new FileWriter(newSinglePatternPath+filename));
		iter = countTwo.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			Integer value = (Integer) entry.getValue();
			SequencePair key = (SequencePair) entry.getKey();//System.out.println("current" + "[" + key.firstSeq +":"+key.secondSeq +"]" + " ;  "+ value);
			out1.write("[" + key.firstSeq + ":" + key.secondSeq + "]" +":" +value + "\n");
		}
		 
		if(out1!=null)
			out1.close();
	}
	
	//given a file, generate all sequence of  size 
	private static void getPatterns(int size) {
		// TODO Auto-generated method stub
		hashOne = new HashMap<SequencePair,Integer>();
		hashTwo = new HashMap<SequencePair,Integer>();
		ArrayList<Integer> first;
		ArrayList<Integer> second;
		ArrayList<Integer> third;
		
		//System.out.println(lrcSeq.size());
		
		for(int i = 0; i < lrcSeq.size() - size ;++i) {
			first = new ArrayList<Integer>();
			second = new ArrayList<Integer>();
			third = new ArrayList<Integer>();
			
			for(int j = i; j < i+ size ;++j) {
				first.add(lrcSeq.get(j));
				second.add(meloSeq.get(j));
				third.add(durSeq.get(j));
			}
			//System.out.println(first);
			//System.out.println(second);
			//System.out.println(third);
			SequencePair cur= new SequencePair(first, second);
		
			int count = hashOne.containsKey(cur)?hashOne.get(cur):0;
			hashOne.put(cur, count+1);
			
			cur = new SequencePair(second, third);
			count = hashTwo.containsKey(cur)?hashTwo.get(cur):0;
			hashTwo.put(cur, count+1);
		
		}
	
	}
	
}
