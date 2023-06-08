package Topic.JAVA.lda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

@SuppressWarnings("unused")
public class SimpleWeightConverter {
	public static void main(String[] args) {
		//convertWeights();
		getRegionProbabilities();
	}

	private static void convertWeights() {
		try {
			File file = new File("Topic/JAVA/lda/topics_weighted.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int expectedIndex = 0;

			while ((line = reader.readLine()) != null) {
				if(line.startsWith(" (") && line.endsWith(",")) {
					int index = Integer.parseInt(line.substring(2, line.indexOf(",")));
					if(index != expectedIndex) {
						reader.close();
						throw new RuntimeException("Error: index " + index + " is not expected " + expectedIndex);
					}
					expectedIndex++;

					String[] aspects = new String[4];
					for(int i=0; i<4; i++) {
						line = reader.readLine();
						aspects[i] = line.substring(line.indexOf("\'") + 1, line.lastIndexOf("\'"));
					}

					System.out.println("Topic " + index);

					int count = 0;
					for (String string : aspects) {
						String[] words = string.split("\\+");
						for (String word : words) {
							word = word.trim();
							if(word.length() > 0) {
								count++;
								String keyword = word.substring(word.indexOf("\"") + 1,word.lastIndexOf("\""));
								double weight = Double.parseDouble(word.substring(0, word.indexOf("*")));
								System.out.println(keyword + "," + weight);
							}
						}
					}

					if(count != 20) {
						reader.close();
						throw new RuntimeException("Error: count " + count + " is not expected 20");
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getRegionProbabilities() {
		try {
			File file = new File("Topic/results_with_probabilities.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;

			String filmFlag = "|||FILM:", topicFlag = "|||TOPICS:[", textFlag = "]|||TEXT:";
			ArrayList<Film> films = Film.initAllFilms();
			HashMap<String, Film> filmMap = new HashMap<String, Film>();
			for (Film film : films) {
				filmMap.put(film.key, film);
			}

			//String: REGION-TOPIC => Double: Probability
			HashMap<String, Double> regionProbabilities = new HashMap<String, Double>();

			while ((line = reader.readLine()) != null) {
				if(line.startsWith(filmFlag)) {
					int index = line.indexOf(filmFlag) + filmFlag.length();
					int end = line.indexOf(topicFlag);
					String filmKey = line.substring(index, end).trim();
					Film film = filmMap.get(filmKey);
					String[] regions = film.getCategory();
					index = line.indexOf(topicFlag) + topicFlag.length();
					end = line.indexOf(textFlag);
					String[] topics = line.substring(index, end).trim().split("\\), \\(");

					ArrayList<String> topicProbs = new ArrayList<String>();
					for (String topic : topics) {
						topic = topic.trim();
						if(topic.startsWith("(")) {
							topic = topic.substring(1);
						}
						if(topic.endsWith(")")) {
							topic = topic.substring(0, topic.length() - 1);
						}
						topicProbs.add(topic);
					}

					for(String r : regions) {
						for(String tp : topicProbs) {
							String[] tpArray = tp.split(", ");
							String topic = tpArray[0];
							double prob = Double.parseDouble(tpArray[1]);
							String key = r + "-" + topic;
							if(regionProbabilities.containsKey(key)) {
								double oldProb = regionProbabilities.get(key);
								regionProbabilities.put(key, oldProb + prob);
							} else {
								regionProbabilities.put(key, prob);
							}
						}
					}
				}
			}
			reader.close();

			//Write results in file
			file = new File("Topic/regions/region_probabilities.csv");
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(file));
			writer.write("Region,Topic,Probability\n");
			for(String key : regionProbabilities.keySet()) {
				double prob = regionProbabilities.get(key);
				String region = key.substring(0, key.indexOf("-"));
				String topic = key.substring(key.indexOf("-") + 1);
				writer.write(region + "," + topic + "," + prob + "\n");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
