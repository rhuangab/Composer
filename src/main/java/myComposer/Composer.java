package myComposer;

import java.io.File;
import filePath.FilePath;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import FindPattern.FindPattern;
import FindPattern.SequencePair;
import Phonetic.WordStruct;
import Relative2absolute.Rel2abs.EndType;

public class Composer {
	
	public  static HashMap<Integer, HashSet<SequencePair> > allPatternOne; //[key patternSize : value pattern]
	public  static HashMap<Integer, HashSet<SequencePair> > allPatternTwo;
	//public static HashMap<ArrayList< > >
	static int resolution = 480;


	private static String str;
	private static HTree hashtable;
	public String jasonResult;
	private static HashMap<String, Integer> beatsMap;
	private ArrayList<Integer> lrc;
	private ArrayList<Integer> lrc2;
	private ArrayList<String> lrcText;
	private ArrayList<Integer> melo;
	private ArrayList<Integer> dur;
	private ArrayList<Integer> beatArray;
	static String beatFirst;
	static String beatSecond;
	static int MIN = 2;
	static int MAX = 10;
	
	public static void out(int o)
	{
		System.out.println(o);
	}

	public static void main(String[] args) throws IOException, InvalidMidiDataException {
		RecordManager recman = RecordManagerFactory.createRecordManager(FilePath.dictionary);
		long recid = recman.getNamedObject("Pattern");
		if(recid!=0) {
          	hashtable  = HTree.load(recman, recid);
          	//System.out.println("can open dictionary");
          }
          else {
          	hashtable = HTree.createInstance(recman);
          	recman.setNamedObject("Pattern", hashtable.getRecid());
          }
		Scanner ins = new Scanner(System.in);
		while(true)
		{
			System.out.print("Input an array: ");
			String inputString = ins.nextLine();
			if(inputString == "richeng") break;
			ArrayList<ArrayList<Integer> > meloArr = (ArrayList<ArrayList<Integer>>) hashtable.get(inputString);
			if(meloArr != null)
				System.out.println(meloArr.size() + " "+meloArr.toString());
		}
		//ArrayList<ArrayList<Integer> > meloArr = hashtable.get(key) 
		
		recman.close();
		if(true) return;
		String input  = "In this Map example, we will learn how to check if HashMap is empty in Java. There are two ways to find out if Map is empty, one is using size() method, if size is zero means Map is empty. Another way to check if HashMap is empty is using more readable isEmpty() method which returns true if Map is empty. Here is code example:";
		//Composer com = new Composer(input,4,4);
		if(allPatternOne == null){
			allPatternOne = new HashMap<Integer, HashSet<SequencePair> >();
			for(int i = MIN; i <=MAX; ++i) {
				allPatternOne.put(new Integer(i), new HashSet<SequencePair>());
			}
			readInPatterns(FilePath.allPatternOne,true);
		}
		out(allPatternOne.get(3).size());
		recman.commit();
		recman.close();
	}


