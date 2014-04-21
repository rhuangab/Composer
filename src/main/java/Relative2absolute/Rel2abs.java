package Relative2absolute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import myComposer.Composer;
import myComposer.MidiPlayer;

import org.json.JSONArray;
import org.json.JSONObject;


public class Rel2abs {
	
	public static String[] SofaName = new String[] {"do","re","mi","fa","so","la","ti"};

	public static boolean [][] major_notes = {
		{true,false,true,false,true,true,false,true,false,true,false,true},//C-Major
		{true,false,true,false,true,false,true,true,false,true,false,true},//G-Major
		{false,true,true,false,true,false,true,true,false,true,false,true},//D-Major
		{false,true,true,false,true,false,true,false,true,true,false,true},//A-Major
		{false,true,false,true,true,false,true,false,true,true,false,true},//E-Major
		{false,true,false,true,true,false,true,false,true,false,true,true},//B-Major
		{true,false,true,false,true,true,false,true,false,true,true,false},//F-Major
		{true,false,true,true,false,true,false,true,false,true,true,false},//bB-Major
		{true,false,true,true,false,true,false,true,true,false,true,false},//bE-Major
		{true,true,false,true,false,true,false,true,true,false,true,false},//bA-Major
		{true,true,false,true,false,true,true,false,true,false,true,false},//bD-Major
		{true,true,false,true,false,true,true,false,true,false,false,true},//bG-Major
		};
	
	public static int [][] major_sofa_pitch = {
		{0,2,4,5,7,9,11},//C-Major
		{7,9,11,0,2,4,6},//G-Major
		{2,4,6,7,9,11,1},//D-Major
		{9,11,1,2,4,6,8},//A-Major
		{4,6,8,9,11,1,3},//E-Major
		{11,1,3,4,6,8,10},//B-Major
		{5,7,9,10,0,2,4},//F-Major
		{10,0,2,3,5,7,9},//bB-Major
		{3,5,7,8,10,0,2},//bE-Major
		{8,10,0,1,3,5,7},//bA-Major
		{1,3,5,6,8,10,0},//bD-Major
		{8,10,11,1,3,5,6},//bG-Major
		};
	
	public static int [][] base_period_second_notes = {
		{60,64,67}, {67,71,74}, {62,66,69}, {69,73,76}, {64,68,71}, {71,75,78},
		{65,69,72}, {70,74,77}, {63,67,71}, {68,72,75}, {61,65,68}, {66,70,73}
		};
	
	public enum EndType{
		LastPeriod,Period,Comma
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvalidMidiDataException 
	 */
	public static void main(String[] args) throws IOException, InvalidMidiDataException {
		String input  = "I'm a hot air balloon that could go to space With the air, like I don't care baby by the way";
	    //String input = "This is ! ? good.";
		//input = "Close your eyes and roll a dice Under the board there's a compromise If after all we only lived twice Which lies the run road to paradise Don't say a word, here comes the break of the day And wide clouds of sand raised by the wind of the end of May Close your eyes and make a betFace to the glare of the sunset";
//		Composer com = new Composer(input,4,4);
//		
//		ArrayList<Integer>test = new ArrayList<Integer>();
//		test = com.getMelo();
//		EndType myType = EndType.LastPeriod;
//		ArrayList<Integer>output = new ArrayList<Integer>();
//		output.add(EndNotesGenerator(0,myType,test.get(test.size() - 1)).get(1));
//		output.add(EndNotesGenerator(0,myType,test.get(test.size() - 1)).get(0));
//		
//		for (int i = test.size() - 2; i>=0 ; i--){
//			output.add(adjacentNoteGenerator(0,test.get(i),output.get(test.size() - i - 1)));
//		}
//		
//		ArrayList<Integer> myMelody = new ArrayList<Integer>();
//		for (int j = output.size() - 1; j >=0; j--){
//			myMelody.add(output.get(j));
//			System.out.println(output.get(j));
//		}
		getJsonOutput(input, 4, 2);
	}//211111112-221111-12-432-3164
	//1-33-31111111-4111111111169
	
