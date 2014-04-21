package myComposer;

import java.util.ArrayList;
import java.util.HashMap;


public class Test {

	public static void main(String[] args) {
		ArrayList<Integer> first = new ArrayList<Integer>();
		ArrayList<Integer> third = new ArrayList<Integer>();
		
		ArrayList<Integer> second = new ArrayList<Integer>();
		for(int i = 0 ; i < 10;++i) {
			first.add(new Integer(i));
			third.add(new Integer(i));
			second.add(new Integer(i+1));
		}
		HashMap<ArrayList<Integer>, ArrayList<Integer>> map = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();
		map.put(first, second);
		
		
		System.out.println(map.get(third));
		
	}
	
	private static boolean checkArray(ArrayList<Integer> first, ArrayList<Integer> second) {
		// TODO Auto-generated method stub
		if(first.size() != second.size())		return false;
		for(int i = 0; i < first.size();++i) {
			if(first.get(i).equals(second.get(i))) {
				System.out.println(first.get(i));
				return false;
			}
		}
		return true;
	}
}
