package GIS.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import GIS.JAVA.MapPlot.GeoPerson;
import OCR.JAVA.Film;

public class GeographyMovement {
	protected static final String ALL_NODES_LIST = "Network/csv/nodes/nodes-all.csv";
	private static final String NODES_DIRECTORY = "Network/csv/nodes/";
	private static final String TAR = "GIS/source/people_plots.csv", SRC = "GIS/source/map_plots.csv", GEO_SRC = "GIS/source/studios_geo_src.csv";
	private static final String PIX_SRC = "GIS/source/studios_pixel_src.csv";

	/* If inert is applied, the plot will stay in the city of last
	 * year if it is found to affiliate with multiple regions in the next year and these regions include its previous one
	*/
	protected static boolean APPLY_INERT = true, SPLIT_SHANGHAI_STUDIOS = true;  //These two only work for "initAllPlotlyAnimatedMapPlots()"
	protected static final boolean APPLY_RANDOM_OFFSET = true;
	protected static final double RANDOM_OFFSET = 3.0;
	protected static final boolean USE_PIXEL_DATA_AS_SOURCE = true; //Whether to use latitude and longitude data or pixel data as the source of geographical positions
	private static final boolean UPDATE_BASE_MAP_PLOTS = false; //Run with this enabled once and then disabled once if metadata has been uddated

	protected static class Pos {
		double lat, lon;
		public Pos(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		public static Pos getMiddlePoint(Pos a, Pos b) {
			return new Pos((a.lat + b.lat) / 2, (a.lon + b.lon) / 2);
		}

		public static Pos getMiddlePoint(Pos a, Pos b, Pos c) {
			return new Pos((a.lat + b.lat + c.lat) / 3, (a.lon + b.lon + c.lon) / 3);
		}

		@Override
		public String toString() {
			String la = Double.toString(this.lat);
			if(la.contains("."))
				if(la.substring(la.indexOf(".")).length() > 6)
					la = la.substring(0, la.indexOf(".") + 6);
			String lo = Double.toString(this.lon);
			if(lo.contains("."))
				if(lo.substring(lo.indexOf(".")).length() > 6)
					lo = lo.substring(0, lo.indexOf(".") + 6);
			return la + "," + lo;
		}
	}

	protected static HashMap<String, Pos> studioPos = new HashMap<String, Pos>();

