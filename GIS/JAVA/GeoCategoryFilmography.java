package GIS.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

public class GeoCategoryFilmography {
	private static final String SOURCE_PATH = "GIS/source/studios_geo_src.csv", SOURCE_PIX_PATH = "GIS/source/studios_pixel_src.csv",
		TAR = "GIS/source/studios_geo.csv", TAR_SHANGHAI_SPLITED = "GIS/source/studios_geo_shanghai_splited.csv";
	private static final double MAPBOX_SIZE_SCALE = 10; //The size of the circle in Mapbox is scaled by this number

	/* To update all data used for visualzation,
	run with "USE_PIXEL_AS_AXIS = false" and the other four 
	in four combinations first, then run with 
	"USE_PIXEL_AS_AXIS = true, ACCUMULATE = false, CLUSTER_SHANGHAI_SUBS =  true"
	*/

	private static boolean CLUSTER_SHANGHAI_SUBS = true,
		ACCUMULATE = false; //enable ACCUMULATE to count the films shot before the selected year into the selected year
	private static boolean USE_PIXEL_AS_AXIS = true; //Whether to use pixel data as the axis of the map

	private static class GeoCategoryYearData {
		public int colourFilmCount, bwFilmCount;
		public int filmCountByYear;
		
		public GeoCategoryYearData(int filmCountByYear, int colourFilmCount, int bwFilmCount) {
			this.colourFilmCount = colourFilmCount;
			this.bwFilmCount = bwFilmCount;
			this.filmCountByYear = filmCountByYear;
		}

		private double getColourRatio() {
			if(colourFilmCount + bwFilmCount == 0) return 0;
			else
				return (double)colourFilmCount / (double)(colourFilmCount + bwFilmCount);
		}

		@Override
		public String toString() {
			return filmCountByYear + "," + getColourRatio() + "," + (double)filmCountByYear * MAPBOX_SIZE_SCALE;
		}
	}

	private static class GeoCategoryFilmCounter {
		public String city;
		public String region;
		public double latitude;
		public double longitude;
		public int filmTotalCount;
		public HashMap<Integer, GeoCategoryYearData> filmographyByYear;
		
		public GeoCategoryFilmCounter(String city, String region, double latitude, double longitude, int filmTotalCount) {
			this.city = city;
			this.region = region;
			this.latitude = latitude;
			this.longitude = longitude;
			this.filmTotalCount = filmTotalCount;
			this.filmographyByYear = new HashMap<Integer, GeoCategoryYearData>();
			for(int y=1949; y<1967; y++) 
				filmographyByYear.put(y, new GeoCategoryYearData(0, 0, 0));
		}

		//Add the appearance of a film to the filmography HashMap
		public void addFilm(Film film, int selectedYear) {
			filmographyByYear.get(selectedYear).filmCountByYear++;
			if(film.colour.equalsIgnoreCase("Colour") || film.colour.equalsIgnoreCase("Color"))
				filmographyByYear.get(selectedYear).colourFilmCount++;
			else if(!film.colour.isBlank()) //Some films have no colour information and should not be counted
				filmographyByYear.get(selectedYear).bwFilmCount++;
		}

		public String toString() {
			String base = city + "," + region + "," + latitude + "," + longitude + "," + filmTotalCount;
			String s = "";
			for (int year = 1949; year < 1967; year++) {
				s += base + "," + formatYearToRFC3339(year) + "," + filmographyByYear.get(year).toString() + "\n";
			}
			return s;
		}

		private static String formatYearToRFC3339(int year) {
			return year + "-12-31";
		}
	}

