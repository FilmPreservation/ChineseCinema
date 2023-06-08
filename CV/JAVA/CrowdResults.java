package CV.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

public class CrowdResults {
	private static final String PERSON_BOXES = "CV/crowd/boxes-Soviet.csv",
			HEAD_BOXES = "CV/crowd/head_boxes-Soviet.csv",
			FOUND_FRAMES = "CV/crowd/found_frames-Soviet.csv",
			FRAME_COUNTS = "CV/crowd/processed_videos.csv";
	private static boolean NON_CHN = true; //The metadata only contains Chinese films, so some auto-generated info must be applied to non-Chinese films
	private static String filerType = "Feature"; //Set a type to filter films, otherwise keep all
	private static final boolean RUN_ALL_TYPES = false; //Set to true to run all types, otherwise only run the type above
	private static final boolean MAXIMUM_ONLY = true; //Set to true to only run the maximum number of heads per film

	int numPeople, numHeads;
	public CrowdResults() {
		numPeople = 0;
		numHeads = 0;
	}

	public static void main(String args[]) throws IOException {
		if(RUN_ALL_TYPES) {
			String[] types = {"Feature", "Musical", "Performance", "Artistic Documentary", "Opera"};
			for(String type : types) {
				filerType = type;
				processCrowdBoxes();
			}
		} else {
			processCrowdBoxes();
		}
	}

	private static void processCrowdBoxes() throws IOException {
		HashMap<String, Integer> personBoxes = new HashMap<String, Integer>();
		HashMap<String, Integer> headBoxes = new HashMap<String, Integer>();
		HashMap<String, Integer> filmMaxHeads = new HashMap<String, Integer>();

		// Read in person boxes
		FileReader fr = new FileReader(new File(PERSON_BOXES));
		BufferedReader br = new BufferedReader(fr);

		ArrayList<String> keys = new ArrayList<String>();

		String line = br.readLine();
		int i = 0;
		while ((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String film = split[0];
			String frame = split[1];
			String key = film + "," + frame;
			if(!personBoxes.containsKey(key)) {
				personBoxes.put(key, 1);
			} else {
				personBoxes.put(key, personBoxes.get(key) + 1);
			}
			i++;
			System.out.println(i);
		}

		fr.close();
		br.close();

		// Read in head boxes
		fr = new FileReader(new File(HEAD_BOXES));
		br = new BufferedReader(fr);

		line = br.readLine();
		i = 0;
		while ((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String film = split[0];
			String frame = split[1];
			String key = film + "," + frame;
			if(!headBoxes.containsKey(key)) {
				headBoxes.put(key, 1);
			} else {
				headBoxes.put(key, headBoxes.get(key) + 1);
			}
			i++;
			System.out.println(i);

			int tot = headBoxes.get(key);
			if(!filmMaxHeads.containsKey(film)) {
				filmMaxHeads.put(film, tot);
			} else {
				if(tot > filmMaxHeads.get(film))
					filmMaxHeads.put(film, tot);
			}
		}

		fr.close();
		br.close();

		// Read in found frames
		fr = new FileReader(new File(FOUND_FRAMES));
		br = new BufferedReader(fr);

		line = br.readLine();
		i = 0;
		while ((line = br.readLine()) != null) {
			String key = line.substring(0, line.lastIndexOf(","));
			if(!keys.contains(key)) {
				keys.add(key);
			}
			i++;
			System.out.println(i);
		}

		HashMap<String, Film> filmTitleToObj = new HashMap<String, Film>();
		ArrayList<Film> films = Film.initAllFilms();
		for(Film film : films) {
			String title = film.title;
			String key = film.key;
			String fKey = "";
			if(title.equals("母亲"))
				fKey = "mu qin"; //Wanted key
				if(key.equals(fKey)) //Ignore duplicates and add wanted
				filmTitleToObj.put(title, film);
			else if(key.equals("欢天喜地"))
				fKey = "huan tian xi di (1959)";
				if(key.equals(fKey))
				filmTitleToObj.put(title, film);
			else if(key.equals("春雷"))
				fKey = "chun lei (1958)";
				if(key.equals(fKey))
				filmTitleToObj.put(title, film);
			else if(key.equals("穆桂英挂帅"))
				fKey = "mu gui ying gua shuai (1958)";
				if(key.equals(fKey))
				filmTitleToObj.put(title, film);
			else if(title.equals("大李、小李和老李"))
				filmTitleToObj.put("大李小李和老李", film);
			else if(title.equals("“特快”列车"))
			filmTitleToObj.put("特快列车", film);
			else
				filmTitleToObj.put(title, film);
		}

		HashMap<String, Integer> filmToFrameCount = new HashMap<String, Integer>();
		fr = new FileReader(new File(FRAME_COUNTS));
		br = new BufferedReader(fr);

		line = br.readLine();
		i = 0;
		int allFrames = 0;
		while ((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String film = split[0];
			int count = Integer.parseInt(split[1]);
			allFrames += count;
			filmToFrameCount.put(film, count);
			i++;
			System.out.println(i);
		}

		// Write results to an integrated file
		String output = "CV/crowd_results-Soviet.csv";
		if(!filerType.isBlank())
			output = "CV/crowd/crowd_results-" + filerType + ".csv";
		if(MAXIMUM_ONLY)
			output = output.replace(".csv", "-max.csv");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		bw.write("film,frame,pos,num_people,num_heads,geo_category,production,year,type,n_heads\n");
		for(String key : keys) {
			String[] split = key.split(",");
			String film = split[0];
			Film filmObj = filmTitleToObj.get(film);
			int frame = Integer.parseInt(split[1]);
			int total = filmToFrameCount.get(film);
			double pos = (double)frame / (double)total;
			if(pos > 1.0000) System.out.println("Found a frame with pos > 1: " + key);

			String cat;
			String studio;
			int year;
			String type;

			if(NON_CHN) {
				cat = "Non-Chinese";
				studio = "Non-Chinese";
				year = 1950;
				type = "Feature";
			}else{
				String[] cats = filmObj.getCategory();
				cat = "";
				if(cats.length > 1) {
					boolean hkct = false;
					if(cats[0].equals("Canton") && cats[1].equals("Hong Kong"))
						hkct = true;
					else if (cats[0].equals("Hong Kong") && cats[1].equals("Canton"))
						hkct = true;
					if(!hkct)
						System.out.println("Found an unexpected multi-region film: " + film);
					else {
						cat = "Canton";
					}
				}else{
					cat = cats[0];
				}
				studio = filmObj.productionToString();
				year = filmObj.year;
				type = filmObj.getFilmType();
			}

			if(!filerType.isBlank())
				if(!filerType.equals(type))
					continue;
			
			int numPeople = 0;
			if(personBoxes.containsKey(key))
				numPeople = personBoxes.get(key);
			else
				System.out.println("Found an filtered frame without people: " + key);

			int numHeads = 0;
			if(headBoxes.containsKey(key))
				numHeads = headBoxes.get(key);
			else
				System.out.println("Found an filtered frame without heads: " + key);
			int maxHeads = filmMaxHeads.get(film);
			double normHeads = 0.0;
			if(maxHeads > 0.001) {
				normHeads = (double)numHeads / (double)maxHeads;
			}
			if(MAXIMUM_ONLY) {
				if(numHeads != maxHeads)
					continue;
			}
			bw.write(key + "," + pos + "," + numPeople + "," + numHeads + "," + cat + "," + studio + "," + year + "," + type + "," + normHeads + "\n");
		}
		bw.close();
		System.out.println("All frames processed: " + allFrames);
	}
}