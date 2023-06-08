package GIS.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import GIS.JAVA.GeographyMovement.Pos;
import OCR.JAVA.Film;

public class PrivateDebutTracking {
	private static int START = 1961, END = 1966;
	private static boolean EXCLUDE_MULTI_REGION_NODES = false;
	private static final String SOURCE = "Network/csv/nodes/nodes-";
	private static final String TARGET = "GIS/statistics/private_studio_tracking/pri_tracking-", GEO = "GIS/source/studios_geo_src.csv";

	private static class PriTrack {
		String name;
		String cat;
		String job;
		int debutYear;
		String debutRegion;

		public PriTrack(String name, String cat, String job, int debutYear, String debutRegion) {
			this.name = name;
			this.cat = cat;
			this.job = job;
			this.debutYear = debutYear;
			this.debutRegion = debutRegion;
		}
	}

	private static ArrayList<String> privateStudioMembers = new ArrayList<String>();

	public static void main(String[] args) throws IOException {
		//privateStudioMembers = getAllPrivateCompanyMembers();
		//countDebutFromPrivate("1961-1963");
		
		generateTrackingFile(1954, 1960);
	}

	static ArrayList<String> getAllPrivateCompanyMembers() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		ArrayList<String> people = new ArrayList<String>();

		for (Film film : films) {
			if(Arrays.asList(film.getCategory()).contains("Shanghai (private)")) {
				String[] peopleInFilm = film.getAllNamesArrayWithoutDuplication();
				for (String person : peopleInFilm) {
					if(!people.contains(person)) {
						people.add(person);
					}
				}
			}
		}
		System.out.println(people.size());
		return people;
	}

	static void countDebutFromPrivate(String tag) throws IOException {
		String[] tags = {"1954-1957", "1958-1960", "1961-1963", "1964-1966"};
		HashMap<String, HashMap<String, Integer>> tagRegCount = new HashMap<String, HashMap<String, Integer>>();
		ArrayList<String> categories = new ArrayList<String>();
		categories.add("Shanghai (private)");
		categories.add("Shanghai (state)");
		categories.add("Multi-region");
		categories.add("Northeast");
		categories.add("Beijing");
		categories.add("Canton");
		//categories.add("Misc");

		ArrayList<String> appeared = new ArrayList<String>();

		for(int i=0; i<tags.length; i++) {
			String source = SOURCE + tags[i] + ".csv";
			BufferedReader br = new BufferedReader(new FileReader(source));

			String line = br.readLine();

			HashMap<String, Integer> newRegCount = new HashMap<String, Integer>();

			while((line = br.readLine()) != null) {
				String[] split = line.split(",");
				String name = split[1].replaceAll("\"", "");
				String cat = split[2].replaceAll("\"", "");
				//String debutRegion = split[7];

				if(privateStudioMembers.contains(name)) {
					if(!appeared.contains(name)) {
						appeared.add(name);
					}
					if(cat.contains(" / ")) cat = "Multi-region";
					if(!categories.contains(cat)) {
						categories.add(cat);
					}
					if(newRegCount.containsKey(cat)) {
						newRegCount.put(cat, newRegCount.get(cat) + 1);
					} else {
						newRegCount.put(cat, 1);
					}
				}
			}

			br.close();

			tagRegCount.put(tags[i], newRegCount);
		}

		for (String string : privateStudioMembers) {
			if(!appeared.contains(string)) {
				//System.out.println(string + " missing");
			}
		}

		String target = "GIS/statistics/private_studio_members.csv";
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(new File(target)));
		bw.write("Years,Category,Count\n");
		for(int i=0; i<tags.length; i++) {
			for(int j=0; j<categories.size(); j++) {
				String cat = categories.get(j);
				if(tagRegCount.get(tags[i]).containsKey(cat)) {
					if(cat.equals("Shanghai (roc)")) cat = "Shanghai (ROC)";
					bw.write(tags[i] + "," + cat + "," + tagRegCount.get(tags[i]).get(cat) + "\n");
				} else {
					if(cat.equals("Shanghai (roc)")) cat = "Shanghai (ROC)";
					bw.write(tags[i] + "," + cat + ",0\n");
				}
			}
		}
		bw.close();
		/*bw.write("Years");
		for(int i=0; i<categories.size(); i++) {
			bw.write("," + categories.get(i));
		}
		bw.write(",Total");
		bw.newLine();

		for(int i=0; i<tags.length; i++) {
			bw.write(tags[i]);
			HashMap<String, Integer> newRegCount = tagRegCount.get(tags[i]);
			int total = 0;
			for(int j=0; j<categories.size(); j++) {
				String cat = categories.get(j);
				if(newRegCount.containsKey(cat)) {
					bw.write("," + newRegCount.get(cat));
					total += newRegCount.get(cat);
				} else {
					bw.write(",0");
				}
			}
			bw.write("," + total);
			bw.newLine();
		}

		bw.close();*/
	}

	static void generateTrackingFile(int start, int end) throws IOException {
		START = start;
		END = end;
		
		String source = SOURCE + (START == END ? START : (START + "-" + END)) + ".csv";
		BufferedReader br = new BufferedReader(new FileReader(source));

		String line = br.readLine();
		ArrayList<PriTrack> priTracks = new ArrayList<PriTrack>();

		while((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String name = split[1].replaceAll("\"", "");
			String cat = split[2].replaceAll("\"", "");
			String job = split[4].replaceAll("\"", "");
			int debutYear = Integer.parseInt(split[6]);
			String debutRegion = split[7];

			priTracks.add(new PriTrack(name, cat, job, debutYear, debutRegion));
		}

		br.close();

		HashMap<String, Pos> catTracks = new HashMap<String, Pos>();

		br = new BufferedReader(new FileReader(GEO));
		line = br.readLine();

		while((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String name = split[1];
			float lat = Float.parseFloat(split[2]);
			float lon = Float.parseFloat(split[3]);

			catTracks.put(name, new Pos(lat, lon));
		}

		br.close();

		//Write all nodes in PriTrack to file with format "name,cat,job,debutYear,debutRegion,lat,lon"
		GeographyMovement.initAllGeographicalPositions(false);
		String target = TARGET + (START == END ? START : (START + "-" + END)) + ".csv";
		if(EXCLUDE_MULTI_REGION_NODES) {
			String tag = START == END ? START + "" : (START + "-" + END);
			target = target.substring(0, target.lastIndexOf("/") + 1) + "single_region_track-" + tag + ".csv";
		}
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(new File(target)));
		bw.write("name,cat,job,debutYear,debutRegion,lat,lon\n");

		for (PriTrack priTrack : priTracks) {
			String name = priTrack.name;
			String cat = priTrack.cat;
			String job = priTrack.job;
			int debutYear = priTrack.debutYear;
			String debutRegion = priTrack.debutRegion;
			if(!debutRegion.contains("Shanghai (private)")) {
				continue;
			}
			String[] regions = cat.split(" / ");
			if(EXCLUDE_MULTI_REGION_NODES && regions.length > 1) {
				continue;
			}
			Pos wantedPos = MapPlot.GeoAppearance.estimateGeographicalPosition(regions);
			double lat = wantedPos.lat;
			double lon = wantedPos.lon;

			bw.write(name + "," + cat + "," + job + "," + debutYear + "," + debutRegion + "," + lat + "," + lon + "\n");
		}

		bw.close();
	}
		
}
