package OCR.JAVA.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;
import OCR.JAVA.Studio;

@SuppressWarnings("unused")
public class StudioCounter {

	private static final String[] STATE_OWNED = {"Jiangsu Film Studio", "Shandong Film Studio", "Hunan Film Studio",
				"Zhejiang Film Studio", "Xinjiang Film Studio", "Shanghai Animation Film Studio", "Tianjin Film Studio",
				"August First Film Studio", "Tianma Film Studio", "Qinghai Film Studio", "Emei Film Studio",
				"Northeast Film Studio", "Jiangnan Film Studio", "Haiyan Film Studio", "Xi'an Film Studio", "Central Newsreel and Documentary Film Studio",
				"Anhui Film Studio", "Shanghai Film Studio", "Wuhan Film Studio", "Beijing Film Studio",
				"Studio of Beijing Film Academy", "Inner Mongolia Film Studio", "Lanzhou Film Studio",
				"Changchun Film Studio", "Pearl River Film Studio", "Harbin Film Studio"};
	private static final String[] NON_GEO = {"August First Film Studio", "Tianma Film Studio", "Jiangnan Film Studio",
				"Haiyan Film Studio", "Central Newsreel and Documentary Film Studio"};
	private static final String[] NON_MAINLAND = {"Hong Kong", "Western Europe", "Soviet Union"};

	public static void main(String[] args) throws IOException {
		//geoStudio();
		//System.out.println();
		//countStudio();
		//allCats();
		studioCats();
		//HKCanton();
	}

	private static void HKCanton() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> catsCount = new HashMap<String, Integer>();
		int minor = 0, major = 0, inter = 0, cross = 0;

