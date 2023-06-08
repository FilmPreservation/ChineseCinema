package GIS.JAVA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import GIS.JAVA.GeographyMovement.Pos;
import OCR.JAVA.Studio;

public class MapPlot {
	String name;
	int startYear, endYear;
	int id;
	boolean debutAtPrivate;
	String debutRegion;
	//Use a String formatted as "YEAR-REGION" as the key to get the time of appearance of this person in this region in the year
	HashMap<String, Integer> locations;

	//private static ArrayList<String> tripleCityPlots = new ArrayList<String>();
	
	public MapPlot(int id, String name, int startYear, boolean debutAtPrivate, String debutRegion) {
		this.id = id;
		this.name = name;
		this.startYear = startYear;
		this.endYear = -1;
		this.locations = new HashMap<String, Integer>();
		this.debutAtPrivate = debutAtPrivate;
		this.debutRegion = debutRegion;
	}

	@Override
	public String toString() {
		String str = id + "," + name + "," + startYear + "," + endYear + "," + debutAtPrivate + "," + debutRegion;
		for(int year=1949; year<1967; year++) {
			str = str.concat(",");
			boolean firstOfNode = true;
			for (String yearRegion : locations.keySet()) {
				if(yearRegion.startsWith(year + "-")) {
					if(!firstOfNode) {
						str += " & ";
					}
					firstOfNode = false;
					str += yearRegion.substring(yearRegion.indexOf("-") + 1, yearRegion.length()) + " [" + locations.get(yearRegion) + "]";
				}
			}
		}
		return str;
	}

	//Call this after all locations have been added to find the last appearance of this person
	public void setEndYearAfterLocated() {
		int maxYear = -1;
		for(String key : locations.keySet()) {
			//if(!key.contains("-")) System.err.println("Key " + key + " does not contain -");
			int year = Integer.parseInt(key.split("-")[0]);
			if(year > maxYear) {
				maxYear = year;
			}
		}
		this.endYear = maxYear;
	}

	public void addAppearance(String studio, int year, int count) throws IOException {
		String cat = Studio.getStudioCategory(studio);
		addLocation(cat, year, count);
	}

	private void addLocation(String location, int year, int count) {
		String key = year + "-" + location;
		if(locations.containsKey(key)) {
			locations.put(key, locations.get(key) + count);
		} else {
			locations.put(key, count);
		}
	}

	@Deprecated
	protected static class Location {
		String category;
		int year;
		int count;
		
		public Location(String category, int year) {
			this.category = category;
			this.year = year;
			this.count = 0;
		}
	}

	@Deprecated
	protected static class Person {
		public ArrayList<Location> locations;
		public String name;
		private int id;

		public Person(String name, int id) {
			this.name = name;
			this.id = id;
			locations = new ArrayList<Location>();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == null)
				return false;
			
			if(obj instanceof Person) {
				Person other = (Person) obj;
				return other.id == this.id;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return id;
		}
	}

	//The appearance of a person in a year
	protected static class GeoAppearance {
		public HashMap<String, Integer> affiliatedCategories;//"Studio Category" -> "Number of appearances"
		protected static Random random = new Random();

		public GeoAppearance() {
			this.affiliatedCategories = new HashMap<String, Integer>();
		}

		public String[] getMainCategory() {
			HashMap<String, Integer> hs = new HashMap<String, Integer>();
			int freq = 0;
	
			for (String cat : this.affiliatedCategories.keySet()) {
				int newFreq = 0;
				if (hs.containsKey(cat)) {
					newFreq = hs.get(cat) + this.affiliatedCategories.get(cat);
					throw new RuntimeException("Unexpected studio duplication!");
				} else {
					newFreq = this.affiliatedCategories.get(cat);
				}
				hs.put(cat, newFreq);
				freq = Math.max(newFreq, freq);
			}
	
			Set<Map.Entry<String, Integer> > set = hs.entrySet();
			ArrayList<String> keys = new ArrayList<String>();
			for (Map.Entry<String, Integer> me : set) {
				if (me.getValue() == freq) {
					keys.add(me.getKey());
				}
			}
			return keys.toArray(new String[keys.size()]);
		}