	public static JSONArray getJsonOutput(String inputLrc, int octave, int step) throws IOException
	{
		inputLrc = inputLrc.replace("‘", "'");
		inputLrc = inputLrc.replace("？", "?");
		inputLrc = inputLrc.replace("！", "!");
		inputLrc = inputLrc.replace("，", ",");
		inputLrc = inputLrc.replace("。", ".");
		Composer com = new Composer(inputLrc,octave,step);
		ArrayList<Integer>test = new ArrayList<Integer>();
		test = com.getMelo();
		for(int i=0;i<com.getDur().size();i++) System.out.print(com.getDur().get(i));
		//System.out.println(test.size() + " "+ com.getDur().size());
		EndType myType = EndType.LastPeriod;
		ArrayList<Integer>output = new ArrayList<Integer>();
		ArrayList<Integer> endNotes = EndNotesGenerator(0,myType,test.get(test.size() - 1));
		output.add(endNotes.get(1));
		output.add(endNotes.get(0));
		
		for (int i = test.size() - 2; i>=0 ; i--){
			output.add(adjacentNoteGenerator(0,test.get(i),output.get(test.size() - i - 1)));
		}
		
		ArrayList<Integer> myMelody = new ArrayList<Integer>();
		
		JSONArray noteArray = new JSONArray();
		for (int j = output.size() - 1; j >=0; j--){
			//myMelody.add(output.get(j));
			System.out.println(output.get(j));
			//System.out.println(com.getDur().get(j-1));
			JSONObject jo = new JSONObject();
			int key = output.get(j);
			while(key > 108) key -= 12;
			while(key < 21) key += 12;
			jo.put("keys", key);
			jo.put("duration","4");
			noteArray.put(jo);
		}
		//System.out.println(noteArray.toString());
		
		return noteArray;
	}
	
	
	public static String getRandomNote(ArrayList<String> arr){
		return arr.get((int)(Math.random() * arr.size()));
	}
	
	public static Integer adjacentNoteGenerator (int my_tone, int diff, int latterPitch){
		int newPitchValue = latterPitch + diff;
		int valueOfPitch = newPitchValue;
		int backupPitch1,backupPitch2;
		backupPitch1 = backupPitch2 = newPitchValue;
		boolean matchJudge = false;

		if (!major_notes[my_tone][newPitchValue % 12]){
			while (!matchJudge){
				if (major_notes[my_tone][++backupPitch1 % 12]){
					matchJudge = true;
					valueOfPitch = backupPitch1;
					}
				else if (major_notes[my_tone][--newPitchValue % 12]){
					matchJudge = true;
					valueOfPitch = backupPitch2;					
					}
				}
			}
		
		return new Integer (valueOfPitch);
		
	}
	
