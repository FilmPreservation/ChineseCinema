package Network.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import OCR.JAVA.Film;
import OCR.JAVA.postprocessing.Entry.Temp;

@SuppressWarnings("unused")
public class AverageBetweenness {
	private static String[] NON_MAINLAND = {"Hong Kong", "Soviet Union", "Western Europe"};
	private static boolean SKIP_MULT_REG = true;

	public static void main(String[] args) throws IOException {
		//writeNodeFilmCount();
		//regionCentrality("AllNodes", false);
		//roleCentrality();
		//removeMultiRegion();
		//debutCentrality();
		removeMultiDebutRegion();
	}
	
	private static void removeMultiDebutRegion() throws IOException {
		File source = new File("Network/csv/AllNodes.csv");

		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = br.readLine();
		BufferedWriter bw = new BufferedWriter(new FileWriter("Network/csv/AllNodes-SingleDebut.csv"));
		bw.write(line);

		while((line = br.readLine()) != null) {
			String[] data = line.split(",");
			String region = data[7];
			if(region.contains(" / ")) {
				continue;
			}
			bw.newLine();
			bw.write(line);
		}
		br.close();
		bw.close();
	}

	private static class TempDebut {
		String name, region;
		int year;
		double betweenness;

		public TempDebut(String name, String region, int year, double betweenness) {
			this.name = name;
			this.region = region;
			this.year = year;
			this.betweenness = betweenness;
		}
	}

