package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Production {
	private static String input = "OCR/JAVA/postprocessing/ProductionEntries.csv",
		output = "ExpectedStudios.csv",
		output2 = "OCR/JAVA/postprocessing/AllProductionEntries.csv";
	private static ArrayList<Pro> productions = new ArrayList<Pro>();
	private static class Pro {
		public String name, year, film;
		public Pro(String name, String year, String film) {
			this.name = name;
			this.year = year;
			this.film = film;
		}
		@Override
		public String toString() {
			return name + "," + year + "," + film;
		}
	}

	public static void main(String[] args) throws IOException {
		File in = new File(input);
		File out = new File(output);
		File out2 = new File(output2);
		BufferedReader read = new BufferedReader(new FileReader(in));
		BufferedWriter write = new BufferedWriter(new FileWriter(out));
		BufferedWriter write2 = new BufferedWriter(new FileWriter(out2));
		String line;
		int st = 0;
		while((line = read.readLine()) != null) {
			String[] asp = line.split(",");
			String[] studios = asp[2].split("&");
			for (int i=0; i<studios.length; i++) {
				studios[i] = studios[i].trim();
				Pro temp = new Pro(studios[i], asp[1], asp[0]);
				write2.write(temp.toString() + "\n");
				addToArray(temp);
				st++;
			}
		}
		System.out.println("Read studios: " + st);
		read.close();

		write.write("Production,Start Year,First Film\n");
		for (Pro pro : productions) {
			write.write(pro.toString() + "\n");
		}
		write.close();
		write2.close();
	}

	public static void addToArray(Pro entry) {
		for (Pro en : productions) {
			if(en.name.equals(entry.name)){
				return;
			}
		}
		productions.add(entry);
	}
}