	public static ArrayList<Integer> EndNotesGenerator(int my_tone,EndType end_type, int diff){
		ArrayList<Integer> endingNotes = new ArrayList<Integer>(Arrays.asList(new Integer(0),new Integer(0)));
		
		ArrayList<String> noteCombinationPeriodFirst = new ArrayList<String>(Arrays.asList("re","ti","so"));
		ArrayList<String> noteCombinationPeriodSecond = new ArrayList<String>(Arrays.asList("so","mi","do"));
		ArrayList<String> noteCombinationCommaFirst = new ArrayList<String>(Arrays.asList("so","mi","do"));
		ArrayList<String> noteCombinationCommaSecond = new ArrayList<String>(Arrays.asList("re","ti","so","do","la","fa"));
		
		switch(end_type){
		case LastPeriod:
			endingNotes.set(1, new Integer(base_period_second_notes[my_tone][0]));// The last note must be "do"
			int previousNotePitch = base_period_second_notes[my_tone][0] + diff; 
			
			if (previousNotePitch % 12 == major_sofa_pitch[my_tone][1] || previousNotePitch % 12 == major_sofa_pitch[my_tone][4] || previousNotePitch % 12 == major_sofa_pitch[my_tone][6])
				endingNotes.set(0, new Integer(previousNotePitch)); // the second last note is one among "re","ti","so"
			else{
				int tempNote1 = previousNotePitch;
				int tempNote2 = previousNotePitch;
				boolean tempNoteJudge = false;
				while (!tempNoteJudge){
					tempNote1++;
					tempNote2--;
					if (tempNote1 % 12 == major_sofa_pitch[my_tone][1] || tempNote1 % 12 == major_sofa_pitch[my_tone][4] || tempNote1 % 12 == major_sofa_pitch[my_tone][6]){
						endingNotes.set(0, new Integer(tempNote1));
						tempNoteJudge = true;
					}
					else if (tempNote2 % 12 == major_sofa_pitch[my_tone][1] || tempNote2 % 12 == major_sofa_pitch[my_tone][4] || tempNote2 % 12 == major_sofa_pitch[my_tone][6]){
						endingNotes.set(0, new Integer(tempNote2));
						tempNoteJudge = true;
					}
				}
			}
		
			break;
			
		case Period:
			int temp = (int)(Math.random() * 3); //generate random number among 0,1,2
			endingNotes.set(1, new Integer(base_period_second_notes[my_tone][temp]));// The last note can be "do","mi","so"
			int period_previousNotePitch = base_period_second_notes[my_tone][temp] + diff; 
			
			if (period_previousNotePitch % 12 == major_sofa_pitch[my_tone][1] || period_previousNotePitch % 12 == major_sofa_pitch[my_tone][4] || period_previousNotePitch % 12 == major_sofa_pitch[my_tone][6])
				endingNotes.set(0, new Integer(period_previousNotePitch)); // the second last note is one among "re","ti","so"
			else{
				int tempNote1 = period_previousNotePitch;
				int tempNote2 = period_previousNotePitch;
				boolean tempNoteJudge = false;
				while (!tempNoteJudge){
					tempNote1++;
					tempNote2--;
					if (tempNote1 % 12 == major_sofa_pitch[my_tone][1] || tempNote1 % 12 == major_sofa_pitch[my_tone][4] || tempNote1 % 12 == major_sofa_pitch[my_tone][6]){
						endingNotes.set(0, new Integer(tempNote1));
						tempNoteJudge = true;
					}
					else if (tempNote2 >= 0 && (tempNote2 % 12 == major_sofa_pitch[my_tone][1] || tempNote2 % 12 == major_sofa_pitch[my_tone][4] || tempNote2 % 12 == major_sofa_pitch[my_tone][6])){
						endingNotes.set(0, new Integer(tempNote2));
						tempNoteJudge = true;
					}
				}
			}
			
			break;
			
		case Comma:
			// generate random number comma_temp except 2 with equal probability
			int comma_temp = (int)(Math.random() * 7); 
			if (comma_temp == 2){
				int prob = (int)(Math.random() * 6); 
				if (prob <= 1)
					comma_temp =  (int)(Math.random() * 2); 
				else
					comma_temp =  (int)(Math.random() * 4) + 3; 
			}
				
			endingNotes.set(1, new Integer(base_period_second_notes[my_tone][comma_temp]));// The last note can not be "mi"
			int comma_previousNotePitch = base_period_second_notes[my_tone][comma_temp] + diff; 
			
			if (comma_previousNotePitch % 12 == major_sofa_pitch[my_tone][0] || comma_previousNotePitch % 12 == major_sofa_pitch[my_tone][2] || comma_previousNotePitch % 12 == major_sofa_pitch[my_tone][4])
				endingNotes.set(0, new Integer(comma_previousNotePitch)); // the second last note is one among "re","ti","so"
			else{
				int tempNote1 = comma_previousNotePitch;
				int tempNote2 = comma_previousNotePitch;
				boolean tempNoteJudge = false;
				while (!tempNoteJudge){
					tempNote1++;
					tempNote2--;
					if (tempNote1 % 12 == major_sofa_pitch[my_tone][0] || tempNote1 % 12 == major_sofa_pitch[my_tone][2] || tempNote1 % 12 == major_sofa_pitch[my_tone][4]){
						endingNotes.set(0, new Integer(tempNote1));
						tempNoteJudge = true;
					}
					else if (tempNote2 >= 0 && (tempNote2 % 12 == major_sofa_pitch[my_tone][0] || tempNote2 % 12 == major_sofa_pitch[my_tone][2] || tempNote2 % 12 == major_sofa_pitch[my_tone][4])){
						endingNotes.set(0, new Integer(tempNote2));
						tempNoteJudge = true;
					}
				}
			}
			
			break;
			
		default:
			break;
				
		}
		
		return endingNotes;
		
	}
	
	/*
	public static ArrayList<String> endingNotesGenerator(int type){
		ArrayList<String> endingNotes = new ArrayList<String>(Arrays.asList("so","do"));
		
		ArrayList<String> noteCombinationPeroidFirst = new ArrayList<String>(Arrays.asList("re","ti","so"));
		ArrayList<String> noteCombinationPeroidSecond = new ArrayList<String>(Arrays.asList("so","mi","do"));
		ArrayList<String> noteCombinationCommaFirst = new ArrayList<String>(Arrays.asList("so","mi","do"));
		ArrayList<String> noteCombinationCommaSecond = new ArrayList<String>(Arrays.asList("re","ti","so","do","la","fa"));
		
		switch(type){
		case 0: //Ending sentence with period, with the last note being "do"
			endingNotes.set(0, getRandomNote(noteCombinationPeroidFirst));
			endingNotes.set(1, "do");
			break;
		case 1: //None-ending sentence with period
			endingNotes.set(0, getRandomNote(noteCombinationPeroidFirst));
			endingNotes.set(1, getRandomNote(noteCombinationPeroidSecond));
			break;
		case 2: //sentence with comma
			endingNotes.set(0, getRandomNote(noteCombinationCommaFirst));
			endingNotes.set(1, getRandomNote(noteCombinationCommaSecond));
			break;
		
		default:
			break;		
		}
		
		return endingNotes;
	}
	*/
	
	public ArrayList<Integer> generateAbsolueMed(ArrayList<Integer> melo) {
		ArrayList<Integer> absoluteMed = new ArrayList<Integer>();
		absoluteMed.add(0, 60);
		for(int i = absoluteMed.size() - 1; i >= 0 ; i--){
			
		}
		
		return null;
		
	}

}
