package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HyphenCheck {
	
	public static void main(String[] args) throws IOException {
		File out = new File("un-hyphen.csv");
		File in = new File("descripted.csv");
		BufferedReader reader = new BufferedReader(new FileReader(in));
		BufferedWriter writer = new BufferedWriter(new FileWriter(out));
		String line = "", prevLine = "";

		while((line = reader.readLine()) != null) {
			if(prevLine.endsWith("-")) {
				char beforeHyp = prevLine.charAt(prevLine.length() - 2);
				System.out.println(prevLine + '\n' + line);
				if(Character.isDigit(beforeHyp)) {
					System.out.println("Digita before hyphen detected, procceed?");
					System.in.read();
				}
				String splittedWord = line.split(" ", 2)[0];
				if(splittedWord.length() < line.length()) {
					line =  line.substring(splittedWord.length() + 1, line.length());
				}else {
					line = "";
				}
				writer.write(prevLine.substring(0, prevLine.length() - 1) + splittedWord + '\n');
			}else
				writer.write(prevLine + '\n');
			prevLine = line;
		}
		writer.write(prevLine);
		writer.close();
		reader.close();
	}
}
