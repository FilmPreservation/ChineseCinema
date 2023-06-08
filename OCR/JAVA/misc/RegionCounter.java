package OCR.JAVA.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

public class RegionCounter {

	private static String[] NON_MAINLAND = {"Hong Kong", "Western Europe", "Soviet Union"};

	public static void main(String[] args) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		HashMap<String, Integer> firstYear = new HashMap<String, Integer>();
		HashMap<String, Integer> lastYear = new HashMap<String, Integer>();

		for(Film f : films) {
			String[] cats;
			if(f.key.equals("xiao zhu yan kai")) {
				cats = new String[2];
				cats[0] = "Changchun";
				cats[1] = "Harbin";
			} else {
				cats = f.getCategory();
			}

			for(String c : cats) {
				//If c is in NON_MAINLAND, skip
				boolean skip = false;
				for(String s : NON_MAINLAND) {
					if(c.equals(s)) {
						skip = true;
						break;
					}
				}
				if(skip) {
					continue;
				}

				if(c.contains("Shanghai"))
					c = "Shanghai";
				if(c.equals("Northeast"))
					c = "Changchun";

				if(map.containsKey(c)) {
					map.put(c, map.get(c) + 1);
				} else {
					map.put(c, 1);
				}

				if(!firstYear.containsKey(c)) {
					firstYear.put(c, f.year);
				}else{
					if(firstYear.get(c) > f.year) {
						firstYear.put(c, f.year);
					}
				}

				if(!lastYear.containsKey(c)) {
					lastYear.put(c, f.year);
				}else{
					if(lastYear.get(c) < f.year) {
						lastYear.put(c, f.year);
					}
				}
			}
		}

		//Print all maps
		for(String key : map.keySet()) {
			System.out.println(key + ": " + map.get(key));
			//System.out.println("Debut year: " + firstYear.get(key));
		}

		ArrayList<String> regs = new ArrayList<String>();
		ArrayList<String> regs2 = new ArrayList<String>();

		for(int year = 1949; year <= 1966; year++) {
			int yearCount = 0;
			for(String key : map.keySet()) {
				if(firstYear.get(key) <= year) {
					yearCount++;
					regs.add(key);
				}
			}
			System.out.println(year + ": " + yearCount);
			for(String s : regs) {
				if(!regs2.contains(s))
					regs2.add(s);
				else
					continue;
				System.out.print(s + " ");
			}
			System.out.println();
		}
	}
	
}