		@Override
		public String toString() {
			String s = "";
			String[] cats = getMainCategory();
			if(cats.length > 0) {
				for(String cat : cats) {
					s += cat + " & ";
				}
			} else {
				s += "None & ";
			}
			Pos tar = estimateGeographicalPosition(cats);

			if(tar != null) {
				return s.substring(0, s.length() - 3) + "," + tar.toString();
			}else{
				return s.substring(0, s.length() - 3) + ",,";
			}
		}

		public static Pos estimateGeographicalPosition(String[] cats) {
			Pos tar = null;
			Pos out;
			if(cats.length > 3) throw new RuntimeException("Unexpected string length!");
			if(cats.length == 1) {
				tar = GeographyMovement.studioPos.get(cats[0]);
				out = new Pos(tar.lat, tar.lon);
			}else if(cats.length == 2) {
				if((cats[0].equals("Shanghai (private)") && cats[1].equals("Northeast")) || (cats[1].equals("Shanghai (private)") && cats[0].equals("Northeast"))) {
					//This midpoint is between Shanghai (private studio) and Northeast
					//Draw it more to the west to avoid being too close to the territal sea of South Korea
					if(GeographyMovement.USE_PIXEL_DATA_AS_SOURCE) {
						out = new Pos(-622, 1618);
					}else{
						out = new Pos(37.2, 122.12);
					}
				}else if((cats[0].equals("Shanghai (state)") && cats[1].equals("Northeast")) || (cats[1].equals("Shanghai (state)") && cats[0].equals("Northeast"))) {
					if(GeographyMovement.USE_PIXEL_DATA_AS_SOURCE) {
						out = new Pos(-622, 1620);
					}else{
						out = new Pos(37.2, 122.2);
					}
				}else if(cats[0].equals("Western Europe") || cats[1].equals("Western Europe")) {
					//This midpoint is between a city in China and Paris
					//Draw it on the location of Paris to mark it as a special case
					if(GeographyMovement.USE_PIXEL_DATA_AS_SOURCE) {
						out = new Pos(-458, 273);
					}else{
						out = new Pos(48.8566, 2.3522);
					}
				}else if(cats[0].equals("Soviet Union") || cats[1].equals("Soviet Union")) {
					//This midpoint is between a city in China and Moscow
					//Draw it on the location of Moscow to mark it as a special case
					if(GeographyMovement.USE_PIXEL_DATA_AS_SOURCE) {
						out = new Pos(-382, 670);
					}else{
						out = new Pos(55.7558, 37.6173);
					}
				}else {
					Pos a = GeographyMovement.studioPos.get(cats[0]);
					Pos b = GeographyMovement.studioPos.get(cats[1]);
					out = Pos.getMiddlePoint(a, b);
				}
			}else if(cats.length == 3) {
				Pos a = GeographyMovement.studioPos.get(cats[0]);
				Pos b = GeographyMovement.studioPos.get(cats[1]);
				Pos c = GeographyMovement.studioPos.get(cats[2]);
				out = Pos.getMiddlePoint(a, b, c);

				/*String form = cats[0] + ", " + cats[1] + ", " + cats[2];
				if(!tripleCityPlots.contains(form)) {
					tripleCityPlots.add(form);
					System.out.println("Triple city plot: " + form);
					try {
						System.in.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}*/
			}else {
				out = tar = null;
			}

			if(GeographyMovement.APPLY_RANDOM_OFFSET) {
				if(tar != null) {
					double lat = tar.lat + random.nextDouble() * GeographyMovement.RANDOM_OFFSET - GeographyMovement.RANDOM_OFFSET / 2;
					double lon = tar.lon + random.nextDouble() * GeographyMovement.RANDOM_OFFSET - GeographyMovement.RANDOM_OFFSET / 2;
					out = new Pos(lat, lon);
				}
			}
			
			return out;
		}

