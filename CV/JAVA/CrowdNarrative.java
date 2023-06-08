package CV.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import OCR.JAVA.Film;

public class CrowdNarrative {
	private static boolean NON_CHN = false;
	private static String source = "CV/crowd_results.csv", source2 = "CV/crowd_results-Soviet.csv", source3 = "CV/crowd_results-Hollywood.csv";
	private static HashMap<String, ArrayList<CrowdResult>> results = new HashMap<String, ArrayList<CrowdResult>>();
	private static boolean FEATURE_AND_MUSICAL_ONLY = true;
	private static int CHUNK_DIV = 10;

	//private static String[] tar_regions = {"Shanghai (state)", "Beijing", "Northeast"};
	private static String[] tar_regions = {"Canton", "Xi'an"};
	//private static String[] tar_regions = {"Soviet", "Hollywood"};

	private static String[] non_chn = {"A Dream of Cossack", "Brave People", "Carnival Night", "Pavel Korchagin", "Prologue", "The District Secretary",
					"Casablanca", "Citizen Kane", "Grapes of Wrath", "Great Expectations", "Notorious", "The Wizard of OZ"};

	private static class CrowdResult implements Comparable<CrowdResult> {
		String film;
		int frame;
		float pos;
		int num_people, num_head;
		public Film filmObj;

		public CrowdResult(String film, int frame, float pos, int num_people, int num_head, Film obj) {
			this.film = film;
			this.frame = frame;
			this.pos = pos;
			this.num_people = num_people;
			this.num_head = num_head;
			this.filmObj = obj;
		}
		
