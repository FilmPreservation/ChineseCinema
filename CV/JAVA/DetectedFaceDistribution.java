package CV.JAVA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import OCR.JAVA.Film;

public class DetectedFaceDistribution {
	private final static String SRC = 
				"../Python/DeepFace/emotion_faces/openface_conf40_height20_reform.csv";
				//"../Python/DeepFace/face-height_min20.csv";

	private static String[] types = {"Feature", "Musical"};

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(SRC));
		String line = "";

		HashMap<String, Integer> map = new HashMap<String, Integer>();

		while((line = br.readLine()) != null) {
			String f = line.split(",")[0];
			Film film = VideoKey.videoToObject(VideoKey.videoToFilmKey(f));
			String[] regions = film.getCategory();
			String r = "";
			if(regions.length == 1) {
				r = regions[0];
			}else{
				r = "Canton";
			}

			if(!Arrays.asList(types).contains(film.getFilmType())) {
				continue;
			}

			if(map.containsKey(r)) {
				map.put(r, map.get(r) + 1);
			}else{
				map.put(r, 1);
			}
		}

		br.close();

		System.out.print("Distribution of Detected Faces (");
		for(int i = 0; i < types.length; i++) {
			System.out.print(types[i]);
			if(i != types.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.println("):");
		for(String key : map.keySet()) {
			System.out.println(key + " : " + map.get(key));
		}
	}
}