		public static String formatCategories(ArrayList<String> categories) {
			String s = "";
			if(categories.size() > 0) {
				for(String cat : categories) {
					s += cat + " & ";
				}
			} else {
				s += "None & ";
			}
			return s.substring(0, s.length() - 3);
		}
	}

	protected static class GeoPerson {
		public int networkId;
		public String name;
		public int debutYear, lastAppearance;
		public boolean debutFromPrivate;
		public String debutRegion;
		public HashMap<Integer, GeoAppearance> appearances;

		public GeoPerson(int networkId, String name, int debutYear, int lastAppearance, boolean debutFromPrivate, String debutRegion) {
			this.networkId = networkId;
			this.name = name;
			this.debutYear = debutYear;
			this.lastAppearance = lastAppearance;
			this.debutFromPrivate = debutFromPrivate;
			this.debutRegion = debutRegion;
			this.appearances = new HashMap<Integer, GeoAppearance>();
			for(int year=1949; year<1967; year++) {
				this.appearances.put(year, new GeoAppearance());
			}
		}

		@Override
		public String toString() {
			String out = "";
			String base = this.networkId + "," + this.name + "," + this.debutYear + "," + this.lastAppearance + "," + this.debutFromPrivate + "," + this.debutRegion;
			if(!GeographyMovement.APPLY_INERT) {
				for (int year = 1949; year < 1967; year++) {
					out += base + "," + year + "," + this.appearances.get(year).toString() + "\n";
				}
			} else {
				ArrayList<String> prevLocs = new ArrayList<String>();
				for (int year = 1949; year < 1967; year++) {
					if(year < debutYear || year > lastAppearance) {
						//out += base + "," + year + ",None,,\n";
					}else if(year == debutYear) {
						out += base + "," + year + "," + this.appearances.get(year).toString() + "\n";
						String[] locs = this.appearances.get(year).getMainCategory();
						for(String loc : locs) {
							prevLocs.add(loc);
						}
					}else if(this.appearances.get(year).getMainCategory().length <= 0){
						Pos pos = GeoAppearance.estimateGeographicalPosition(prevLocs.toArray(new String[prevLocs.size()]));
						out += base + "," + year + ",Stay," + pos.toString() + "\n";
						System.out.println(out);
						//out += base + "," + year + "," + GeoAppearance.formatCategories(prevLocs) + " ," + pos.toString() + "\n";
					}else{
						String[] newLocs = this.appearances.get(year).getMainCategory();
						ArrayList<String> inertLocs = new ArrayList<String>();
						for (String string : newLocs) {
							if(prevLocs.contains(string)) {
								inertLocs.add(string);
							}
						}

						if(inertLocs.size() <= 0) {
							String[] locs = this.appearances.get(year).getMainCategory();
							for (String string : locs) {
								inertLocs.add(string);
							}
						}

						Pos pos = GeoAppearance.estimateGeographicalPosition(inertLocs.toArray(new String[inertLocs.size()]));
						out += base + "," + year + "," + GeoAppearance.formatCategories(inertLocs) + "," + pos.toString()+ "\n";

						prevLocs.clear();
						for(String loc : inertLocs) {
							prevLocs.add(loc);
						}
					}
				}
			}
			return out;
		}

		public void addAffiliationCategory(String category, int year, int count) throws IOException {
			String key = category;
			if(!GeographyMovement.SPLIT_SHANGHAI_STUDIOS && category.contains("Shanghai")) {
				key = "Shanghai (all)";
			}
			HashMap<String, Integer> app = this.appearances.get(year).affiliatedCategories;
			if(app.containsKey(key)) {
				app.put(key, app.get(key) + count);
			} else {
				app.put(key, count);
			}
		}
	}
		
}
