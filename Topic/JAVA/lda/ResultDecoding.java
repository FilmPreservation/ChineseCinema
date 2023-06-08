package Topic.JAVA.lda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

@SuppressWarnings("unused")
public class ResultDecoding {
	private static final String SRC = "Topic/results(main_only).txt";
	private static final String TAR = "Topic/results_decoded(main_only).csv",
			TAR_DELUXE = "Topic/results_full(main_only).csv";
	private static final String FILM_KEY_FLAG = "|||FILM:", TOPIC_CODE_FLAG = "|||TOPICS:[", TEXT_FLAG = "]|||TEXT:";

	public static void main(String[] args) {
		//decodeRetuls();
		//rankMostFrequentTopicsInRegion();
		writeDecodedResultsDeluxe();
		//rankMostFrequentTopicsInRegion("Shanghai (private)");
		//rankMostFrequentTopicsInRegion("Shanghai (state)");
		//rankMostFrequentTopicsInRegion("Northeast");
		//rankMostFrequentTopicsInRegion("Beijing");
		//rankMostFrequentTopicsInRegion("Xi'an");
		//rankMostFrequentTopicsInRegion("Canton");
	}

	private static void decodeRetuls() {
		try {
			ArrayList<String> decoded = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(SRC));
			String line = null;
			while ((line = br.readLine()) != null) {
				//If no flag found, throw RunTimeException
				if (line.indexOf(FILM_KEY_FLAG) == -1 || line.indexOf(TOPIC_CODE_FLAG) == -1 || line.indexOf(TEXT_FLAG) == -1) {
					br.close();
					throw new RuntimeException("No flag found in line: " + line);
				}

				String filmKey = line.substring(line.indexOf(FILM_KEY_FLAG) + FILM_KEY_FLAG.length(), line.indexOf(TOPIC_CODE_FLAG)).trim();
				System.out.println(filmKey);
				String topicCode = line.substring(line.indexOf(TOPIC_CODE_FLAG) + TOPIC_CODE_FLAG.length(), line.indexOf(TEXT_FLAG)).trim();

				String[] allTopics = topicCode.split("\\), \\(");
				HashMap<Integer, Double> topicMap = new HashMap<Integer, Double>();
				for (String string : allTopics) {
					if(string.startsWith("(")) {
						string = string.substring(1);
					}
					if(string.endsWith(")")) {
						string = string.substring(0, string.length() - 1);
					}
					int topic = Integer.parseInt(string.substring(0, string.indexOf(",")));
					double prob = Double.parseDouble(string.substring(string.indexOf(",") + 1).trim());
					topicMap.put(topic, prob);
				}
				Integer codeOfMostPossibleTopic = -1;
				double maxProb = -1;

				for (Integer topic : topicMap.keySet()) {
					if (topicMap.get(topic) > maxProb) {
						maxProb = topicMap.get(topic);
						codeOfMostPossibleTopic = topic;
					}
				}

				//System.out.println(topicCode);
				//int topic = Integer.parseInt(topicCode);
				decoded.add(filmKey + "," + codeOfMostPossibleTopic);
			}
			br.close();

			//Write to file
			BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(TAR));
			bw.write("Film Key,Topic\n");
			for (String s : decoded) {
				bw.write(s);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeDecodedResultsDeluxe() {
		try {
			HashMap<String, Integer> filmTopicMap = new HashMap<String, Integer>();
			BufferedReader br = new BufferedReader(new FileReader(TAR));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String filmKey = line.substring(0, line.indexOf(","));
				int topic = Integer.parseInt(line.substring(line.indexOf(",") + 1));
				if (!filmTopicMap.containsKey(filmKey)) {
					filmTopicMap.put(filmKey, topic);
				} else {
					br.close();
					throw new RuntimeException("Film key duplicated: " + filmKey);
				}
			}
			br.close();

			ArrayList<Film> films;

			if(TAR.contains("(main_only)")) {
				films = Film.initAllFilmsInMainMetadata();
			}else if(TAR.contains("(extra_only)")) {
				films = Film.initAllFilmsInExtraMetadata();
			}else{
				films = Film.initAllFilms();
			}
			
			ArrayList<String> output = new ArrayList<String>();
			for (Film film : films) {
				if(!filmTopicMap.containsKey(film.key)) {
					throw new RuntimeException("Film key not found in results: " + film.key);
				}

				int topic = filmTopicMap.get(film.key);
				String production = film.productionToString();
				String title = film.title;
				String key = film.key;
				String plot = film.plot;

				output.add(key + "," + title + "," + film.year + "," + production + "," + film.getFilmType() + "," + topic + "," + plot);
			}

			//Write to file
			BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(TAR_DELUXE));
			bw.write("Key,Film Title,Year,Production,Type,Topic,Plot Summary\n");
			for (String s : output) {
				bw.write(s);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void rankMostFrequentTopicsInRegion() {
		//Use a BufferedReader to read the results_decoded.csv and load film keys into a HashMap
		try {
			HashMap<String, Integer> filmTopicMap = new HashMap<String, Integer>();
			BufferedReader br = new BufferedReader(new FileReader(TAR));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String filmKey = line.substring(0, line.indexOf(","));
				int topic = Integer.parseInt(line.substring(line.indexOf(",") + 1));
				if (!filmTopicMap.containsKey(filmKey)) {
					filmTopicMap.put(filmKey, topic);
				} else {
					br.close();
					throw new RuntimeException("Film key duplicated: " + filmKey);
				}
			}
			br.close();

			ArrayList<Film> films = Film.initAllFilms();
			HashMap<String, Integer> regionTopicCount = new HashMap<String, Integer>();
			for (Film film : films) {
				if(!filmTopicMap.containsKey(film.key)) {
					throw new RuntimeException("Film key not found in results: " + film.key);
				}

				int topic = filmTopicMap.get(film.key);
				String[] region = film.getCategory();

				for (String r : region) {
					String key = r + "," + topic;

					if (!regionTopicCount.containsKey(key)) {
						regionTopicCount.put(key, 1);
					} else {
						regionTopicCount.put(key, regionTopicCount.get(key) + 1);
					}
				}
			}

			//Write to file
			BufferedWriter bw = new BufferedWriter(new java.io.FileWriter("Topic/regions/region_topic_count(all).csv"));
			bw.write("Region,Topic,Count\n");
			for (String key : regionTopicCount.keySet()) {
				bw.write(key + "," + regionTopicCount.get(key));
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void rankMostFrequentTopicsInRegion(String region) {
		//Use a BufferedReader to read the results_decoded.csv and load film keys into a HashMap
		try {
			HashMap<String, Integer> filmTopicMap = new HashMap<String, Integer>();
			BufferedReader br = new BufferedReader(new FileReader(TAR));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String filmKey = line.substring(0, line.indexOf(","));
				int topic = Integer.parseInt(line.substring(line.indexOf(",") + 1));
				if (!filmTopicMap.containsKey(filmKey)) {
					filmTopicMap.put(filmKey, topic);
				} else {
					br.close();
					throw new RuntimeException("Film key duplicated: " + filmKey);
				}
			}
			br.close();

			ArrayList<Film> films = Film.initAllFilms();
			HashMap<String, Integer> yearTopicCount = new HashMap<String, Integer>();
			for (Film film : films) {
				if(!filmTopicMap.containsKey(film.key)) {
					throw new RuntimeException("Film key not found in results: " + film.key);
				}

				int topic = filmTopicMap.get(film.key);
				String[] regions = film.getCategory();

				for (String r : regions) {
					if(!r.equals(region)) {
						continue;
					}

					String key = film.year + "," + topic;

					if (!yearTopicCount.containsKey(key)) {
						yearTopicCount.put(key, 1);
					} else {
						yearTopicCount.put(key, yearTopicCount.get(key) + 1);
					}
				}
			}

			//Write to file
			BufferedWriter bw = new BufferedWriter(new java.io.FileWriter("Topic/region_topic_count(" + region + ").csv"));
			bw.write("Year,Topic,Count\n");
			for (String key : yearTopicCount.keySet()) {
				bw.write(key + "," + yearTopicCount.get(key));
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
