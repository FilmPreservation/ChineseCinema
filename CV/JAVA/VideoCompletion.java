package CV.JAVA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VideoCompletion {
	public static final boolean COUNT_MUSICAL_AS_FEATURE = true;

	public static final String SOURCE = "CV/video_collection.csv";
	public static final String META = "metadata.csv", EXTRA = "metadata-extra.csv";
	public static final String VIDEO_FILES = "../Temp";

	public static void main(String[] args) throws IOException {
		checkCompletion();
		//checkMissingFile();
	}

	public static void checkMissingFile() throws IOException {
		File source = new File(SOURCE);
		BufferedReader reader = new BufferedReader(new FileReader(source));
		reader.readLine();

		String line = null;
		ArrayList<String> titles = new ArrayList<String>();

		File dir = new File(VIDEO_FILES);
		File[] files = dir.listFiles();
		int missing = 0;

		int downloaded = 0;
		
		for (File file : files) {
			if(file.getName().endsWith(".mp4")) {
				String expectedTitle = file.getName().substring(0, file.getName().length() - 4);
				titles.add(expectedTitle);
			}
		}

		while((line = reader.readLine()) != null) {
			String[] values = line.split(",", -1);
			String title = values[1];
			title = title.replace("、", "");
			title = title.replace("“", "");
			title = title.replace("”", "");
			String downloadable = values[4];
			if(downloadable.equalsIgnoreCase("True") || downloadable.equalsIgnoreCase("False")) {
				boolean found = false;
				for (String string : titles) {
					if(string.equals(title)) {
						titles.remove(string);
						found = true;
						downloaded++;
						break;
					}
				}
				if(!found) {
					System.out.println("Missing entry: " + title);
					missing++;
				}
			}
		}

		for (String t : titles) {
			System.out.println("File " + t + ".mp4 cannot match with any enrty.");
		}

		reader.close();
		System.out.println("Downloaded " + downloaded + " out of " + (downloaded + missing) + " files.");
	}

	public static void checkCompletion() throws IOException {
		File source = new File(SOURCE);
		BufferedReader reader = new BufferedReader(new FileReader(source));
		reader.readLine();

		int collectedPrivate = 0, privateTotal = 0;
		int collectedFeature = 0, featureTotal = 0;
		int collectedState = 0, stateTotal = 0;
		int collectedStateFeature = 0, stateFeatureTotal = 0;
		int collectedDocumentary = 0, documentaryTotal = 0;
		int collectedPerformance = 0, performanceTotal = 0;
		int collectedOpera = 0, operaTotal = 0;

		HashMap<String, Integer> catFilm = new HashMap<String, Integer>();
		HashMap<String, Integer> catVideo = new HashMap<String, Integer>();

		int documentaryAvgReel = 0, featureAvgReel = 0;
		int reelAvailibleDocumentary = 0, reelAvailibleFeature = 0;
		
		String line = null;
		int video = 0, total = 0;
		
		//Read all lines from the file and get csv values
		while((line = reader.readLine()) != null) {
			String[] values = line.split(",", -1);
			String key = values[0];
			String category = values[3];
			if(!catFilm.containsKey(category)) catFilm.put(category, 1);
			else catFilm.put(category, catFilm.get(category) + 1);

			//This category is now not considered a major production category and should be skipped
			if(category.equals("Shanghai (roc)")) continue;

			//if(!isFeatureFilm(key)) System.out.println(getFilmType(key));
			//System.out.println(values[1] + " is Feature: " + isFeatureFilm(key) + ", is Private:" + isPrivateFilm(values[3]));
			total++;
			if(!values[4].isEmpty()) {
				video++;
				if(!catVideo.containsKey(category)) catVideo.put(category, 1);
				else catVideo.put(category, catVideo.get(category) + 1);
			}

			if(isFeatureFilm(key)) {
				featureTotal++;
				if(!values[4].isEmpty()) collectedFeature++;

				if(!isPrivateFilm(values[3])) {
					stateFeatureTotal++;
					if(!values[4].isEmpty()) collectedStateFeature++;
					else System.out.println("Undetected state feature: " + values[1]);
				}

				int r = getFilmReels(key);
				if(r > 0) {
					featureAvgReel += r;
					reelAvailibleFeature++;
				}
			}
			if(isPrivateFilm(values[3])) {
				privateTotal++;
				if(!values[4].isEmpty()) collectedPrivate++;
			} else {
				stateTotal++;
				if(!values[4].isEmpty()) collectedState++;
			}

			if(getFilmType(key).equals("Artistic Documentary")) {
				documentaryTotal++;
				if(!values[4].isEmpty()) collectedDocumentary++;
				int r = getFilmReels(key);
				if(r > 0) {
					documentaryAvgReel += r;
					reelAvailibleDocumentary++;
				}
			}

			if(getFilmType(key).equals("Performance")) {
				performanceTotal++;
				if(!values[4].isEmpty()) collectedPerformance++;
			}

			if(getFilmType(key).equals("Opera")) {
				operaTotal++;
				if(!values[4].isEmpty()) collectedOpera++;
			}
		}
		
		reader.close();
		
		System.out.println();
		for (String key : catFilm.keySet()) {
			System.out.println(key + ": " + catFilm.get(key));
		}

		System.out.println("\nWith video:");
		for (String key : catVideo.keySet()) {
			System.out.println(key + ": " + catVideo.get(key));
		}

		System.out.println();
		System.out.println("Collected " + video + " out of " + total + " targets.");
		System.out.println("Collected " + collectedPrivate + " out of " + privateTotal + " private films.");
		System.out.println("Collected " + collectedFeature + " out of " + featureTotal + " feature films.");
		System.out.println("Collected " + collectedState + " out of " + (stateTotal-4) + " state films (exclusive of 4 films produced by the Republic of China).");
		System.out.println("Collected " + collectedStateFeature + " out of " + stateFeatureTotal + " state feature films.");
		System.out.println("Collected " + collectedDocumentary + " out of " + documentaryTotal + " documentaries.");
		System.out.println("Collected " + collectedPerformance + " out of " + performanceTotal + " performances.");
		System.out.println("Collected " + collectedOpera + " out of " + operaTotal + " operas.");
		System.out.println("\nFeature film average reel length: " + (featureAvgReel / reelAvailibleFeature));
		System.out.println("Documentary average reel length: " + (documentaryAvgReel / reelAvailibleDocumentary));
	}

	private static boolean isFeatureFilm(String key) throws IOException {
		File mFile = new File(META);
		File eFile = new File(EXTRA);
		BufferedReader mReader = new BufferedReader(new FileReader(mFile));
		BufferedReader eReader = new BufferedReader(new FileReader(eFile));

		String line = null;
		while((line = mReader.readLine()) != null) {
			String[] values = line.split(",");
			String mKey = values[0];
			if(mKey.equals(key)) {
				String type = OCR.JAVA.postprocessing.FilmTyping.getFilmType(line, false);
				mReader.close();
				eReader.close();
				return COUNT_MUSICAL_AS_FEATURE ? (type.equals("Feature") || type.equals("Musical")) : type.equals("Feature");
			}
		}
		while((line = eReader.readLine()) != null) {
			String[] values = line.split(",");
			String eKey = values[0];
			if(eKey.equals(key)) {
				String type = OCR.JAVA.postprocessing.FilmTyping.getFilmType(line, true);
				mReader.close();
				eReader.close();
				return COUNT_MUSICAL_AS_FEATURE ? (type.equals("Feature") || type.equals("Musical")) : type.equals("Feature");
			}
		}
		mReader.close();
		eReader.close();
		throw new IOException("Cannot find film with key: " + key);
	}

	private static boolean isPrivateFilm(String category) {
		return category.equals("Shanghai (private)");
	}

	private static String getFilmType(String key) throws IOException {
		File mFile = new File(META);
		File eFile = new File(EXTRA);
		BufferedReader mReader = new BufferedReader(new FileReader(mFile));
		BufferedReader eReader = new BufferedReader(new FileReader(eFile));

		String line = null;
		while((line = mReader.readLine()) != null) {
			String[] values = line.split(",");
			String mKey = values[0];
			if(mKey.equals(key)) {
				String type = OCR.JAVA.postprocessing.FilmTyping.getFilmType(line, false);
				mReader.close();
				eReader.close();
				return type;
			}
		}
		while((line = eReader.readLine()) != null) {
			String[] values = line.split(",");
			String eKey = values[0];
			if(eKey.equals(key)) {
				String type = OCR.JAVA.postprocessing.FilmTyping.getFilmType(line, true);
				mReader.close();
				eReader.close();
				return type;
			}
		}
		mReader.close();
		eReader.close();
		throw new IOException("Cannot find film with key: " + key);
	}

	//Return -1 if not presented in data
	private static int getFilmReels(String key) throws IOException {
		File mFile = new File(META);
		File eFile = new File(EXTRA);
		BufferedReader mReader = new BufferedReader(new FileReader(mFile));
		BufferedReader eReader = new BufferedReader(new FileReader(eFile));

		String line = null;
		while((line = mReader.readLine()) != null) {
			String[] values = line.split(",");
			String mKey = values[0];
			if(mKey.equals(key)) {
				mReader.close();
				eReader.close();
				return Integer.parseInt(values[6]);
			}
		}
		while((line = eReader.readLine()) != null) {
			String[] values = line.split(",");
			String eKey = values[0];
			if(eKey.equals(key)) {
				mReader.close();
				eReader.close();
				return -1;
			}
		}
		mReader.close();
		eReader.close();
		throw new IOException("Cannot find film with key: " + key);
	}
	
}
