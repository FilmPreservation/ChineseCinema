package CV.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import OCR.JAVA.Film;

@SuppressWarnings("unused")
public class EmotionNarrative {
	static String src = "CV/emotion_results.csv";
	private static float step = 0.1f;

	//private static String[] regions = {"Beijing", "Northeast", "Shanghai (state)"};
	//private static String[] regions = {"Canton", "Xi'an"};
	private static String[] regions = {"Soviet", "Hollywood"};
	
	private static String[] non_chn = {"A Dream of Cossack", "Brave People", "Carnival Night", "Pavel Korchagin", "Prologue", "The District Secretary",
	"Casablanca", "Citizen Kane", "Grapes of Wrath", "Great Expectations", "Notorious", "The Wizard of OZ"};

	private static class EmotionResult {
		String film;
		int frame;
		float pos;
		int year;
		String category;
		String studio;
		String type;
		float intensity;
		int level;

		public EmotionResult(String[] data) {
			film = data[0];
			frame = Integer.parseInt(data[1]);
			pos = Float.parseFloat(data[2]);
			year = Integer.parseInt(data[3]);
			category = data[4];
			studio = data[5];
			type = data[6];
			intensity = Float.parseFloat(data[7]);
			level = Integer.parseInt(data[8]);
		}
	}
	
	public static void main(String[] args) throws IOException {
		ArrayList<String> videos = VideoKey.allVideoFiles();
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter("CV/emotion/narrative.csv"));
		bw.write("film,title,chunk_st,chunk_ed,chunk_cen,intensity_sum,level_sum,intensity_avg,intensity_avg,year,category,type,studio\n");

		for(String v : videos) {
			BufferedReader br = new BufferedReader(new java.io.FileReader(src));
			String line = br.readLine();

			ArrayList<EmotionResult> results = new ArrayList<EmotionResult>();
			Film f = VideoKey.videoToObject(VideoKey.videoToFilmKey(v));

			if(!Arrays.asList(regions).contains(VideoKey.metaCategoryToVideoCategory(f.getCategory()))) {
				continue;
			}
			
			while((line = br.readLine()) != null) {
				String[] data = line.split(",");
				String film = data[0];
				if(!film.equals(v)) {
					continue;
				}

				results.add(new EmotionResult(data));
			}

			br.close();

			System.out.println("Film: " + v);
			int chunks = (int)(1.0f / step);

			for(int i = 0; i < chunks; i++) {
				float pos = i * step;
				float centre = pos + step / 2;
				int count = 0;
				float intensity = 0;
				int level = 0;

				for(EmotionResult r : results) {
					if(r.pos >= pos && r.pos < pos + step) {
						count++;
						intensity += r.intensity;
						level += r.level;
					}
				}

				float in_avg = count > 0 ? intensity / count : 0;
				float lv_avg = count > 0 ? (float)level / count : 0;

				bw.write(f.key + "," + f.title + "," + pos + "," + (pos + step) + "," + centre + "," + intensity + "," + level + "," + in_avg + "," + lv_avg + "," + f.year + "," + VideoKey.metaCategoryToVideoCategory(f.getCategory()) + "," + f.getFilmType() + "," + f.productionToString() + "\n");
			}
			
			System.out.println();
		}

		for(String v : non_chn) {
			BufferedReader br = new BufferedReader(new java.io.FileReader(src));
			String line = br.readLine();

			ArrayList<EmotionResult> results = new ArrayList<EmotionResult>();
			String region = "";
			
			while((line = br.readLine()) != null) {
				String[] data = line.split(",");
				String film = data[0];
				if(!film.equals(v)) {
					continue;
				}

				results.add(new EmotionResult(data));
				region = data[4];
			}

			br.close();

			if(!Arrays.asList(regions).contains(region)) {
				continue;
			}

			System.out.println("Film: " + v);
			int chunks = (int)(1.0f / step);

			for(int i = 0; i < chunks; i++) {
				float pos = i * step;
				float centre = pos + step / 2;
				int count = 0;
				float intensity = 0;
				int level = 0;

				for(EmotionResult r : results) {
					if(r.pos >= pos && r.pos < pos + step) {
						count++;
						intensity += r.intensity;
						level += r.level;
					}
				}

				float in_avg = count > 0 ? intensity / count : 0;
				float lv_avg = count > 0 ? (float)level / count : 0;

				Film f = new Film(v, v);
				bw.write(f.key + "," + f.title + "," + pos + "," + (pos + step) + "," + centre + "," + intensity + "," + level + "," + in_avg + "," + lv_avg + "," + 0 + "," + region + ",Feature,Non_CHN\n");
			}
			
			System.out.println();
		}

		bw.close();
	}
}
