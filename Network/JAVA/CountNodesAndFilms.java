package Network.JAVA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import OCR.JAVA.Film;

public class CountNodesAndFilms {
	private static String[] tags = {"1949", "1950-1957", "1950-1953", "1954-1957", "1958-1960", "1961-1966", "1961-1963", "1964-1966"};

	public static void main(String[] args) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		System.out.println("Tag,Nodes,Films");

		for (String tag : tags) {
			int startYear = 0;
			int endYear = 0;
			if(tag.contains("-")) {
				startYear = Integer.parseInt(tag.split("-")[0]);
				endYear = Integer.parseInt(tag.split("-")[1]);
			}else{
				startYear = Integer.parseInt(tag);
				endYear = Integer.parseInt(tag);
			}
			int filmCon = 0;

			for(Film f : films) {
				int year = f.year;
				if (year >= startYear && year <= endYear) {
					filmCon++;
				}
			}

			File node = new File("Network/csv/nodes/nodes-" + tag + ".csv");
			BufferedReader br = new BufferedReader(new FileReader(node));
			String line = br.readLine();
			int nodeCon = 0;
			while ((line = br.readLine()) != null) {
				if(line.contains(","))
					nodeCon++;
			}
			br.close();

			System.out.println(tag + "," + nodeCon + "," + filmCon);
		}
	}
}