	public Composer(String input, int first, int second) throws IOException {
		
		//ini
		melo = new ArrayList<Integer>();
		dur = new ArrayList<Integer>();
		
		this.str = input;
		beatFirst = Integer.toString(first);
		beatSecond = Integer.toString(second);
		if(beatsMap ==null) beatMap();
		beatArray = new ArrayList<Integer>();
		
		for(int i = 1; i <= Integer.parseInt(beatSecond); ++i) {
			if(beatsMap.get(beatFirst+"+"+beatSecond+"+"+i)==null) 
				System.out.println("does not exist this type");
			else 
				beatArray.add(beatsMap.get(beatFirst+"+"+beatSecond+"+"+i));
			
		}
		
		if(allPatternOne == null){
			allPatternOne = new HashMap<Integer, HashSet<SequencePair> >();
			for(int i = MIN; i <=MAX; ++i) {
				allPatternOne.put(new Integer(i), new HashSet<SequencePair>());
			}
			readInPatterns(FilePath.allPatternOne,true);
		}
		if(allPatternTwo == null){
			allPatternTwo = new HashMap<Integer, HashSet<SequencePair> >();
			for(int i = MIN; i <=MAX; ++i) {
				allPatternTwo.put(new Integer(i), new HashSet<SequencePair>());
			}
			readInPatterns(FilePath.allPatternTwo,false);
		}
		
		lrc = parseLrc();
		//System.out.println(lrc);
		//generate Relative Array
		for(int i = 0; i < lrc.size() -1 ;++i) {
			if(lrc.get(i+1) == 99) 
				lrc.set(i+1, lrc.get(i));
			lrc.set(i, lrc.get(i+1)-lrc.get(i));
			//System.out.println("lrc:"+lrc.get(i));
		}
		lrc.remove(lrc.size()-1);
		//add beat information
		/*int position = 0;
		for(int i = 0; i < lrc.size(); ++i) {
			lrc.set(i, lrc.get(i) + beatArray.get(position));
			//System.out.println("add " + beatArray.get(position));
			position = (position+1) % beatArray.size();
		}*/
		
		
		//System.out.println(lrc);
		
		//generate melo array
		int findDurLen = 0;
		int notfindDurLen = 0;
		int successLen = 0;
		int startPos = 0;
		while(startPos < lrc.size() - MIN) {
			boolean find = false;
			
			for(int i = MAX; i>MIN;i--) {
				ArrayList<Integer> searchResult = new ArrayList<Integer>();
				
				ArrayList<Integer> current = new ArrayList<Integer>();
				
				for(int j = startPos; j < startPos + i && j < lrc.size(); ++j) {
					current.add(lrc.get(j));	
				}
				HashSet<SequencePair> currentSet = allPatternOne.get(i);//get the set
				Iterator iter = currentSet.iterator();
				while(iter.hasNext()) {
					SequencePair key = (SequencePair) iter.next();
					if(ArrayContentCompare(current, key.firstSeq)) {
						System.out.println("!!find for " + startPos + " to "+(startPos+i) +":"+current+" VS "+key.firstSeq+" : "+key.secondSeq);
						find = true;
						searchResult = key.secondSeq;
						successLen+=i;
						startPos+=i;
						melo.addAll(searchResult);
						break;
					}
					
				}
				
				if(!find) {
					//System.out.println("can not find for size "+ i);
				}
			}
			//cannot find for at least length two
			if(!find) {
				System.out.println("cannnot find for any size for " + startPos);
				startPos++;
				int a[] = {1,0,-1};
				melo.add(a[startPos%3]);
			}
		}
		
		//deal with the end 
		while(startPos< lrc.size() && startPos!= lrc.size()) {
			melo.add(new Integer(0));
			startPos++;
		}
		/*
		System.out.println("lrc size" + lrc.size());
		System.out.println("melo size" + melo.size());
		System.out.println("lrc array is " + lrc);
		System.out.println("melo array is " + melo);
		
		System.out.println("success length" + successLen);*/
		
		
		//generate duration according to melo
		successLen = 0;
		startPos = 0;
		int a[] = {1,-2,2};
		int index = 0;
		while(startPos < melo.size() - MIN) {
			boolean find = false;
			index++;
			for(int i = MAX; i>MIN;i--) {
				ArrayList<Integer> searchResult = new ArrayList<Integer>();
				
				ArrayList<Integer> current = new ArrayList<Integer>();
				
				for(int j = startPos; j < startPos + i && j < melo.size(); ++j) {
					current.add(melo.get(j));	
				}
				HashSet<SequencePair> currentSet = allPatternTwo.get(i);//get the set
				Iterator iter = currentSet.iterator();
				while(iter.hasNext()) {
					SequencePair key = (SequencePair) iter.next();
					if(ArrayContentCompare(current, key.firstSeq)) {
						//System.out.println("**Durfind for " + startPos + " to "+(startPos+i));
						find = true;
						findDurLen += i;
						searchResult = key.secondSeq;
						successLen+=i;
						startPos+=i;
						dur.addAll(searchResult);
						break;
					}
					
				}
				
				if(!find) {
					//System.out.println("can not find for size "+ i);
				}
					
			}
			//cannot find for at least length two
			if(!find) {
				//System.out.println("DDDDur cannot find for "+ startPos);
				startPos++;
				notfindDurLen++;
				dur.add(a[index%3]);
			}
			
		}
		//System.out.println("Find:"+findDurLen+",notFind:"+notfindDurLen);
		//deal with the end 
		while(startPos< lrc.size() && startPos!= lrc.size()) {
			dur.add(new Integer(1));
			startPos++;
		}
		
		/*
		System.out.println("melo size" + melo.size());
		System.out.println("dur size" + dur.size());
		System.out.println("melo array is " + melo);
		System.out.println("dur array is " + dur);
		System.out.println("success length" + successLen); */
		//getJason();
		
		
	}
	
	
	private boolean ArrayContentCompare(ArrayList<Integer> first,
			ArrayList<Integer> second) {
		// TODO Auto-generated method stub
		if(first==null || second == null) return false;
		if(first.size() != second.size())  return false;
		for(int i = 0; i < first.size();++i) {
			if(first.get(i)!=second.get(i)) {
				return false;
			}
		}
		return true;
	}