		for (Film film : films) {
			String[] cats = film.getCategory();
			if(cats.length < 2)	continue;
			boolean canton = false, hk = false;

			for(String c : cats) {
				if(c.equals("Canton")) canton = true;
				if(c.equals("Hong Kong")) hk = true;
			}

			if(canton && hk) {
				System.out.println(film.title + ": " + film.productionToString());
			}
		}
	}


	private static void studioCats() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		System.out.println(films.size() + " films.");
		ArrayList<Studio> studios = new ArrayList<Studio>();
		HashMap<String, Integer> catsCount = new HashMap<String, Integer>();
		int total = 0;

		for (Film film : films) {
			Studio[] studiosf = film.production;
			
			for(Studio studio : studiosf) {
				if(!studios.contains(studio)) {
					studios.add(studio);

					String cat = studio.category;

					boolean non_major = false, non_inter = false, non_mainland = false;
					if(!cat.equals("Beijing") && !cat.equals("Shanghai (private)") &&
						!cat.equals("Shanghai (state)") && !cat.equals("Northeast") && !cat.equals("Shanghai (roc)")) non_major = true;
					if(!cat.equals("Canton") && !cat.equals("Xi'an")) non_inter = true;
					for(String non : NON_MAINLAND) {
						if(cat.equals(non)) non_mainland = true;
					}

					if(non_mainland) continue;
					if(!(non_major && non_inter)) continue;
					//if(non_major) continue;

					total++;
					if(catsCount.containsKey(cat)) {
						catsCount.put(cat, catsCount.get(cat) + 1);
					} else {
						catsCount.put(cat, 1);
					}
				}
			}
		}
		System.out.println(studios.size() + " studios.");
		for(String cat : catsCount.keySet()) {
			System.out.println(cat + ": " + catsCount.get(cat));
		}
		System.out.println("Total: " + total);
		System.out.println("Total cats: " + catsCount.size());
	}

	private static void allCats() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> catsCount = new HashMap<String, Integer>();
		int minor = 0, major = 0, inter = 0, cross = 0;

		for (Film film : films) {
			String[] cats = film.getCategory();
			boolean hasMinorRegion = false, hasInterRegion = false, hasMajorRegion = false;

			for(String c : cats) {
				boolean non_major = false, non_inter = false, non_mainland = false;
				if(!c.equals("Beijing") && !c.equals("Shanghai (private)") &&
					!c.equals("Shanghai (state)") && !c.equals("Northeast") && !c.equals("Shanghai (roc)")) non_major = true;
				if(!c.equals("Canton") && !c.equals("Xi'an")) non_inter = true;
				for(String non : NON_MAINLAND) {
					if(c.equals(non)) non_mainland = true;
				}

				if(non_mainland) continue;

				if(non_inter && non_major) {
					hasMinorRegion = true;
				}
				if(non_inter && !non_major) {
					hasMajorRegion = true;
				}
				if(!non_inter && non_major) {
					hasInterRegion = true;
				}

				/*if(catsCount.containsKey(c)) {
					catsCount.put(c, catsCount.get(c) + 1);
				} else {
					catsCount.put(c, 1);
				}*/
			}

			if(hasMinorRegion) {
				minor++;
			}
			if(hasInterRegion) {
				inter++;
			}
			if(hasMajorRegion) {
				major++;
			}

			if(hasInterRegion && hasMajorRegion) {System.out.println(film.title + "INTER MAJOR");cross++;}
			if(hasInterRegion && hasMinorRegion) {System.out.println(film.title + "INTER MINOR");cross++;}
			if(hasMajorRegion && hasMinorRegion) {System.out.println(film.title + "MAJOR MINOR");cross++;}
		}

		System.out.println("Minor: " + minor);
		System.out.println("Inter: " + inter);
		System.out.println("Major: " + major);
		System.out.println("Cross: " + cross);
	}

	private static void countStudio() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> studioCounter = new HashMap<String, Integer>();
		HashMap<String, Integer> studioReels = new HashMap<String, Integer>();
		HashMap<String, Integer> studioWithReels = new HashMap<String, Integer>();

		for (Film film : films) {
			Studio[] studios = film.production;
			
			for(Studio studio : studios) {
				String name = studio.name;

				boolean isStateOwned = false;
				for(String stateOwned : STATE_OWNED) {
					if(name.equals(stateOwned)) {
						isStateOwned = true;
					}
				}
				if(!isStateOwned) {
					continue;
				}

				name = studio.chn;

				if (studioCounter.containsKey(name)) {
					studioCounter.put(name, studioCounter.get(name) + 1);
				} else {
					studioCounter.put(name, 1);
				}

				if(film.reels <= 0) continue;
				if(studioReels.containsKey(name)) {
					studioReels.put(name, studioReels.get(name) + film.reels);
				} else {
					studioReels.put(name, film.reels);
				}
				studioWithReels.put(name, studioWithReels.getOrDefault(name, 0) + 1);
			}
		}

		for (String studio : studioCounter.keySet()) {
			System.out.println(studio + ": " + studioCounter.get(studio));
		}

		for (String studio : studioReels.keySet()) {
			System.out.println(studio + ": " + studioReels.get(studio) + " reels, " + studioWithReels.get(studio) + " films with reel info.");
			System.out.println("Average: " + ((float)studioReels.get(studio) / (float)studioWithReels.get(studio)));
		}
	}

	private static void geoStudio() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> studioCounter = new HashMap<String, Integer>();

		for (Film film : films) {
			Studio[] studios = film.production;
			
			for(Studio studio : studios) {
				String name = studio.name;

				boolean isStateOwned = false;
				for(String stateOwned : STATE_OWNED) {
					if(name.equals(stateOwned)) {
						isStateOwned = true;
					}
				}
				if(!isStateOwned) {
					continue;
				}

				name = studio.chn;

				if (studioCounter.containsKey(name)) {
					studioCounter.put(name, studioCounter.get(name) + 1);
				} else {
					studioCounter.put(name, 1);
				}
			}
		}

		/*for (String studio : studioCounter.keySet()) {
			System.out.println(studio + ": " + studioCounter.get(studio));
		}*/
		System.out.println("Total state-owned: " + studioCounter.size());
		System.out.println("Total non-geographic: " + NON_GEO.length);
	}

}