	private static void debutCentrality() throws IOException {
		File source = new File("Network/csv/AllNodes.csv");

		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = br.readLine();
		ArrayList<TempDebut> list = new ArrayList<TempDebut>();

		while((line = br.readLine()) != null) {
			String[] data = line.split(",");
			//String name = data[1];
			String region = data[2];
			String betweenness = data[16];
			String[] regions;

			if(!SKIP_MULT_REG) {
				if(region.contains(" / ")) {
					regions = region.split(" / ");
				}else{
					regions = new String[] {region};
				}
			}else{
				if(region.contains(" / ")) {
					continue;
				}else{
					regions = new String[] {region};
				}
			}

			System.out.println("sajdoisadois");

			for(String r : regions) {
				if(Arrays.asList(NON_MAINLAND).contains(r)) {
					continue;
				}
				
				double d = Double.parseDouble(betweenness);
				if(d < 0.00001) d = 0.0000001;

				list.add(new TempDebut(data[1], r, Integer.parseInt(data[6]), d));
			}
		}
		br.close();

		String tar2 = "Network/csv/DebutCentrality.csv";
		if(SKIP_MULT_REG) tar2 = tar2.replace(".csv", "_single_region.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(tar2));
		bw.write("Name,D_Year,D_Region,Betweenness Centrality\n");
		for(TempDebut temp : list) {
			bw.write(temp.name + "," + temp.year + "," + temp.region + "," + temp.betweenness);
			bw.newLine();
		}
		bw.close();
	}

	private static void removeMultiRegion() throws IOException {
		File source = new File("Network/csv/AllNodes.csv");

		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = br.readLine();
		BufferedWriter bw = new BufferedWriter(new FileWriter("Network/csv/AllNodes2.csv"));
		bw.write(line);

		while((line = br.readLine()) != null) {
			String[] data = line.split(",");
			String region = data[2];
			if(region.contains(" / ")) {
				continue;
			}
			bw.newLine();
			bw.write(line);
		}
		br.close();
		bw.close();
	}

	private static void roleCentrality() throws IOException {
		File source = new File("Network/csv/NodeFilmCount.csv");

		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = br.readLine();
		HashMap<String, Double> map = new HashMap<String, Double>();
		HashMap<String, Integer> count = new HashMap<String, Integer>();

		while((line = br.readLine()) != null) {
			String[] data = line.split(",");
			String job = data[2];
			String betweenness = data[3];
			String[] jobs;

			if(!SKIP_MULT_REG) {
				if(!job.contains(" | ")) {
					jobs = new String[] {job};
				}else{
					jobs = job.split(" \\| ");
				}
			}else{
				if(!job.contains(" | ")) {
					jobs = new String[] {job};
				}else{
					continue;
				}
			}

			double d = Double.parseDouble(betweenness);
			if(d < 0.00001) d = 0.0000001;

			for (String string : jobs) {
				map.put(string, map.getOrDefault(string, 0.0) + Double.parseDouble(betweenness));
				count.put(string, count.getOrDefault(string, 0) + 1);
			}
		}

		br.close();

		String tar = "Network/csv/RoleCentrality.csv";
		if(SKIP_MULT_REG) tar = tar.replace(".csv", "_single_region.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(tar));
		bw.write("Role,Betweenness Centrality,People\n");
		for(String key : map.keySet()) {
			bw.write(key + "," + String.format("%.8g", map.get(key)) + "," + count.get(key));
			bw.newLine();
		}
		bw.close();
	}

	private static void regionCentrality(String tar, boolean debut) throws IOException {
		File source = new File("Network/csv/" + tar + ".csv");

		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = br.readLine();
		HashMap<String, Double> map = new HashMap<String, Double>();
		HashMap<String, Integer> count = new HashMap<String, Integer>();

		while((line = br.readLine()) != null) {
			String[] data = line.split(",");
			//String name = data[1];
			String region = data[2];
			if(debut) region = data[7];
			int bt;
			switch(tar) {
			case "1950-57nodes":
			case "1958-60nodes":
			case "1961-66nodes":
				bt = 12;
				break;
			case "1949nodes":
				bt = 14;
				break;
			default:
				bt = 16;
				break;
			}
			String betweenness = data[bt];
			String[] regions;

			if(!SKIP_MULT_REG) {
				if(region.contains(" / ")) {
					regions = region.split(" / ");
				}else{
					regions = new String[] {region};
				}
			}else{
				if(region.contains(" / ")) {
					continue;
				}else{
					regions = new String[] {region};
				}
			}

			for(String r : regions) {
				if(Arrays.asList(NON_MAINLAND).contains(r)) {
					continue;
				}

				double d = Double.parseDouble(betweenness);
				if(d < 0.00001) d = 0.0000001;

				map.put(r, map.getOrDefault(r, 0.0) + Double.parseDouble(betweenness));
				count.put(r, count.getOrDefault(r, 0) + 1);
			}
		}
		br.close();

		String tar2 = "Network/csv/RegionCentrality-" + tar + (debut ? "_debut" : "") + ".csv";
		if(SKIP_MULT_REG) tar2 = tar2.replace(".csv", "_single_region.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(tar2));
		bw.write("Region,Betweenness Centrality\n");
		for(String key : map.keySet()) {
			double avg = map.get(key) / count.get(key);
			bw.write(key + "," + avg);
			bw.newLine();
		}
		bw.close();
	}

	private static class CentralNode {
		double betweenness;
		int filmCount;
		String name, cat, job;

		public CentralNode(String name, String cat, String job, double betweenness, int filmCount) {
			this.name = name;
			this.cat = cat;
			this.job = job;
			this.betweenness = betweenness;
			this.filmCount = filmCount;
		}
	}

	private static void writeNodeFilmCount() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		ArrayList<CentralNode> nodes = new ArrayList<CentralNode>();

		for(Film film : films) {
			String[] names = film.getAllNamesArrayWithoutDuplication();
			for(String name : names) {
				map.put(name, map.getOrDefault(name, 0) + 1);
			}
		}

		BufferedReader br = new BufferedReader(new FileReader("Network/csv/AllNodes.csv"));
		String line = br.readLine();

		while((line = br.readLine()) != null) {
			String[] data = line.split(",");
			String name = data[1];
			String cat = data[2];
			String job = data[4];
			String betweenness = data[16];
			nodes.add(new CentralNode(name, cat, job, Double.parseDouble(betweenness), map.get(name)));
		}
		br.close();

		BufferedWriter bw = new BufferedWriter(new FileWriter("Network/csv/NodeFilmCount.csv"));
		bw.write("Name,Category,Job,Betweenness,FilmCount");
		bw.newLine();
		for(CentralNode node : nodes) {
			bw.write(node.name + "," + node.cat + "," + node.job + "," + String.format("%.8g", node.betweenness) + "," + node.filmCount);
			bw.newLine();
		}
		bw.close();
	}
	
}
