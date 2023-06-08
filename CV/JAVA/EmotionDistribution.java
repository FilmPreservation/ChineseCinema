package CV.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import OCR.JAVA.Film;

public class EmotionDistribution {
	private static String src = "CV/emotion_results.csv";
	private static String[] region = {"Shanghai (state)", "Beijing", "Northeast", "Canton", "Xi'an", "Soviet", "Hollywood"},
		type = {"Feature", "Musical"};

	private static float step = 1.0f;
	
	public static void main(String[] args) throws IOException {
		HashMap<String, Integer> regionVideoTotal = getRegionVideoTotal();
		BufferedWriter bw = new BufferedWriter(null, 0)

		for (String rg : region) {
			//boolean non_chn = rg.equals("Soviet") || rg.equals("Hollywood");

			BufferedReader br = new BufferedReader(new FileReader(src));
			String line = br.readLine();

			int regionLevel = 0, regionFrame = 0, regionFilm = regionVideoTotal.get(rg);
			float regionIntensity = 0;

			while((line = br.readLine()) != null) {
				String[] data = line.split(",");
				String cat = data[4];
				if(!cat.equals(rg)) {
					continue;
				}

				float intensity = Float.parseFloat(data[7]);
				float level = Float.parseFloat(data[8]);

				regionIntensity += intensity;
				regionLevel += level;

				regionFrame++;
			}

			br.close();

			System.out.println("Region: " + rg);
			System.out.println("Region average intensity: " + regionIntensity / regionFrame);
			System.out.println("Region average level: " + regionLevel / regionFrame);
			System.out.println("Region films: " + regionFilm);
			System.out.println();
		}		
	}

	private static HashMap<String, Integer> getRegionVideoTotal() throws IOException {
		ArrayList<String> videos = VideoKey.allVideoKeys();
		HashMap<String, Integer> videoCompletion = new HashMap<String, Integer>();
		
		for(String v : videos) {
			Film f = VideoKey.videoToObject(v);
			String reg = VideoKey.metaCategoryToVideoCategory(f.getCategory());
			String id = reg;
			if(videoCompletion.containsKey(id)) {
				videoCompletion.put(id, videoCompletion.get(id) + 1);
			} else {
				videoCompletion.put(id, 1);
			}
		}

		videoCompletion.put("Soviet", 6);
		videoCompletion.put("Hollywood", 6);

		return videoCompletion;
	}
}