	private boolean checkArray(ArrayList<Integer> first, ArrayList<Integer> second) {
		// TODO Auto-generated method stub
		if(first.size() != second.size())		return false;
		for(int i = 0; i < first.size();++i) {
			if(first.get(i)!=second.get(i)) {
				return false;
			}
		}
		return true;
	}


	public void pushLrcText(String current, int part)
	{
		if(part == 1) lrcText.add(current);
		else if(part >=2 && current.length() >=part)
		{
			int length = current.length();
			int subLen = length / part;
			for(int i=0;i<part-1;i++){
				lrcText.add(current.substring(subLen*i,subLen*(i+1)));
			}
			lrcText.add(current.substring(subLen*(part-1)));
		}
		else{
			for(int i=0;i<part;i++){
				lrcText.add(current);
			}
		}
	}
	
	private ArrayList<Integer> parseLrc() throws IOException {
		ArrayList<Integer> result = new ArrayList<Integer>();
		lrcText = new ArrayList<String>();
		str = str.replace("[^a-zA-Z]", "");
		str = str.replace("?", "");
		str = str.replace("!", "");
		str = str.replace(".", "");
		str = str.replace(",", "");
		str = str.replace(":", "");
		str = str.replace("()","");
		str = str.trim().replaceAll(" +", " ");
		
		//System.out.println(str);
		RecordManager recman = RecordManagerFactory.createRecordManager(FilePath.dictionary);
		long recid = recman.getNamedObject("Dictionary");
		if(recid!=0) {
          	hashtable  = HTree.load(recman, recid);
          	//System.out.println("can open dictionary");
          }
          else {
          	hashtable = HTree.createInstance(recman);
          	recman.setNamedObject("Dictionary", hashtable.getRecid());
          }
		
		String[] strArray = str.split(" ");   //to be modified here
		
		
		for(String current: strArray) {
			WordStruct ws = (WordStruct) hashtable.get(current.toUpperCase());
			if(ws==null) {
				//System.out.println("no: " + current);
				if(current.length() <=5){
					result.add(new Integer(99));
					pushLrcText(current, 1);
				}
				else if(current.length() > 5 && current.length() < 9)
				{
					result.add(0);
					result.add(3);
					pushLrcText(current, 2);
				}
				else
				{
					result.add(0);
					result.add(6);
					result.add(3);
					pushLrcText(current, 3);
				}
			}
			else {
				String stress = ws.stress;
				for(int i = 0;i < stress.length();++i) {
					result.add(Integer.parseInt(stress.substring(i, i+1))*3);
				}
				pushLrcText(current, stress.length());
			}
		}
		int position = 0;
		for(int i = 0; i < result.size(); ++i) {
			result.set(i, result.get(i) + beatArray.get(position));
			//System.out.println("add " + beatArray.get(position));
			position = (position+1) % beatArray.size();
		}
		return result;
		
	}
	
	public String getLrcText(int index)
	{
		return lrcText.get(index);
	}
//	static void getJason() {
//		  
//		
//		jasonResult = "[";
//		//int first = startOne;
//		//int two = startTwo;
//		  
//	       
//	       for(int i = 0; i < lrc.size(); ++i) {
//	    	   /*first+=lrc.get(i);
//	    	   //System.out.println(lrc);
//	    	   if(dur.get(i) > 0) {
//	    		   two = two * dur.get(i);
//	    	   }
//	    	   else 
//	    		   two = two / dur.get(i) * (-1);
//	    	   if(two > 64) {
//	    		   two =64;
//	    	   }
//	    	   if(two< 1)
//	    		   two = 1;*/
//	    	   
//	    	   jasonResult = jasonResult +  "{keys:" + melo.get(i) +"," +"duration:" + dur.get(i) +"},";
//	 
//	       }
//	       jasonResult = jasonResult.substring(0, jasonResult.length()-1);
//	       jasonResult+="]";
//	       //System.out.println(jasonResult);
//	       
//		
//	}
	
