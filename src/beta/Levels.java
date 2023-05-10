package beta;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Levels {
	static int[] levels = {1, 2, 3, 4, 5};
	static int[] shuffledLevels = shuffleLevels(levels);
	final static String l1 = "src/res/level1.txt";
	final static String l2 = "src/res/level2.txt";
	final static String l3 = "src/res/level3.txt";
	final static String l4 = "src/res/level4.txt";
	final static String l5 = "src/res/level5.txt";
	
	public int[] getLevelList() {
		return shuffledLevels;
	}
	
	public static int[] shuffleLevels(int[] levels) {
		Random rand = new Random();
		
		for (int i = 0; i < levels.length; i++) {
			int randomIndexToSwap = rand.nextInt(levels.length);
			int temp = levels[randomIndexToSwap];
			levels[randomIndexToSwap] = levels[i];
			levels[i] = temp;
		}
		
		return levels;
	}
	
	public short[] getNthLevelFromList(int n) {
		int nthLevel = shuffledLevels[n % 5];
		String file = "";
		
		if (nthLevel == 1) {
			file = l1;
		}
		else if (nthLevel == 2) {
			file = l2;
		}
		else if (nthLevel == 3) {
			file = l3;
		}
		else if (nthLevel == 4) {
			file = l4;
		}		
		else if (nthLevel == 5) {
			file = l5;
		}
		
		
		BufferedReader bf;
		ArrayList<Short> temp = new ArrayList<Short>();
		try {
			bf = new BufferedReader(new FileReader(file));
			String line = bf.readLine();
		
			while (line != null) {
				short num = Short.valueOf(line);
				temp.add(num);
				line = bf.readLine();
			}
			
			bf.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		short[] level = new short[temp.size()];
		for (int i = 0; i < level.length; i++) {
			level[i] = temp.get(i);
		}
		return level;
 	}
		
}
