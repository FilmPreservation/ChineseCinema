package CV.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.FileWriter;

import OCR.JAVA.Film;

public class CrowdDistribution {
	private static String src = "CV/crowd_results.csv", s_src = "CV/crowd_results-Soviet.csv", h_src = "CV/crowd_results-Hollywood.csv";
	private static String[] region = {"Shanghai (state)", "Beijing", "Northeast", "Canton", "Xi'an", "Soviet", "Hollywood"},
		type = {"Feature", "Musical"};
	private static int step = 50;

	private static String out = "CV/crowd/years.csv";

	//private static String[] Soviet = {"A Dream of Cossack", "Brave People", "Carnival Night", "Pavel Korchagin", "Prologue", "The District Secretary"};
	//private static String[] Hollywood = {"Casablanca", "Citizen Kane", "Grapes of Wrath", "Great Expectations", "Notorious", "The Wizard of OZ"};
	
	public static void main(String[] args) throws IOException {
		HashMap<String, Integer> videoCompletion = new HashMap<String, Integer>();
		ArrayList<String> videos = VideoKey.allVideoKeys();
		HashMap<String, Integer> regionYearVideoTotal = new HashMap<String, Integer>();

		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.write("Region,Year,Count,Sum,AvgSize\n");
		bw.close();

		BufferedWriter bw2 = new BufferedWriter(new FileWriter("CV/crowd/counts.csv"));
		bw2.write("Region,Segment,Frames,Heads,AvgSize\n");
		bw2.close();

		for(String v : videos) {
			Film f = VideoKey.videoToObject(v);
			String reg = VideoKey.metaCategoryToVideoCategory(f.getCategory());
			String type = f.getFilmType();
			String id = reg + "," + type;
			if(videoCompletion.containsKey(id)) {
				videoCompletion.put(id, videoCompletion.get(id) + 1);
			} else {
				videoCompletion.put(id, 1);
			}
			String ryId = reg + "," + f.year;
			regionYearVideoTotal.put(ryId, regionYearVideoTotal.getOrDefault(reg, 0) + 1);
		}

		for (String rg : region) {
			String sc = rg.equals("Soviet") ? s_src : rg.equals("Hollywood") ? h_src : src;
			boolean non_chn = rg.equals("Soviet") || rg.equals("Hollywood");
			BufferedReader br = new BufferedReader(new FileReader(sc));
			String line = br.readLine();
			HashMap<String, Integer> typeCount = new HashMap<String, Integer>();
			HashMap<Integer, Integer> stepCount = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> stepHeads = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> yearHeads = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> yearFrames = new HashMap<Integer, Integer>();

			int total = 0, totalFrames = 0;

			while((line = br.readLine()) != null) {
				String v_name = line.split(",")[0];
				Film film = VideoKey.videoToObject(VideoKey.videoToFilmKey(v_name));
				if(non_chn) film = new Film(v_name, v_name);

				String[] rs;
				String r;
				String t;

				if(non_chn) {
					r = rg;
					t = "Feature";
				}else{
					System.out.println(v_name);
					rs = film.getCategory();
					r = VideoKey.metaCategoryToVideoCategory(rs);
					t = film.getFilmType();
				}

				if(!Arrays.asList(type).contains(t)) {
					continue;
				}

				if(r.equals(rg) || non_chn) {
					int size = Integer.parseInt(line.split(",")[4]);
					total += size;
					totalFrames++;
					
					if(typeCount.containsKey(t)) {
						typeCount.put(t, typeCount.get(t) + size);
					} else {
						typeCount.put(t, size);
					}

					int group = (int)(size / step);
					stepCount.put(group, stepCount.getOrDefault(group, 0) + 1);
					stepHeads.put(group, stepHeads.getOrDefault(group, 0) + size);

					int year = film.year;
					yearHeads.put(year, yearHeads.getOrDefault(year, 0) + size);
					yearFrames.put(year, yearFrames.getOrDefault(year, 0) + 1);
				}
			}

			br.close();

			System.out.println(rg + " Total: " + total);
			System.out.println(rg + " Total Frames: " + totalFrames);
			System.out.println(rg + " Avg: " + (total / totalFrames));
			int regionTotalFilms = 0;
			for(String t : type) {
				if((rg.equals("Canton") && t.equals("Musical"))) {
					continue;
				}
				int filmCount = non_chn ? 6 : videoCompletion.get(rg + "," + t);
				regionTotalFilms += filmCount;
				System.out.println(rg + " " + t + ": " + typeCount.getOrDefault(t, 0));
				System.out.println("Film avg: " + (typeCount.getOrDefault(t, 0) / filmCount));
			}

			if(non_chn) regionTotalFilms = 6;

			bw2 = new BufferedWriter(new FileWriter("CV/crowd/counts.csv", true));
			float avg = (float)total / totalFrames;
			for(int i=0; i<stepCount.size(); i++) {
				//int steppedSize = stepCount.getOrDefault(i, 0);
				//System.out.println("Stepped count " + i*step + "-" + step*(i+1) + ": " + steppedSize);
				System.out.println("Stepped avg " + i*step + "-" + step*(i+1) + ": " + ((float)stepHeads.getOrDefault(i, 0) / regionTotalFilms));
				String chunk = i == 0 ? "10-50" : (i*step) + "-" + (step*(i+1));
				bw2.write(rg + "," + chunk + "," + (float)stepCount.getOrDefault(i, 0)/regionTotalFilms + "," + (float)stepHeads.getOrDefault(i, 0)/regionTotalFilms + "," + (avg) + "\n");
			}
			bw2.close();
			
			System.out.println();

			bw = new BufferedWriter(new FileWriter(out, true));
			for(int y : yearHeads.keySet()) {
				int yearFilms = regionYearVideoTotal.getOrDefault(rg + "," + y, 6);
				bw.write(rg + "," + y + "," + yearFrames.get(y) + "," + yearHeads.get(y) + "," + ((float)yearFrames.getOrDefault(y, 0) / yearFilms) + "\n");
			}
			bw.close();
		}
	}
}