	/*
	public static Sequence generateSequence(ArrayList<Integer> melo,ArrayList<Integer> dur) throws InvalidMidiDataException, IOException {
		// TODO Auto-generated method stub
		Sequence sequence = new Sequence(Sequence.PPQ, resolution);
		Track t = sequence.createTrack();
		
		//Turn on General MIDI sound set
		byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
		SysexMessage sm = new SysexMessage();
		sm.setMessage(b, 6);
		MidiEvent me = new MidiEvent(sm,(long)0);
		t.add(me);
		
		//set tempo
		MetaMessage mt = new MetaMessage();
        byte[] bt = {0x02, (byte)0x00, 0x00};
		mt.setMessage(0x51 ,bt, 3);
		me = new MidiEvent(mt,(long)0);
		t.add(me);
		
		//set trackname
		mt = new MetaMessage();
		String TrackName = new String("midifile track");
		mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
		me = new MidiEvent(mt,(long)0);
		t.add(me);
		
		//set omni on
		ShortMessage mm = new ShortMessage();
		mm.setMessage(0xB0, 0x7D,0x00);
		me = new MidiEvent(mm,(long)0);
		t.add(me);
		
		//set ploy on
		mm = new ShortMessage();
		mm.setMessage(0xB0, 0x7F,0x00);
		me = new MidiEvent(mm,(long)0);
		t.add(me);
		
		//set Instrument to Piano
		mm = new ShortMessage();
		mm.setMessage(0xC0, 0x00, 0x00);
		me = new MidiEvent(mm,(long)0);
		t.add(me);
		
		//middle part
		int pitch = 0x3C;  //start with middle C
		int start = 0;
		for(int i = 0; i < melo.size(); ++i) {
			
			//System.out.println("get into here" + i);
			//note on
			mm = new ShortMessage();
			pitch += melo.get(i); 
			mm.setMessage(0x90 ,pitch + melo.get(start), 0x60);
			//System.out.println(pitch);
			//System.out.println(pitch + beatArray.get(start));
			start = (1+ start) % Integer.parseInt(beatSecond);
			me = new MidiEvent(mm,(long) (i) * 1000);
			t.add(me);
			///note off
			mm = new ShortMessage();
			mm.setMessage(0x80 ,pitch + melo.get(start) , 0x40);
			start = (1+ start) % Integer.parseInt(beatSecond);
			me = new MidiEvent(mm,(long) (i+0.5) * 1000);
			t.add(me);
			
			//test using middle C
			/*mm = new ShortMessage();
			mm.setMessage(0x90,0x3C,0x60);
			me = new MidiEvent(mm,(long)1);
			t.add(me);
			
			mm = new ShortMessage();
			mm.setMessage(0x80,0x3C,0x40);
			me = new MidiEvent(mm,(long)121);
			t.add(me);
			
			
			
		}
		
		
		//set end of track
		mt = new MetaMessage();
        byte[] bet = {}; // empty array
		mt.setMessage(0x2F,bet,0);
		me = new MidiEvent(mt, (long)140);
		t.add(me);
		
		//write the MIDI sequence to a MIDI file
		File f = new File(FilePath.generatedMIDI);
		MidiSystem.write(sequence,1,f);
		return sequence;
	}*/
	
