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
	private ArrayList<Integer> melo;
	private ArrayList<Integer> dur;
	private ArrayList<Integer> beatArray;
	static String beatFirst;
	static String beatSecond;
	static int MIN = 2;
	static int MAX = 10;
	

	public static void main(String[] args) throws IOException, InvalidMidiDataException {
		
		String input  = "In this Map example, we will learn how to check if HashMap is empty in Java. There are two ways to find out if Map is empty, one is using size() method, if size is zero means Map is empty. Another way to check if HashMap is empty is using more readable isEmpty() method which returns true if Map is empty. Here is code example:";
	    //String input = "This is ! ? good.";
		//input = "Close your eyes and roll a dice Under the board there's a compromise If after all we only lived twice Which lies the run road to paradise Don't say a word, here comes the break of the day And wide clouds of sand raised by the wind of the end of May Close your eyes and make a betFace to the glare of the sunset";
		Composer com = new Composer(input,4,4);

		
		//test
	    //Sequence sequence = MidiSystem.getSequence(new File("/Users/jzhaoaf/Desktop/2_hearts.mid"));
		//Sequence sequence = generateSequence(melo,dur);
		
		
	   
	    //play the generate sequence
	    //MidiPlayer myPlayer = new MidiPlayer();
	    //myPlayer.play(sequence, false);
	    //myPlayer.stop();
	
		
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
		}
		lrc.remove(lrc.size()-1);
		
		//add beat information
		int position = 0;
		for(int i = 0; i < lrc.size(); ++i) {
			lrc.set(i, lrc.get(i) + beatArray.get(position));
			//System.out.println("add " + beatArray.get(position));
			position = (position+1) % beatArray.size();
		}
		
		
		//System.out.println(lrc);
		
		//generate melo array
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
				//System.out.println("cannnot find for any size");
				startPos++;
				melo.add(new Integer(0));
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
						find = true;
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
				//System.out.println("cannnot find for any size");
				startPos++;
				dur.add(a[index%3]);
			}
			
		}
		
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


	private ArrayList<Integer> parseLrc() throws IOException {
		ArrayList<Integer> result = new ArrayList<Integer>();
		
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
				result.add(new Integer(99));
			}
				
			else {
				String stress = ws.stress;
				for(int i = 0;i < stress.length();++i) {
					result.add(Integer.parseInt(stress.substring(i, i+1)));
				}
			}
		}
		
		
		return result;
		
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
			t.add(me);*/
			
			
			
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
	}
	
	private static void readInPatterns(String filename,boolean LRC) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner s = new Scanner(new File(filename));
		String line;
		ArrayList<Integer> lrc = new ArrayList<Integer>();
		ArrayList<Integer> melo = new ArrayList<Integer>();
		
		
		int size  = - 1;


		int count = 0;

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
					//System.out.print("[" + one[i] + "]");
					lrc.add(Integer.parseInt(one[i]));
					melo.add(Integer.parseInt(two[i]));
				}
					
				//System.out.println();
			}
			if(LRC) {
				allPatternOne.get(size).add(new SequencePair(lrc,melo));
			}
			else {
				allPatternTwo.get(size).add(new SequencePair(lrc,melo));
			}
		}	
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
