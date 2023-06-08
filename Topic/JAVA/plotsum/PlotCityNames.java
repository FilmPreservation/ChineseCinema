package Topic.JAVA.plotsum;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

public class PlotCityNames {
	private final static String TAR = "Topic/all_city_names_in_plots.csv",
		TAR_PREFS = "Topic/region_plot_preferences.csv", TAR_PROVINCES = "Topic/plot_province_geo.csv";
	final static int CONTEXT_LENGTH = 10;

	static HashMap<String, ProvinceObj> provinces = new HashMap<String, ProvinceObj>();
	static ArrayList<CityObj> cities = new ArrayList<CityObj>();
	static HashMap<String, Integer> provinceIds = new HashMap<String, Integer>();
	/**
	 * Format the key String as "source-target" and the value as the preference value
	 * (how many times the target region appeared in a movie made by the source region)
	 */
	static HashMap<String, RegionPreference> regionPreferences = new HashMap<String, RegionPreference>();

	public static void main(String[] args) throws IOException {
		writeAllPlotCitiesAndProvinces();
	}

	private static void writeAllPlotCitiesAndProvinces() throws IOException {
		URL url = new URL("https://raw.githubusercontent.com/brightgems/china_city_dataset/master/china_city_list.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "GBK"));
		String line = reader.readLine();
		
		while((line = reader.readLine()) != null) {
			String[] data = line.split(",");
			CityObj city = new CityObj(data[0], data[2], data[3], data[1]);
			//Although Chongqing was not a municipality before 1997, consider it as one to fit a modern Chinese map
			if(!(city.chinese.equals("北京") || city.chinese.equals("上海") || city.chinese.equals("天津") || city.chinese.equals("重庆")))
				cities.add(city);
			if(!provinces.containsKey(data[3])) {
				ProvinceObj province = new ProvinceObj(data[3], data[4]);
				provinces.put(data[3], province);
			}
		}
		reader.close();
		System.out.println(provinces.size() + " provinces added, " + cities.size() + " cities added.");

		ArrayList<Film> films = Film.initAllFilms();
		HashMap<Film, FilmLocationContainer> filmLocations = new HashMap<Film, FilmLocationContainer>();
		//Iterate all films and find the city names in plot summaries
		for(Film film : films) {
			//Skip non-feature films
			if(!film.getFilmType().equals("Feature")) continue;

			String plot = film.plot;

			if(plot.isEmpty()) continue;

			//If the film is a single region one or a HongKong-Canton one, add the region to the region preference list	
			String[] regions = film.getCategory();
			String region = "";
			if(regions.length == 2) {
				if((regions[0].equals("Hong Kong") && regions[1].equals("Canton"))
					|| (regions[0].equals("Canton") && regions[1].equals("Hong Kong"))) {
					region = "Canton";
				}
			}else if(regions.length == 1) {
				region = regions[0];
			}

			FilmLocationContainer container = new FilmLocationContainer(film.key);
			for(String province : provinces.keySet()) {
				if(plot.contains(province)) {
					container.addNoCityProvince(provinces.get(province), plot, region);
				}
			}
			for(CityObj city : cities) {
				if(plot.contains(city.chinese)) {
					container.addCity(city, plot);
					container.addProvince(provinces.get(city.province), region);
				}
			}
			filmLocations.put(film, container);

			System.out.println(film.title + " " + container.cities.size() + " cities found.");
		}

		//Write the result to a csv file
		BufferedWriter writer = new BufferedWriter(new FileWriter(TAR));
		writer.write("key,cities,provinces,contexts\n");
		for(Film film : filmLocations.keySet()) {
			writer.write(filmLocations.get(film).toString() + "\n");
		}
		writer.close();

		writeAllProvinceGeoLocations();

		//Write the region preference list to a csv file
		writer = new BufferedWriter(new FileWriter(TAR_PREFS));
		writer.write("Source,Target,Weight\n");
		for(String key : regionPreferences.keySet()) {
			RegionPreference preference = regionPreferences.get(key);
			//writer.write(provinceIds.get(preference.source) + "," + provinceIds.get(preference.target) + "," + preference.preference + "\n");
			writer.write((preference.source) + "," + (preference.target) + "," + preference.preference + "\n");
		}
		writer.close();
	}

	private static void writeAllProvinceGeoLocations() throws IOException {
		URL url2 = new URL("https://raw.githubusercontent.com/yhdjyyzk/GeoJSON_data/master/%E5%85%A8%E5%9B%BD%E5%8E%BF%E7%BA%A7%E4%BB%A5%E4%B8%8A%E5%9C%B0%E5%90%8D%E4%BB%A3%E7%A0%81%E5%8F%8A%E7%BB%8F%E7%BA%AC%E5%BA%A6.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(url2.openStream(), "GBK"));
		String line = reader.readLine();

		while((line = reader.readLine()) != null) {
			String[] data = line.split(",");
			String name = data[1];
			for (String string : provinces.keySet()) {
				if(name.equals(string) || name.equals(string + "省")) {
					provinces.get(string).setLatLon(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
					break;
				}else if(string.equals("北京") || string.equals("天津") || string.equals("上海") || string.equals("重庆")) {
					if(name.equals(string + "市")) {
						provinces.get(string).setLatLon(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
						break;
					}
				}else if(string.equals("新疆") && name.equals("新疆维吾尔自治区")) {
					provinces.get(string).setLatLon(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
					break;
				}else if(string.equals("西藏") && name.equals("西藏自治区")) {
					provinces.get(string).setLatLon(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
					break;
				}else if(string.equals("内蒙古") && name.equals("内蒙古自治区")) {
					provinces.get(string).setLatLon(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
					break;
				}else if(string.equals("广西") && name.equals("广西壮族自治区")) {
					provinces.get(string).setLatLon(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
					break;
				}else if(string.equals("宁夏") && name.equals("宁夏回族自治区")) {
					provinces.get(string).setLatLon(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
					break;
				}
			}
		}
		reader.close();

		ProvinceObj reg1 = new ProvinceObj("Northeast", "Northeast");
		provinces.put("Northeast", reg1);
		ProvinceObj reg2 = new ProvinceObj("Beijing", "Beijing");
		provinces.put("Beijing", reg2);
		ProvinceObj reg3 = new ProvinceObj("Canton", "Canton");
		provinces.put("Canton", reg3);
		ProvinceObj reg4 = new ProvinceObj("Shanghai", "Shanghai");
		provinces.put("Shanghai", reg4);
		ProvinceObj reg5 = new ProvinceObj("Xi'an", "Xi'an");
		provinces.put("Xi'an", reg5);

		int id = 0;
		BufferedWriter writer = new BufferedWriter(new FileWriter(TAR_PROVINCES));
		writer.write("Id,Label,English,Lat,Lon\n");
		for(String string : provinces.keySet()) {
			ProvinceObj province = provinces.get(string);
			if(province.lat < -1000) province.lat = province.lon = -1;
			writer.write(id + ",\"" + province.chinese + "\",\"" + province.english + "\"," + province.lat + "," + province.lon + "\n");
			id++;
			provinceIds.put(string, id);
		}
		writer.close();
	}
}

class FilmLocationContainer {
	public ArrayList<CityObj> cities;
	public ArrayList<ProvinceObj> provinces;
	public String key;
	public ArrayList<String> contexts;

	public FilmLocationContainer(String key) {
		this.key = key;
		cities = new ArrayList<CityObj>();
		provinces = new ArrayList<ProvinceObj>();
		contexts = new ArrayList<String>();
	}

	@Override
	public String toString() {
		String output = key + ",";
		for(CityObj city : cities) {
			output += city.chinese + "; ";
		}
		output += ",";
		for(ProvinceObj province : provinces) {
			output += province.chinese + "; ";
		}
		output += ",";
		for(String context : contexts) {
			output += context + "{LINE_CUT}";
		}
		if(contexts.size() > 0) output = output.substring(0, output.length() - 10);
		return output;
	}

	public void addCity(CityObj city, String context) {
		if(!cities.contains(city)) {
			cities.add(city);
			contexts.add(getContext(context, city.chinese));
		}
	}

	public void addProvince(ProvinceObj province, String productionRegion) {
		if(!provinces.contains(province)) {
			provinces.add(province);
			putNewRegionPreference(productionRegion, province);
		}
	}

	public static void putNewRegionPreference(String productionRegion, ProvinceObj province) {
		if(!productionRegion.isEmpty()) {
			if(productionRegion.contains("Shanghai")) productionRegion = "Shanghai";
			//Production region must be one of "Beijing", "Shanghai", "Northeast", "Canton", "Xi'an", otherwise return
			if(!(productionRegion.equals("Beijing") || productionRegion.equals("Shanghai") || productionRegion.equals("Northeast") || productionRegion.equals("Canton") || productionRegion.equals("Xi'an"))) return;
			
			String key = productionRegion+"-"+province;
			if(PlotCityNames.regionPreferences.containsKey(key)) {
				PlotCityNames.regionPreferences.get(key).preference++;
			}else{
				PlotCityNames.regionPreferences.put(key, new RegionPreference(productionRegion, province.chinese, 1));
			}
		}
	}

	public void addNoCityProvince(ProvinceObj province, String context, String productionRegion) {
		if(!provinces.contains(province)) {
			provinces.add(province);
			contexts.add(getContext(context, province.chinese));

			putNewRegionPreference(productionRegion, province);
		}
		
	}

	/**
	 * Get the context of the city name in the plot summary
	 * @param context The plot summary
	 * @return The extracted context of the city name
	 */
	static String getContext(String context, String city) {
		String output = context;

		//If context is shorter than CONTEXT_LENGTH, return the whole context
		//else return a substring with CONTEXT_LENGTH characters surrounding the city name
		if(context.length() <= PlotCityNames.CONTEXT_LENGTH) return output;
		else {
			int index = context.indexOf(city);
			int start = index - PlotCityNames.CONTEXT_LENGTH / 2;
			int end = index + PlotCityNames.CONTEXT_LENGTH / 2;
			if(start < 0) {
				start = 0;
				end = PlotCityNames.CONTEXT_LENGTH;
			}
			if(end > context.length()) {
				end = context.length();
				start = end - PlotCityNames.CONTEXT_LENGTH;
			}
			output = context.substring(start, end);
		}

		return output;
	}
}

class CityObj {
	public String chinese, english, province, adminName;

	public CityObj(String chinese, String english, String province, String adminName) {
		this.chinese = chinese;
		this.english = english;
		this.province = province;
		this.adminName = adminName;
	}
}

class ProvinceObj {
	public String chinese, english;
	public double lat = -9999, lon = -9999;

	public ProvinceObj(String chinese, String english) {
		this.chinese = chinese;
		this.english = english;
	}

	public void setLatLon(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
}

class RegionPreference {
	public String source, target;
	public int preference;

	public RegionPreference(String source, String target, int preference) {
		this.source = source;
		this.target = target;
		this.preference = preference;
	}
}