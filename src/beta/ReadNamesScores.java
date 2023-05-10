package beta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReadNamesScores {
	public static String[] readNameScore() {
		
		String[] info = new String[2];
		BufferedReader reader;
	
		
		try {
			reader = new BufferedReader(new FileReader("src/res/highscores.txt"));
			
			String line = reader.readLine();
			while(line != null) {
				String[] fields = line.split(" +");
				
				if(fields.length == 2) {
					
					info[0] = fields[0];
					info[1] = fields[1];
				}
				line = reader.readLine();
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return info;
	}
	
	public static void updateNamesScore(String name, int score) {
				
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter("src/res/highscores.txt"));
		
			String s = String.valueOf(score);
			writer.write(name+" +"+s);
			
			writer.flush();
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
