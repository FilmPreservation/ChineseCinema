package OCR.JAVA.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import OCR.JAVA.Film;
import OCR.JAVA.Studio;

public class Coproduction {
	private static String[] NON_MAINLAND = {"（法国）", "（苏联）", "（香港）"};
	private static String enquired_studio = "";

	public static void main(String[] args) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		ArrayList<String> studioList = new ArrayList<String>();

		for(Film film : films) {
			Studio[] studios = film.production;

			for(Studio studio : studios) {
				if(!studioList.contains(studio.chn)) {
					studioList.add(studio.chn);
				}
			}

			if(studios.length < 2) continue;
			String[] names = new String[studios.length];
			for(int i = 0; i < studios.length; i++) {
				names[i] = studios[i].name;
			}
			if(!enquired_studio.isBlank() && !Arrays.asList(names).contains(enquired_studio)) continue;

			System.out.println(film.title + ":");
			for(Studio studio : studios) {
				System.out.print(studio.chn + " ");
			}
			System.out.println();
			System.out.println();
		}

		HashMap<String, Integer> coops = new HashMap<String, Integer>();
		HashMap<String, Integer> allops = new HashMap<String, Integer>();

		for(String studio : studioList) {
			for(Film film : films) {
				Studio[] studios = film.production;
				String[] names = new String[studios.length];
				for(int i = 0; i < studios.length; i++) {
					names[i] = studios[i].chn;
				}
				if(!Arrays.asList(names).contains(studio)) continue;

				boolean isCoop = studios.length > 1;

				if(isCoop) {
					if(coops.containsKey(studio)) {
						coops.put(studio, coops.get(studio) + 1);
					} else {
						coops.put(studio, 1);
					}
				}

				if(allops.containsKey(studio)) {
					allops.put(studio, allops.get(studio) + 1);
				} else {
					allops.put(studio, 1);
				}
			}
		}

		System.out.println("Cooperation ratio (" + allops.size() + ")");
		for(String key : allops.keySet()) {
			int cooped = coops.getOrDefault(key, 0);
			float ratio = (float) cooped / (float) allops.get(key);
			System.out.println(key + ": " + ratio + "(" + cooped + "/" + allops.get(key) + ")");
		}
		System.out.println();

		for(Film film : films) {
			Studio[] studios = film.production;

			if(studios.length == 1) {
				studioList.remove(studios[0].chn);
			}
		}

		System.out.println("Dependent studios:");
		for(String studio : studioList) {
			boolean skip = false;
			for (String string : NON_MAINLAND) {
				if(studio.contains(string)) skip = true;
			}
			if(skip) continue;
			System.out.println(studio);
		}
	}

}