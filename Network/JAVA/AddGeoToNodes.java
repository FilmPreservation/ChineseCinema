package Network.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class AddGeoToNodes {
	private final static String TAG = "all";
	private static final boolean APPLY_RANDOM_OFFSET = true;
	private final static double OFFSET = 2.0;

	private final static String SRC_ROOT = "Network/csv/nodes/nodes-";

	private final static String SRC_GEO = "GIS/source/studios_geo_src.csv";
	private final static String TAR_DIR = "Network/csv/geo_nodes/geo_nodes-";
	private static HashMap<String, GeoTemp> geoCatToPos = new HashMap<String, GeoTemp>();

	public static void main(String[] args) throws NumberFormatException, IOException {
		//Read all entries in SRC_GEO with latitude and longtitude
		File geoSrc = new File(SRC_GEO);
		BufferedReader br = new BufferedReader(new FileReader(geoSrc));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] geo = line.split(",");
			geoCatToPos.put("\"" + geo[1] + "\"", new GeoTemp(Double.parseDouble(geo[2]), Double.parseDouble(geo[3])));
		}
		br.close();

		//Read all entries in SRC_ROOT with TAG and write it with geographical information to TAR_DIR with same TAG
		File tar = new File(TAR_DIR + TAG + ".csv");
		File src = new File(SRC_ROOT + TAG + ".csv");
		br = new BufferedReader(new FileReader(src));
		line = br.readLine();
		BufferedWriter bw = new BufferedWriter(new FileWriter(tar));
		bw.write("Id,Label,GeoCategory,Affilations,Main Role,All Roles,Debut Year,Debut Region,Debut at Private Studio,lat,lon\n");
		while((line = br.readLine()) != null) {
			String geocat = line.split(",")[2];

			//Multi-region nodes add 0
			if(geocat.contains(" / ")) {
				double lat = 0;
				double lon = 0;
				if(APPLY_RANDOM_OFFSET) {
					lat += Math.random() * OFFSET - OFFSET / 2;
					lon += Math.random() * OFFSET - OFFSET / 2;
				}
				bw.write(line + "," + lat + "," + lon + "\n");
				continue;
			}

			if(!geoCatToPos.containsKey(geocat)) {
				br.close();
				bw.close();
				throw new RuntimeException("No geo information for " + geocat);
			}
			GeoTemp geo = geoCatToPos.get(geocat);
			if(geo != null) {
				double lat = geo.lat;
				double lon = geo.lon;
				if(APPLY_RANDOM_OFFSET) {
					lat += Math.random() * OFFSET - OFFSET / 2;
					lon += Math.random() * OFFSET - OFFSET / 2;
				}
				bw.write(line + "," + lat + "," + lon + "\n");
			}
		}
		br.close();
		bw.close();
	}

}

class GeoTemp {
	double lat, lon;

	public GeoTemp(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
}