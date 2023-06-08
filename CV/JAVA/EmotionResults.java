package CV.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.io.IOException;

import OCR.JAVA.Film;

public class EmotionResults {
	private static String DIR = "../Python/";
	private static String[] SUB_DIRS = {"possible_smile", "laughter", "intensive_laughter"};
	private static String output = "CV/emotion_results.csv";

	private static boolean CHN_ONLY = false;

	private static String[] Soviet = {"A Dream of Cossack", "Brave People", "Carnival Night", "Pavel Korchagin", "Prologue", "The District Secretary"};
	private static String[] Hollywood = {"Casablanca", "Citizen Kane", "Grapes of Wrath", "Great Expectations", "Notorious", "The Wizard of OZ"};

	public static void main(String args[]) throws IOException {
		if(CHN_ONLY) {
			output = "CV/emotion_results_chn.csv";
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(output, false));
		bw.write("film,frame,pos,year,category,studio,type,intensity,level\n");
		bw.close();

		for (String string : SUB_DIRS) {
			String dir = DIR + string + "/";
			File src = new File(dir + "/query.csv"), src2 = new File(dir + "/query-nonchn.csv");
			File frs = new File("CV/crowd/processed_videos.csv");

			HashMap<String, Integer> totalFrames = new HashMap<String, Integer>();
			BufferedReader br = new BufferedReader(new FileReader(frs));
			String line = br.readLine();
			while((line = br.readLine()) != null) {
				String[] data = line.split(",");
				String name = data[0];
				int fr = Integer.parseInt(data[1]);
				totalFrames.put(name, fr);
			}
			br.close();

			System.out.println("OIioashdioas");

			br = new BufferedReader(new FileReader(src));
			bw = new BufferedWriter(new FileWriter(output, true));
			//bw.write("film,frame,pos,year,category,studio,type,intensity,level\n");
			line = br.readLine();
			while((line = br.readLine()) != null) {
				String[] data = line.split(",");
				String name = data[0];
				int frame = Integer.parseInt(data[1]);
				float pos = frame / (float) totalFrames.get(name);
				Film film = VideoKey.videoToObject(VideoKey.videoToFilmKey(name));
				int year = film.year;
				String cat = VideoKey.metaCategoryToVideoCategory(film.getCategory());
				String studio = film.productionToString();
				String type = film.getFilmType();
				
				//intensity = parsefloat("AU12_r=" to " and ")
				float intensity = Float.parseFloat(data[2].substring(7, data[2].indexOf(" and ")));
				int level = intensity > 3.0f ? 2 : intensity > 2.0f ? 1 : 0;

				bw.write(name + "," + frame + "," + pos + "," + year + "," + cat + "," + studio + "," + type + "," + intensity + "," + level + "\n");
			}
			br.close();
			bw.close();

			br = new BufferedReader(new FileReader(src2));
			bw = new BufferedWriter(new FileWriter(output, true));
			line = br.readLine();
			while((line = br.readLine()) != null) {
				String[] data = line.split(",");
				String name = data[0];
				int frame = Integer.parseInt(data[1]);
				float pos = frame / (float) totalFrames.get(name);
				Film film = VideoKey.videoToObject(VideoKey.videoToFilmKey(name));

				int year = 0;
				String cat = "";
				String studio = "";
				String type = "Feature";

				if(film != null) {
					year = film.year;
					cat = VideoKey.metaCategoryToVideoCategory(film.getCategory());
					studio = film.productionToString();
					type = film.getFilmType();
				}else{
					if(CHN_ONLY) continue;
					if(Arrays.asList(Hollywood).contains(name)) {
						cat = "Hollywood";
					}else if(Arrays.asList(Soviet).contains(name)){
						cat = "Soviet";
					}else{
						bw.close();
						br.close();
						throw new RuntimeException("Film not found: " + name);
					}
				}

				//intensity = parsefloat("AU12_r=" to " and ")
				float intensity = Float.parseFloat(data[2].substring(7, data[2].indexOf(" and ")));
				int level = intensity > 3.0f ? 2 : intensity > 2.0f ? 1 : 0;

				bw.write(name + "," + frame + "," + pos + "," + year + "," + cat + "," + studio + "," + type + "," + intensity + "," + level + "\n");
			}
			br.close();
			bw.close();
		}
	}
}
