package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LineBreakCheck {
	private static final String FLAG = "{LINE_CUT}";
	private static HashMap<String, Integer> corrupted = new HashMap<String, Integer>();

	public static void main(String args[]) throws IOException {
		File plt = new File("metadata-staff_plot.csv");
		BufferedReader reader = new BufferedReader(new FileReader(plt));
		String line = "";
		int n = 0;
		ArrayList<String> longPlots = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			String plot = line.split(",")[5];
			plot = plot.replaceAll("\\{QUOTE\\}", "\"");
			if(plot.contains(FLAG)) {
				int index = plot.indexOf(FLAG);
				while (index >= 0) {
					String cut = plot.substring(index-1, index+1);
					if(!corrupted.containsKey(cut))corrupted.put(cut, 1);
					else corrupted.put(cut, corrupted.get(cut)+1);
					index = plot.indexOf(FLAG, index + 1);
				}
			}
			plot = plot.replaceAll("\\{LINE_CUT\\}", "\n");
			if(plot.length() > 511) {
				n++;
				longPlots.add(line.split(",")[0]);
			}
		}
		reader.close();
		for (String string : corrupted.keySet()) {
			System.out.println(string + " " + corrupted.get(string));
		}
		System.out.println(n + " plots are longer than 550 characters.");
		for (String string : longPlots) {
			System.out.println(string);
		}
	}

}