		@Override
		public String toString() {
			try {
				String catStr = "";
				String production, type;
				int year = 1950;

				if(!NON_CHN) {
					String[] cat;
					cat = filmObj.getCategory();

					if(cat.length == 1) {
						catStr = cat[0];
					}else{
						//Only Hong Kong-Canton are included in video database
						catStr = "Canton";
					}
					production = filmObj.productionToString();
					year = filmObj.year;
					
					type = filmObj.getFilmType();
				}else{
					production = "Non-Chinese";
					type = "Feature";
					catStr = filmObj.translated;
				}
				
				return film + "," + frame + "," + pos + "," + num_people + "," + num_head + "," + catStr + "," + production + "," + year + "," + type;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public int compareTo(CrowdResult o) {
			return this.frame - o.frame;
		}
	}

	public static void main(String[] args) throws IOException {
		ArrayList<String> nonChnList = new ArrayList<String>(Arrays.asList(non_chn));
		ArrayList<String> videos = NON_CHN ? nonChnList : VideoKey.allVideoFiles();
		ArrayList<CrowdResult> allResults = NON_CHN ? readdAllNonChn() : readAllCrowdResults();

		for (String video : videos) {
			ArrayList<CrowdResult> result = readAllCrowdInFilm(allResults, video);
			Collections.sort(result);
			System.out.println(video + ": " + result.size());
			results.put(video, result);
		}

		String tar = "CV/crowd_narrative.csv";
		if (FEATURE_AND_MUSICAL_ONLY) {
			tar = "CV/crowd_narrative_feature_musical.csv";
		}
		if(NON_CHN) tar = tar.replace(".csv", "_nonchn.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(tar));
		bw.write("film,frame,pos,num_people,num_head,category,production,year,type\n");

		tar = "CV/crowd_narrative_chunked.csv";
		if (FEATURE_AND_MUSICAL_ONLY) {
			tar = "CV/crowd_narrative_feature_musical_chunked.csv";
		}
		if(NON_CHN) tar = tar.replace(".csv", "_nonchn.csv");
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(tar));
		bw2.write("film,title,chunk_start,chunk_end,chunk_cen,head_sum,people_sum,head_avg,people_avg,category,production,year,type\n");

		for (String video : videos) {
			ArrayList<CrowdResult> result = results.get(video);
			for (CrowdResult crowdResult : result) {
				if(Arrays.asList(tar_regions).contains(VideoKey.metaCategoryToVideoCategory(crowdResult.filmObj.getCategory())))
					bw.write(crowdResult.toString() + "\n");
			}

			ArrayList<ChunkedCrowd> chunked = getFilmCrowdsChunked(result, CHUNK_DIV, NON_CHN ? result.get(0).filmObj : VideoKey.videoToObject(VideoKey.videoToFilmKey(video)));
			for (ChunkedCrowd chunkedCrowd : chunked) {
				if(Arrays.asList(tar_regions).contains(VideoKey.metaCategoryToVideoCategory(chunkedCrowd.filmObj.getCategory())))
					bw2.write(chunkedCrowd.toString() + "\n");
			}
		}

		bw.close();
		bw2.close();
	}

	private static class ChunkedCrowd {
		int head_sum, people_sum;
		float head_avg, people_avg;
		float chunk_start, chunk_end, chunk_cen;
		
		Film filmObj;

		public ChunkedCrowd(int head_sum, int people_sum, float head_avg, float people_avg, float chunk_start, float chunk_end, Film filmObj) {
			this.head_sum = head_sum;
			this.people_sum = people_sum;
			this.head_avg = head_avg;
			this.people_avg = people_avg;
			this.chunk_start = chunk_start;
			this.chunk_end = chunk_end;
			this.filmObj = filmObj;

			this.chunk_cen = (chunk_start + chunk_end) / 2;
		}

		@Override
		public String toString() {
			try{
				String catStr = "";
				String type = "";
				String pro = "";

				if(!NON_CHN) {
					String[] cat;
					cat = filmObj.getCategory();

					if(cat.length == 1) {
						catStr = cat[0];
					}else{
						//Only Hong Kong-Canton are included in video database
						catStr = "Canton";
					}

					type = filmObj.getFilmType();
					pro = filmObj.productionToString();
				}else{
					catStr = filmObj.translated;
					type = "Feature";
					pro = "Non-Chinese";
				}

				return filmObj.key + "," + filmObj.title + "," + chunk_start + "," + chunk_end + "," + chunk_cen + "," + head_sum + "," + people_sum + "," + head_avg + "," + people_avg + "," + catStr + "," + pro + "," + filmObj.year + "," + type;
			}catch(IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private static ArrayList<ChunkedCrowd> getFilmCrowdsChunked(ArrayList<CrowdResult> src, int div, Film film) {
		float part = 1f / div;

		ArrayList<ChunkedCrowd> crowds = new ArrayList<ChunkedCrowd>();

		for (int i = 0; i < div; i++) {
			int head_sum = 0, people_sum = 0;
			float head_avg = 0, people_avg = 0;
			int head_count = 0, people_count = 0;
			float chunk_start = i * part;
			float chunk_end = (i + 1) * part;

			for (CrowdResult result : src) {
				if (result.pos >= chunk_start && result.pos < chunk_end) {
					head_sum += result.num_head;
					people_sum += result.num_people;
					head_count++;
					people_count++;
				}
			}

			if (head_count > 0) {
				head_avg = head_sum / head_count;
			}else{
				head_avg = 0;
			}

			if (people_count > 0) {
				people_avg = people_sum / people_count;
			}else{
				people_avg = 0;
			}
			
			ChunkedCrowd crowd = new ChunkedCrowd(head_sum, people_sum, head_avg, people_avg, chunk_start, chunk_end, film);
			crowds.add(crowd);
		}

		return crowds;
	}
	
	private static ArrayList<CrowdResult> readdAllNonChn() throws IOException {
		ArrayList<CrowdResult> results = new ArrayList<CrowdResult>();
		BufferedReader br = new BufferedReader(new FileReader(source2));
		br.readLine(); // skip header
		String line;

		while((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			String film = parts[0];
			int frame = Integer.parseInt(parts[1]);
			float pos = Float.parseFloat(parts[2]);
			int num_people = Integer.parseInt(parts[3]);
			int num_head = Integer.parseInt(parts[4]);

			Film filmObj = new Film(film, film);
			filmObj.translated = "Soviet Union";

			CrowdResult result = new CrowdResult(film, frame, pos, num_people, num_head, filmObj);
			results.add(result);
		}

		br.close();

		br = new BufferedReader(new FileReader(source3));
		br.readLine(); // skip header

		while((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			String film = parts[0];
			int frame = Integer.parseInt(parts[1]);
			float pos = Float.parseFloat(parts[2]);
			int num_people = Integer.parseInt(parts[3]);
			int num_head = Integer.parseInt(parts[4]);

			Film filmObj = new Film(film, film);
			filmObj.translated = "Hollywood";

			CrowdResult result = new CrowdResult(film, frame, pos, num_people, num_head, filmObj);
			results.add(result);
		}

		br.close();

		return results;
	}

	private static ArrayList<CrowdResult> readAllCrowdResults() throws IOException {
		ArrayList<CrowdResult> results = new ArrayList<CrowdResult>();
		BufferedReader br = new BufferedReader(new FileReader(source));
		br.readLine(); // skip header
		String line;

		while((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			String film = parts[0];
			int frame = Integer.parseInt(parts[1]);
			float pos = Float.parseFloat(parts[2]);
			int num_people = Integer.parseInt(parts[3]);
			int num_head = Integer.parseInt(parts[4]);

			Film filmObj = VideoKey.videoToObject(VideoKey.videoToFilmKey(film));
			if(filmObj == null) {
				br.close();
				throw new IOException("Film not found: " + film);
			}

			if(FEATURE_AND_MUSICAL_ONLY) {
				String type = filmObj.getFilmType();
				if(!type.equals("Feature") && !type.equals("Musical")) {
					continue;
				}
			}

			if(tar_regions.length > 0) {
				String region = VideoKey.metaCategoryToVideoCategory(filmObj.getCategory());
				if(!Arrays.asList(tar_regions).contains(region)) {
					continue;
				}
			}

			CrowdResult result = new CrowdResult(film, frame, pos, num_people, num_head, filmObj);
			results.add(result);
		}

		br.close();

		return results;
	}

	private static ArrayList<CrowdResult> readAllCrowdInFilm(ArrayList<CrowdResult> src, String film) {
		ArrayList<CrowdResult> results = new ArrayList<CrowdResult>();

		for (CrowdResult result : src) {
			if (result.film.equals(film)) {
				results.add(result);
			}
		}

		return results;
	}

}