	private static void readInPatterns(String filename,boolean LRC) throws IOException {
		// TODO Auto-generated method stub
		Scanner s = new Scanner(new File(filename));
		String line;
		ArrayList<Integer> lrc = new ArrayList<Integer>();
		ArrayList<Integer> melo = new ArrayList<Integer>();
		
		
		int size  = - 1;
		int count = 0;
		int countZero = 0;
		int countZeroMel = 0;
		boolean allZero = true;
		boolean allZeroMel = true;
		while(s.hasNextLine()) {
			line  = s.nextLine();
			
			lrc = new ArrayList<Integer>();
			melo = new ArrayList<Integer>();
			if(line.contains("total")) break;
			if(line.charAt(0)=='!') {
				int pos = line.indexOf(" ");
				int pos1 = line.indexOf(" ", pos + 1);
				size = Integer.parseInt(line.substring(pos+1, pos1));
				//System.out.println("test size: " + size);
				
			}
			else {
				line = line.substring(1,line.length()-1);
				int pos =line.indexOf(":");
				String first = line.substring(1,pos-1);
				String second = line.substring(pos+2,line.length()-1);
				//System.out.println("["+ first + "]=[" + second + "]");
				String[] one = first.split(",");
				String[] two = second.split(",");
				for(int i = 0; i < one.length;++i) {
					one[i] = one[i].trim();
				    two[i] = two[i].trim();
				    if(Integer.parseInt(one[i]) != 0) allZero = false;
				    if(Integer.parseInt(two[i]) != 0) allZeroMel = false;
					//System.out.print("[" + one[i] + "]");
					lrc.add(Integer.parseInt(one[i]));
					melo.add(Integer.parseInt(two[i]));
				}
				if(allZero) countZero++;
				if(allZeroMel) countZeroMel++;
				//System.out.println();
			}
			if(LRC) {
				if(!allZero && !allZeroMel){
					allPatternOne.get(size).add(new SequencePair(lrc,melo));
					/*ArrayList<ArrayList<Integer> > meloArr =  (ArrayList<ArrayList<Integer>>) hashtable.get(lrc.toString());
					if(meloArr == null)
					{
						meloArr = new ArrayList<ArrayList<Integer> >();
						meloArr.add(melo);
						hashtable.put(lrc.toString(), meloArr);
					}
					else{
						meloArr.add(melo);
						hashtable.put(lrc.toString(), meloArr);
					}*/
				}
			}
			else {
				allPatternTwo.get(size).add(new SequencePair(lrc,melo));
			}
			allZero = true;
			allZeroMel = true;
		}
		System.out.println(countZero+"@"+countZeroMel);
		if(s!=null)
			s.close();
	}
	
	public ArrayList<Integer> getLrc() {
		return lrc;
	}
	
	public ArrayList<Integer> getMelo() {
		return melo;
	}
	
	public ArrayList<Integer> getDur() {
		return dur;
	}
	
	public static void beatMap() {
		beatsMap = new HashMap<String,Integer>();
		beatsMap.put("2+2+1", 3);
		beatsMap.put("2+2+2", 3);
		
		beatsMap.put("4+1+1", 3);
		
		beatsMap.put("4+2+1", 3);
		beatsMap.put("4+2+2", 1);
		
		beatsMap.put("4+3+1", 3);
		beatsMap.put("4+3+2", 1);
		beatsMap.put("4+3+3", 1);
		
		beatsMap.put("4+4+1", 3);
		beatsMap.put("4+4+2", 1);
		beatsMap.put("4+4+3", 2);
		beatsMap.put("4+4+4", 1);
		
		beatsMap.put("4+5+1", 3);
		beatsMap.put("4+5+2", 1);
		beatsMap.put("4+5+3", 3);
		beatsMap.put("4+5+4", 1);
		beatsMap.put("4+5+5", 1);
		
		beatsMap.put("4+6+1", 3);
		beatsMap.put("4+6+2", 1);
		beatsMap.put("4+6+3", 1);
		beatsMap.put("4+6+4", 2);
		beatsMap.put("4+6+5", 1);
		beatsMap.put("4+6+6", 1);
		
		beatsMap.put("8+3+1", 3);
		beatsMap.put("8+3+2", 1);
		beatsMap.put("8+3+3", 1);
		
		beatsMap.put("8+4+1", 3);
		beatsMap.put("8+4+2", 1);
		beatsMap.put("8+4+3", 2);
		beatsMap.put("8+4+4", 1);
		
		beatsMap.put("8+6+1", 3);
		beatsMap.put("8+6+2", 1);
		beatsMap.put("8+6+3", 1);
		beatsMap.put("8+6+4", 2);
		beatsMap.put("8+6+5", 1);
		beatsMap.put("8+6+6", 1);
		
		beatsMap.put("8+12+1", 3);
		beatsMap.put("8+12+2", 1);
		beatsMap.put("8+12+3", 1);
		beatsMap.put("8+12+4", 2);
		beatsMap.put("8+12+5", 1);
		beatsMap.put("8+12+6", 1);
		beatsMap.put("8+12+7", 2);
		beatsMap.put("8+12+8", 1);
		beatsMap.put("8+12+9", 1);
		beatsMap.put("8+12+10", 2);
		beatsMap.put("8+12+11", 1);
		beatsMap.put("8+12+12", 1);
	}
	
}
