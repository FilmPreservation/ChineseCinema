package OCR.JAVA.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import CV.JAVA.VideoKey;
import OCR.JAVA.Film;

public class GetFeatureAndMusicalInMajorRegions {
	private static String[] wantedRegions = {"Shanghai (state)", "Beijing", "Northeast", "Canton", "Xi'an"};
	private static String[] wantedTypes = {"Feature", "Musical"};

	public static void main(String[] args) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		ArrayList<String> videos = CV.JAVA.VideoKey.allVideoKeys();
		HashMap<String, Integer> filmFrames = new HashMap<String, Integer>();

		int totalFrames = 0, totalFilms = 0;

		BufferedReader br = new BufferedReader(new FileReader("CV/crowd/processed_videos.csv"));
		String line = br.readLine();
		while((line = br.readLine()) != null) {
			String[] split = line.split(",");
			filmFrames.put(VideoKey.videoToFilmKey(split[0]), Integer.parseInt(split[1]));
		}
		br.close();

		for (String region : wantedRegions) {
			ArrayList<Integer> totals = new ArrayList<Integer>();
			ArrayList<Integer> videoTotals = new ArrayList<Integer>();
			ArrayList<String> foundGenres = new ArrayList<String>();

			for (String type : wantedTypes) {
				int count = 0, videoCount = 0;
				for (Film film : films) {
					String cat = "";
					String[] cats = film.getCategory();

					if(cats.length == 1) {
						cat = cats[0];
					} else if(cats.length == 2) {
						if(cats[0].equals("Canton") || cats[1].equals("Canton")) {
							if(cats[0].equals("Hong Kong") || cats[1].equals("Hong Kong")) {
								cat = "Canton";
							}
						}
					}
					if(cat.isEmpty()) continue;

					if (cat.equals(region) && film.getFilmType().equals(type)) {
						count++;
						totalFilms++;
						if(videos.contains(film.key)) {
							videoCount++;
							totalFrames += filmFrames.get(film.key);
						}
					}
				}
				System.out.println("Metadata: " + region + " " + type + " " + count);
				totals.add(count);
				System.out.println("Video: " + region + " " + type + " " + videoCount);
				videoTotals.add(videoCount);
				foundGenres.add(type);
			}
			
			int total = 0, videoTotal = 0;
			for(int i = 0; i < totals.size(); i++) {
				total += totals.get(i);
				videoTotal += videoTotals.get(i);
				System.out.println("Ratio: " + region + " " + foundGenres.get(i) + " " + String.format("%.2f", (double)videoTotals.get(i) / totals.get(i) * 100) + "%");
			}
			
			System.out.println("Region total ratio: " + region + " " + String.format("%.2f", (double)videoTotal / total * 100) + "%");
			System.out.println();

			System.out.println("Total frames: " + totalFrames);
			System.out.println("Total films: " + totalFilms);
		}
	}
}