	public static void main(String[] args) {
		try {
			printAllCategoryFilmCount();
			//initAllGeoCategories();
			//countAllCategoryFilmCounts();
			countAllCategoryFilmCountsByYear();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void printAllCategoryFilmCount() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> filmCount = new HashMap<String, Integer>();
		for (Film film : films) {
			String[] locations = film.getCategory();
			for (String location : locations) {
				if(filmCount.containsKey(location))
					filmCount.put(location, filmCount.get(location) + 1);
				else
					filmCount.put(location, 1);
			}
		}
		for (String key : filmCount.keySet()) {
			System.out.println(key + ": " + filmCount.get(key));
		}
		System.out.println("Read all GeoCategory film coutns. Proceed?");
		System.in.read();
	}

	private static void countAllCategoryFilmCountsByYear() throws IOException {
		File srcFile = new File(USE_PIXEL_AS_AXIS ? SOURCE_PIX_PATH : SOURCE_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(srcFile));
		String line = reader.readLine();
		//Read all cities' data into an ArrayList
		ArrayList<GeoCategoryFilmCounter> counters = new ArrayList<GeoCategoryFilmCounter>();
		while((line = reader.readLine()) != null) {
			String[] lineSplit = line.split(",");
			String city = lineSplit[0];
			String region = lineSplit[1];
			double latitude = Double.parseDouble(lineSplit[2]);
			double longitude = Double.parseDouble(lineSplit[3]);
			int filmTotalCount = Integer.parseInt(lineSplit[4]);
			if(!USE_PIXEL_AS_AXIS)
				counters.add(new GeoCategoryFilmCounter(city, region, latitude, longitude, filmTotalCount));
			else
				counters.add(new GeoCategoryFilmCounter(city, region, longitude, latitude, filmTotalCount));
		}
		reader.close();

		if(CLUSTER_SHANGHAI_SUBS) {
			//Cluster Shanghai's sub-groups into one city
			GeoCategoryFilmCounter shanghai;
			if(!USE_PIXEL_AS_AXIS)
				shanghai = new GeoCategoryFilmCounter("Shanghai", "Shanghai (all)", 31.2304,121.4737, 0);
			else
				shanghai = new GeoCategoryFilmCounter("Shanghai", "Shanghai (all)", -708, 1610, 0);
			for (int i=0; i<counters.size(); i++) {
				GeoCategoryFilmCounter counter = counters.get(i);
				if(counter.city.equals("Shanghai")) {
					shanghai.filmTotalCount += counter.filmTotalCount;
					counters.remove(counter);
					i--;
				}
			}
			counters.add(shanghai);
		}

		//Iterate from 1949 to 1966 and get an ArrayList of the films shot in each year (or all films shot before if ACCUMULATE is enabled)
		ArrayList<Film> films = Film.initAllFilms();
		for(int year = 1949; year < 1967; year++) {
			ArrayList<Film> filmsInYear = new ArrayList<Film>();
			for (Film film : films) {
				if(ACCUMULATE) {
					if(film.year <= year) filmsInYear.add(film);
				} else {
					if(film.year == year) filmsInYear.add(film);
				}
			}
			
			//Iterate through all expected films and add their appearance to the filmography HashMap
			for (Film film : filmsInYear) {
				String[] cats = film.getCategory();
				boolean containsShanghai = false;

				for (String cat : cats) {
					for (GeoCategoryFilmCounter counter : counters) {
						if(counter.region.equals(cat)) {
							counter.addFilm(film, year);
						}else if(CLUSTER_SHANGHAI_SUBS && !containsShanghai && cat.contains("Shanghai") && counter.city.equals("Shanghai")) {
							counter.addFilm(film, year);
							containsShanghai = true;
						}
					}
				}
			}

			//Write all the data to a CSV file
			String path = (CLUSTER_SHANGHAI_SUBS ? TAR : TAR_SHANGHAI_SPLITED);
			if(USE_PIXEL_AS_AXIS) path = path.substring(0, path.lastIndexOf(".")) + "(pixel)" + ".csv";
			if(!ACCUMULATE) path = path.substring(0, path.lastIndexOf(".")) + "(year_isolated)" + ".csv";
			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			writer.write("city,geo_category,latitude,longitude,total_films,year,films_in_year,colour_film_ratio_in_year,suggested_plot_size");
			writer.newLine();
			for (GeoCategoryFilmCounter counter : counters) {
				writer.append(counter.toString());
			}
			writer.close();
		}
	}

	@SuppressWarnings("unused")
	private static void initAllGeoCategories() throws IOException {
		File nodeFile = new File("OCR/studios.csv");
		BufferedReader reader = new BufferedReader(new FileReader(nodeFile));
		String line = reader.readLine();
		int n = 0;
		ArrayList<String> names = new ArrayList<String>();
		while((line = reader.readLine()) != null) {
			String[] lineSplit = line.split(",");
			if(!names.contains(lineSplit[1])) names.add(lineSplit[1]);
		}
		reader.close();
		for (String string : names) {
			System.out.println(string);
		}
	}

	@SuppressWarnings("unused")
	private static void countAllCategoryFilmCounts() throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		for (Film film : films) {
			String[] cats = film.getCategory();
			for (String c : cats) {
				if(counts.containsKey(c)) {
					counts.put(c, counts.get(c) + 1);
				} else {
					counts.put(c, 1);
				}
			}
		}
		for (String c : counts.keySet()) {
			System.out.println(c + ": " + counts.get(c));
		}
	}
	
}
