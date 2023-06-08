package CV.JAVA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import OCR.JAVA.Film;

public class VideoKey {
	private static String videoDir = "../Temp/";
	private static ArrayList<Film> films;

	public static String videoToFilmKey(String videoFile) throws IOException {
		if(videoFile.endsWith(".mp4")) videoFile = videoFile.substring(0, videoFile.length() - 4);
		if(videoFile.contains("/")) videoFile = videoFile.substring(videoFile.lastIndexOf("/") + 1);
		String fKey = "";

		if(films == null) films = Film.initAllFilms();

		if(videoFile.equals("母亲"))
			fKey = "mu qin";
		else if(videoFile.equals("欢天喜地"))
			fKey = "huan tian xi di (1959)";
		else if(videoFile.equals("春雷"))
			fKey = "chun lei (1958)";
		else if(videoFile.equals("穆桂英挂帅"))
			fKey = "mu gui ying gua shuai (1958)";
		else if(videoFile.equals("特快列车"))
			fKey = "“te kuai” lie che";
		else {
			for (Film film : films) {
				String title = film.title.replaceAll("、", "");
				if(title.equals(videoFile)) {
					fKey = film.key;
				}
			}
		}

		return fKey;	
	}

	public static Film videoToObject(String key) throws IOException {
		if(films == null) films = Film.initAllFilms();

		for (Film film : films) {
			if(film.key.equals(key)) {
				return film;
			}
		}

		return null;
	}

	public static void main(String[] args) throws IOException {
		File dir = new File(videoDir);
		File[] files = dir.listFiles();

		int i = 0;

		ArrayList<String> found = new ArrayList<String>();

		for (File file : files) {
			if(file.getName().endsWith(".mp4")) {
				try {
					String fKey = videoToFilmKey(file.getName());
					System.out.println(file.getName() + " -> " + fKey);

					boolean isValidKey = false;
					for (Film f : films) {
						if(f.key.equals(fKey)) {
							isValidKey = true;
							found.add(fKey);
							break;
						}
					}
					if(isValidKey)
						i++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Total: " + i + " " + found.size());

		File file = new File("CV/video_collection.csv");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		i = 0;

		while((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String name = split[0];
			boolean hasVideo = false;
			if(split.length == 5) {
				i++;
				hasVideo = true;
			}
			if(!found.contains(name) && hasVideo) {
				System.out.println(name);
			}
		}

		br.close();
	}

	public static ArrayList<String> allVideoKeys() throws IOException {
		File file = new File("CV/video_collection.csv");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		ArrayList<String> videos = new ArrayList<String>();
		int i = 0;

		while((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String name = split[0];
			boolean hasVideo = false;

			if(split.length == 5) {
				hasVideo = true;
			}
			
			if(hasVideo) {
				videos.add(name);
				i++;
			}
		}

		System.out.println(i);

		br.close();
		return videos;
	}

	public static ArrayList<String> allVideoFiles() throws IOException {
		//Return the file names of all mp4s in videoDir

		File dir = new File(videoDir);
		File[] files = dir.listFiles();

		ArrayList<String> videos = new ArrayList<String>();

		for (File file : files) {
			if(file.getName().endsWith(".mp4")) {
				videos.add(file.getName().substring(0, file.getName().length() - 4));
				System.out.println(file.getName());
			}
		}

		return videos;
	}

	public static String metaCategoryToVideoCategory(String[] meta) {
		String r = "";
		if(meta.length == 1) {
			r = meta[0];
		} else if(meta.length == 2) {
			if(meta[0].equals("Canton") || meta[1].equals("Canton")) {
				if(meta[0].equals("Hong Kong") || meta[1].equals("Hong Kong")) {
					r = "Canton";
				}
			}
		}else{
			return "Other";
		}
		return r;
	}
}
