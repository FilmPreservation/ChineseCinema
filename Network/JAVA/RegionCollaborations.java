package Network.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RegionCollaborations {
	private static String tag = "all";
	private static boolean singleRegionOnly = false, //Counts all multi-region filmmakers as collaborations between the regions
		gephi = true, mainland_only = true;

	private static HashMap<Integer, String> idToRegion = new HashMap<Integer, String>();
	private static HashMap<String, Integer> regionToColab = new HashMap<String, Integer>();
	private static String[] regionToCol;

	private static String[] NON_MAINLAND = {"Hong Kong", "Soviet Union", "Western Europe"};

	public static void main(String[] args) {
		try {
			File output = new File("Network/csv/regions/colab_regions-" + tag + ".csv");
			getAllRegionsFromID();
			getAllCollaborations();
			regionsToColumnNumber();

			if(!gephi) {
				writeGrid(output);
				writePlain(new File("Network/csv/regions/colab_regions_plain-" + tag + ".csv"));
			}else{
				writeGephi();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void getAllRegionsFromID() throws IOException {
		File input = new File("Network/csv/nodes/nodes-" + tag + ".csv");

		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = br.readLine();

		while ((line = br.readLine()) != null) {
			String[] split = line.split(",");
			idToRegion.put(Integer.parseInt(split[0]), split[2].replaceAll("\"", ""));
		}
		br.close();
	}

	private static void getAllCollaborations() throws IOException {
		File input = new File("Network/csv/edges/edges-" + tag + ".csv");

		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = br.readLine();

		while ((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String region1 = idToRegion.get(Integer.parseInt(split[0]));
			String region2 = idToRegion.get(Integer.parseInt(split[1]));
			int weight = Integer.parseInt(split[2]);

			if(region1.contains(" / ") || region2.contains(" / ")) {
				continue;
			}
			if(mainland_only) {
				if(Arrays.asList(NON_MAINLAND).contains(region1) || Arrays.asList(NON_MAINLAND).contains(region2)) {
					continue;
				}
			}
			
			String key = region1 + "-" + region2;
			String reverse = region2 + "-" + region1;
			
			if(regionToColab.containsKey(key)) {
				regionToColab.put(key, regionToColab.get(key) + weight);
			} else if(regionToColab.containsKey(reverse)) {
				regionToColab.put(reverse, regionToColab.get(reverse) + weight);
			} else {
				regionToColab.put(key, weight);
			}
		}
		br.close();

		if(!singleRegionOnly) {
			input = new File("Network/csv/nodes/nodes-" + tag + ".csv");
			br = new BufferedReader(new FileReader(input));
			line = br.readLine();

			while ((line = br.readLine()) != null) {
				String[] split = line.split(",");
				String region = split[2].replaceAll("\"", "");
				if(region.contains(" / ")) {
					String[] rs = region.split(" / ");
					ArrayList<String> regions = new ArrayList<String>();
					for(String r : rs) {
						if(mainland_only) {
							if(Arrays.asList(NON_MAINLAND).contains(r)) {
								continue;
							}
						}
						regions.add(r);
					}

					ArrayList<String> done = new ArrayList<String>();
					//Add all combinations of regions to regionToColab
					for(int i=0; i<regions.size(); i++) {
						for(int n=i+1; n<regions.size(); n++) {
							//Skip self-loops
							if(regions.get(i).equals(regions.get(n))) {
								continue;
							}

							String key = regions.get(i) + "-" + regions.get(n);
							String reverse = regions.get(n) + "-" + regions.get(i);

							//Skip if the reverse has already been added
							if(done.contains(regions.get(n) + "-" + regions.get(i))) {
								continue;
							}
							done.add(key);

							if(regionToColab.containsKey(key)) {
								regionToColab.put(key, regionToColab.get(key) + 1);
							} else if(regionToColab.containsKey(reverse)) {
								regionToColab.put(reverse, regionToColab.get(reverse) + 1);
							} else {
								regionToColab.put(key, 1);
							}
						}
					}
				}
			}
			br.close();
		}
	}

	private static void regionsToColumnNumber() {
		ArrayList<String> regions = new ArrayList<String>();
		int col = 0;
		for (String key : regionToColab.keySet()) {
			String[] split = key.split("-");
			if (!regions.contains(split[0])) {
				regions.add(split[0]);
				col++;
			}
			if (!regions.contains(split[1])) {
				regions.add(split[1]);
				col++;
			}
		}
		regionToCol = new String[regions.size()];
		for(int i=0; i<regions.size(); i++) {
			regionToCol[i] = regions.get(i);
		}
		System.out.println("Number of regions: " + col);
	}

	private static void writeGrid(File output) throws IOException {
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(output));
			for (int i=regionToCol.length-1; i>=0; i--) {
				String key = regionToCol[i];
				bw.write("," + key);
			}
			bw.write("\n");
			HashMap<String, Boolean> done = new HashMap<String, Boolean>();
			for(int i=0; i<regionToCol.length; i++) {
				String reg = regionToCol[i];
				String line = "";
				System.out.println(reg + " ");
				for(int n=regionToCol.length-1; n>=0; n--) {
					String reg2 = regionToCol[n];
					boolean found = false;

					String reverse = reg2 + "-" + reg;

					if(done.containsKey(reverse)) {
						line += ",";
						//System.out.println(reg + " " + reg2);
						continue;
					}
					
					for (String key : regionToColab.keySet()) {
						String[] split = key.split("-");
						String tar = "";
						
						if(split[0].equals(reg)) {
							tar = split[1];
						} else if(split[1].equals(reg)) {
							tar = split[0];
						} else {
							continue;
						}

						if(tar.equals(reg2)) {
							found = true;
							
							line += "," + regionToColab.getOrDefault(key, regionToColab.getOrDefault(reverse, 0));
							break;
						}
					}

					if(!found) {
						line += ",0";
					}

					done.put(reg + "-" + reg2, true);
				}
				bw.write(reg + line + "\n");
			}
			bw.close();
	}

	private static void writePlain(File output) throws IOException {
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(output));
		bw.write("Region1,Region2,Weight,Key\n");
		for (String key : regionToColab.keySet()) {
			String[] split = key.split("-");
			bw.write(split[0] + "," + split[1] + "," + regionToColab.get(key) + "," + key + "\n");
		}
		bw.close();
	}

	private static void writeGephi() throws IOException {
		File output;
		output = new File("Network/csv/regions/colab_regions_edges-" + tag + ".csv");
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(output));
		bw.write("Source,Target,Weight\n");
		for (String key : regionToColab.keySet()) {
			String[] split = key.split("-");
			int id = Arrays.asList(regionToCol).indexOf(split[0]);
			int id2 = Arrays.asList(regionToCol).indexOf(split[1]);

			//Skip self-loops
			if(id == id2) {
				continue;
			}

			bw.write(id + "," + id2 + "," + regionToColab.get(key) + "\n");
		}
		bw.close();

		HashMap<String, float[]> regionToCoord = new HashMap<String, float[]>();
		File source = new File("GIS/source/studios_geo_src.csv");
		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] split = line.split(",");
			String region = split[1];
			float lat = Float.parseFloat(split[2]);
			float lon = Float.parseFloat(split[3]);
			regionToCoord.put(region, new float[] {lat, lon});
		}
		br.close();

		output = new File("Network/csv/regions/colab_regions_nodes-" + tag + ".csv");
		bw = new BufferedWriter(new java.io.FileWriter(output));
		bw.write("Id,Label,lat,lon\n");
		for(int i=0; i<regionToCol.length; i++) {
			String reg = regionToCol[i];
			float[] coord = regionToCoord.get(reg);
			bw.write(i + "," + reg + "," + coord[0] + "," + coord[1] + "\n");
		}
		bw.close();
	}
}