package OCR.JAVA.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import OCR.JAVA.Film;

public class RankActors {

	public static void main(String[] args) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> actors = new HashMap<String, Integer>();
		HashMap<String, String> isFromPrivate = new HashMap<String, String>();
		int allActors = 0;
		ArrayList<String> top5PercentActors = new ArrayList<String>();

		File nodes = new File("Network/csv/nodes/nodes-all.csv");
		BufferedReader reader = new BufferedReader(new java.io.FileReader(nodes));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] split = line.split(",");
			isFromPrivate.put(split[1].replaceAll("\"", ""), split[7].concat("," + split[8]).replaceAll("\"", ""));
		}
		reader.close();

		for (Film film : films) {
			for (String actor : film.getActingNameArray()) {
				if (actors.containsKey(actor)) {
					actors.put(actor, actors.get(actor) + 1);
				} else {
					actors.put(actor, 1);
				}
			}
		}

		// Write to file
		File file = new File("CV/JAVA/actors_rank.csv");
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(file));
		File file2 = new File("CV/JAVA/actors_appearance_average_by_region.csv");
		BufferedWriter writer2 = new BufferedWriter(new java.io.FileWriter(file2));

		int privateAvg = 0, privateCount = 0;
		int stateAvg = 0, stateCount = 0;
		HashMap<String, Integer> regionAverage = new HashMap<String, Integer>();
		HashMap<String, Integer> regionCount = new HashMap<String, Integer>();

		writer.write("Actor,Appearance,Debut,From Private\n");
		writer2.write("Region,Average Appearance of Actors Debuting from This Region,Ratio of Apperance of Top 5% Actors to all Actors in Films from This Region\n");
		
		for (String actor : actors.keySet()) {
			int count = actors.get(actor);

			if(isFromPrivate.containsKey(actor)){
				//This is not the name of an organization
				allActors++;

				boolean fromPrivate = isFromPrivate.get(actor).split(",")[1].equalsIgnoreCase("True");
				String[] regions = isFromPrivate.get(actor).split(",")[0].split(" / ");
				if(Arrays.asList(regions).contains("Shanghai (roc)")) fromPrivate = true;
				
				if(fromPrivate){
					privateAvg += count;
					privateCount++;
				} else {
					stateAvg += count;
					stateCount++;
				}
 
				//Count multiple regions separately in average
				for(String region : regions) {
					if(regionAverage.containsKey(region)){
						regionAverage.put(region, regionAverage.get(region) + count);
						regionCount.put(region, regionCount.get(region) + 1);
					} else {
						regionAverage.put(region, count);
						regionCount.put(region, 1);
					}
				}
			}

			//Ignore actors with less than 6 appearances in the list (but not in calculating average appearance) to keep the 5% top actors only
			if(count < 6) continue;
			top5PercentActors.add(actor);

			if(isFromPrivate.containsKey(actor)){
				writer.write(actor + "," + count + "," + isFromPrivate.get(actor) + "\n");
			} else {
				writer.write(actor + "," + count + ",Unknown,Unknown\n");
			}
		}
		writer.close();

		HashMap<String, Double> regionRatios = new HashMap<String, Double>();
		double privateAllRatios = 0.0, stateAllRatios = 0.0;
		int privateFilmsCount = 0, stateFilmsCount = 0;

		//Calculate the average ratio between appearance of top 5% actors and that of all actors in films from each region
		for(Film film : films) {
			String[] regions = film.getCategory();

			//Skip films with no actor information
			if(film.getActingNameArray().length == 0) continue;

			int top5 = 0;
			int all = 0;

			for(String actor : film.getActingNameArray()) {
				if(top5PercentActors.contains(actor)) top5++;
				all++;
			}
			double ratio = (double)top5/(double)all;

			for (String region : regions) {
				if(regionRatios.containsKey(region)){
					double oldRatio = regionRatios.get(region);
					regionRatios.put(region, (oldRatio + ratio) / 2.0);
				} else {
					regionRatios.put(region, ratio);
				}
			}

			boolean fromPrivate = false;
			if(Arrays.asList(regions).contains("Shanghai (roc)") || Arrays.asList(regions).contains("Shanghai (private)"))
				fromPrivate = true;
			
			if(fromPrivate){
				privateAllRatios += ratio;
				privateFilmsCount++;
			} else {
				stateAllRatios += ratio;
				stateFilmsCount++;
			}
		}

		double avg = (double)privateAvg/(double)privateCount;
		double avg2 = (double)privateAllRatios/(double)privateFilmsCount;
		System.out.println("All actors: " + allActors + "\n");
		System.out.println("Private or RoC averge frequency: " + avg);
		writer2.write("Private or RoC Studios Total," + avg + "," + avg2 + "\n");

		avg = (double)stateAvg/(double)stateCount;
		avg2 = (double)stateAllRatios/(double)stateFilmsCount;
		System.out.println("State average frequency: " + avg);
		writer2.write("State-owned Studios Total," + avg + "," + avg2 + "\n");

		System.out.println("\nRegion average frequencies: ");
		for(String region : regionAverage.keySet()){
			avg = (double)regionAverage.get(region)/(double)regionCount.get(region);
			avg2 = regionRatios.get(region);
			System.out.println(region + ": " + avg);
			writer2.write(region + "," + avg + "," + avg2 + "\n");
		}
		writer2.close();
	}

}