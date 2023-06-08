package OCR.JAVA.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ListAllCharacters {
	private static String SRC = "OCR/JAVA/Main-PlayedCharacters.csv", TAR = "Topic/ultimate_character_list(bianmu).txt";

	//Use this to store the name loaded before
	private static String bufferedName = "";

	public static void main(String[] args) throws IOException {
		ArrayList<String> characters = new ArrayList<>();

		BufferedReader br = new BufferedReader(new FileReader(SRC));
		String line = null;
		while((line = br.readLine()) != null) {
			String[] arr = line.split(",");
			if(arr.length < 2) continue;
			String namesString = arr[1];
			String[] names = namesString.split("\\{LINE_CUT\\}");

			for (String name : names) {
				try {
					String[] actingAndCharacter = name.split("-") ;
					String character = actingAndCharacter[1];
					character = character.replaceAll(" ", "");

					//If bufferedName is longer than 2 and the first 2 Chinese Characters in bufferedName is
					//the same as the new character name, then this character is a variant of the previous name (e.g. 工人甲 [Worker A], 工人乙 [Worker B]])
					//Because such similar names appearing so closely is rare, we can safely ignore the case if this is the name of a real character
					if(bufferedName.length() >= 2) {
						if(bufferedName.substring(0, 2).equals(character.substring(0, 2)))
							continue;
					}

					//If the name contains brackers, only keep the content before the bracket
					if(character.contains("（")) {
						character = character.substring(0, character.indexOf("（"));
					}
					if(character.contains("(")) {
						character = character.substring(0, character.indexOf("("));
					}

					if(!characters.contains(character)) {
						bufferedName = character;
						characters.add(character);
					}
				}catch (Exception e) {
					System.out.println(name);
				}
			}
		}

		br.close();

		BufferedWriter bw = new BufferedWriter(new FileWriter(TAR));
		for (String character : characters) {
			bw.write(character);
			bw.newLine();
		}
		bw.close();
	}
}