	public static void main(String[] args) {
		try {
			if(UPDATE_BASE_MAP_PLOTS) {
				Film.WriteAllFilmTypes();
				initAllNodesAsMapPlots();
			} else {
				initAllGeographicalPositions(USE_PIXEL_DATA_AS_SOURCE);
				initAllPlotlyAnimatedMapPlots();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	protected static void initAllGeographicalPositions(boolean usePixel) {
		//Use a BufferedReader to read the geo src file
		try {
			File geoSrc = new File(usePixel ? PIX_SRC : GEO_SRC);
			BufferedReader reader = new BufferedReader(new FileReader(geoSrc));
			String line = reader.readLine();
			while((line = reader.readLine()) != null) {
				String[] lineSplit = line.split(",");
				String region = lineSplit[1];
				double lat = Double.parseDouble(lineSplit[2]);
				double lon = Double.parseDouble(lineSplit[3]);
				if(!usePixel)
					studioPos.put(region, new Pos(lat, lon));
				else studioPos.put(region, new Pos(lon, lat));
				if(region.equals("Shanghai (state)")) {
					if(!usePixel)
						studioPos.put("Shanghai (all)", new Pos(lat, lon));
					else studioPos.put("Shanghai (all)", new Pos(lon, lat));
				}
			}
			reader.close();

			if(!usePixel) {
				for (Pos p : studioPos.values()) {
					if(p.lat > 90 || p.lon > 180) {
						System.out.println(p.lat+":"+p.lon);
						try {
							System.in.read();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	static void initAllPlotlyAnimatedMapPlots() throws IOException {
		HashMap<Integer, GeoPerson> people = new HashMap<Integer, GeoPerson>();
		File nodeFile = new File(SRC);
		int lineNum = getFileLineNumber(nodeFile);
		BufferedReader reader = new BufferedReader(new FileReader(nodeFile));
		String line = reader.readLine();
		int n = 0;
		while((line = reader.readLine()) != null) {
			System.out.println("Initing map plots: " + (n) + "/" + lineNum);
			n++;

			String[] lineSplit = line.split(",", -1);
			String name = lineSplit[1];
			int id = Integer.parseInt(lineSplit[0]);
			int debutYear = Integer.parseInt(lineSplit[2]);
			int endYear = Integer.parseInt(lineSplit[3]);
			boolean fromPrivateStudio = lineSplit[4].equalsIgnoreCase("true");
			people.put(id, new GeoPerson(id, name, debutYear, endYear, fromPrivateStudio, lineSplit[5]));

			for(int col=6; col<24; col++) {
				int year = col + 1943;
				String app = lineSplit[col];
				if(!app.isBlank()) {
					String[] apps = app.split(" & ");
					for (String string : apps) {
						int count = Integer.parseInt(string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
						String cat = string.substring(0, string.lastIndexOf("[")).trim();
						people.get(id).addAffiliationCategory(cat, year, count);
					}
				}
			}
		}
		reader.close();

		int k = 0;
		String path = TAR;
		if(USE_PIXEL_DATA_AS_SOURCE) path = path.substring(0, path.lastIndexOf(".")) + "(pixel-axis).csv";
		else path = path.substring(0, path.lastIndexOf(".")) + "(geographical).csv";
		if(!APPLY_RANDOM_OFFSET) path = path.substring(0, path.lastIndexOf(".")) + "(overlapping).csv";
		File target = new File(path);
		BufferedWriter writer = new BufferedWriter(new FileWriter(target, false));

		writer.write("id,name,debut,last,pri,dg,yr,cat,lat,long");
		writer.newLine();
		GeoPerson[] peopleArray = people.values().toArray(new GeoPerson[people.size()]);
		System.out.println("Writing results...");
		
		for (GeoPerson person : peopleArray) {
			writer.append(person.toString());
			System.out.println("Writing to file: " + k + "/" + people.size());
			k++;
		}
		if(USE_PIXEL_DATA_AS_SOURCE)
			writer.append(getAllColourFixPlots());
		writer.close();

		System.out.println("Done.");
	}

	static void initAllNodesAsMapPlots() throws IOException {
		HashMap<String, MapPlot> mapPlots = new HashMap<String, MapPlot>();
		File nodeFile = new File(ALL_NODES_LIST);
		int lineNum = getFileLineNumber(nodeFile);
		BufferedReader reader = new BufferedReader(new FileReader(nodeFile));
		String line = reader.readLine();
		int n = 0;
		while((line = reader.readLine()) != null) {
			System.out.println("Initing map plots: " + (n) + "/" + lineNum);
			n++;

			String[] lineSplit = line.split(",");
			String name = lineSplit[1].replaceAll("\"", "");
			int id = Integer.parseInt(lineSplit[0]);
			int debutYear = Integer.parseInt(lineSplit[6]);
			boolean fromPrivateStudio = lineSplit[8].replaceAll("\"", "").equalsIgnoreCase("True");
			mapPlots.put(name, new MapPlot(id, name, debutYear, fromPrivateStudio, lineSplit[7].replaceAll("\"", "")));
		}
		reader.close();
		
		for(int year = 1949; year < 1967; year++) {
			File yearFile = new File(NODES_DIRECTORY + "nodes-" + year + ".csv");
			lineNum = getFileLineNumber(yearFile) - 2;
			reader = new BufferedReader(new FileReader(yearFile));
			line = reader.readLine();
			int j = 0;
			while((line = reader.readLine()) != null) {
				System.out.println("Loading plot appearances in " + (year) + ": " + j + "/" + lineNum);
				j++;
				if(line.isBlank()) continue;

				String[] lineSplit = line.split(",");
				String name = lineSplit[1].replaceAll("\"", "");
				String[] affiliations = lineSplit[3].replaceAll("\"", "").split("&");
				for(int i = 0; i < affiliations.length; i++) {
					affiliations[i] = affiliations[i].trim();
					String aff = affiliations[i];
					String studio = aff.substring(0, aff.lastIndexOf("(")).trim();
					int count = Integer.parseInt(aff.substring(aff.lastIndexOf("(") + 1, aff.lastIndexOf(")")));
					mapPlots.get(name).addAppearance(studio, year, count);
				}
			}
			System.out.println("Year " + year + " done.");
			reader.close();
		}

		int k = 0;
		String path = SRC;
		File target = new File(path);
		BufferedWriter writer = new BufferedWriter(new FileWriter(target, false));
		writer.write("Network ID,Name,Debut Year,Last Appearance,Debut from Private Studio,Debut Region");
		for(int year = 1949; year < 1967; year++) {
			writer.write("," + year);
		}
		writer.newLine();
		MapPlot[] plots = new MapPlot[mapPlots.size()];
		plots = mapPlots.values().toArray(plots);
		System.out.println("Writing results...");
		for (MapPlot mapPlot : plots) {
			mapPlot.setEndYearAfterLocated();
			writer.append(mapPlot.toString() + "\n");

			System.out.println("Writing to file: " + k + "/" + mapPlots.size());
			k++;
		}
		writer.close();

		System.out.println("Done.");
	}

	protected static int getFileLineNumber(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int lines = 0;
		while (reader.readLine() != null) lines++;
		reader.close();
		return lines;
	}

	private final static String PLOTLY_COLOUR_DEBUG_PREFIX = ",颜色刷新,1949,1949,false,";
	private final static String PLOTLY_COLOUR_DEBUG_FIX = ",1949,Beijing,1000,-1000\n";
	private final static String[] UNLOADED_CATEGORIES = {
		"Shanghai (state)","Shanghai (state) / Hong Kong","Beijing",
		"Beijing / Hong Kong","Northeast","Xi'an","Canton","Hong Kong / Canton",
		"Xinjiang","Sichuan","Shanghai (state) / Xinjiang","Hubei",
		"Anhui","Shandong","Soviet Union / Northeast","Beijing / Xi'an",
		"Hong Kong / Northeast","Inner Mongolia / Northeast",
		"Beijing / Xinjiang","Xi'an / Qinghai","Xi'an / Northeast",
		"Beijing / Hubei","Beijing / Anhui","Beijing / Western Europe",
		"Shanghai (state) / Anhui","Gansu / Northeast","Shanghai (state) / Canton",
		"Shanghai (state) / Hunan","Tianjin","Beijing / Shanghai (state)",
		"Zhejiang","Shanghai (roc) / Shanghai (private)","Sichuan / Northeast",
		"Shanghai (state) / Jiangsu","Beijing / Northeast","Beijing / Tianjin",
		"Shanghai (state) / Shanghai (private)","Shanghai (state) / Northeast","Shanghai (state) / Xi'an",
		"Northeast / Tianjin","Canton / Northeast","Beijing / Shanghai (private) / Northeast",
		"Beijing / Sichuan","Shanghai (state) / Shandong","Beijing / Shanghai (state) / Anhui",
		"Beijing / Shanghai (state) / Northeast"
	};

	private static String getAllColourFixPlots() {
		String result = "";
		int id = -1;
		for(String category : UNLOADED_CATEGORIES) {
			result += id + PLOTLY_COLOUR_DEBUG_PREFIX + category + PLOTLY_COLOUR_DEBUG_FIX;
			id--;
		}
		return result;
	}
	
}