package OCR.JAVA.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

public class SearchActor {
	private static String name = "祝希娟";

	public static void main(String args[]) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		for(Film f : films) {
			if(f.hasActorOrActress(name)) {
				String[] cats = f.getCategory();
				String cat = "";
				for(String c : cats) {
					cat += c + "、";
				}
				cat = cat.substring(0, cat.length() - 1);
				System.out.println(f.title + "(" + f.key + ", " + cat + ")");

				if(map.containsKey(cat)) {
					map.put(cat, map.get(cat) + 1);
				} else {
					map.put(cat, 1);
				}
			}
		}

		//Get max from map
		int max = 0;
		String mainRegion = "";
		for(String key : map.keySet()) {
			if(map.get(key) > max) {
				max = map.get(key);
				mainRegion = key;
			}
		}

		System.out.println("\nMain region: " + mainRegion + " (" + max + ")");
	}
}