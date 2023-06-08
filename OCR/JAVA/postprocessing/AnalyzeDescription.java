package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AnalyzeDescription {
	private static ArrayList<AnaEntry> entries = new ArrayList<AnaEntry>();
	private static final String ANA_PATH = "analyzed.csv";
	private static ArrayList<String> studioNames = new ArrayList<String>();
	
	public static void main(String[] args) throws IOException {
		findDescriptions();
		System.out.println("Input to procceed...");
		System.in.read();
		analyzeDescription();
		writeEntries();
	}

	public static void findDescriptions() throws IOException {
		File file = new File("un-hyphen.csv");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		boolean inQuote = false;
		int quoteNum = 0;
		String con = "";

		while((line = reader.readLine()) != null) {
			if(inQuote) {
				if(line.endsWith("\"")) {
					quoteNum++;
					inQuote = false;
					con = con.concat("\n" + line);
					entries.add(new AnaEntry(con));
					con = "";
				}else {
					con = con.concat("\n" + line);
				}
			}else {
				if(line.isBlank()) continue;
				if(line.endsWith("\"")) {
					inQuote = true;
					con = con.concat(line);
				}else {
					entries.add(new AnaEntry(line, true));
				}
			}
		}
		System.out.println(quoteNum + " Pairs of Quote Found.");
		reader.close();
	}

	public static void analyzeDescription() {
		int prevYear = 0, year = 0;

		for (AnaEntry entry : entries) {
			entry.replaceAllCommas();
			if(!entry.description.isBlank() && !entry.description.equals("Description")) {
				String[] att = entry.description.split("\\.");
				year = Integer.parseInt(att[0]);
				if(prevYear != year) System.out.println("new year " + year);
				prevYear = year;

				int loc = 1;

				if(att[loc].contains("Studio") || att[loc].contains("Company")) {
					//entry.studio = att[loc].trim();
				}else {
					loc++;
					//entry.studio = att[loc].trim();
					//System.out.println("  " + att[loc].trim());
				}
				entry.studio = att[loc].trim();
				int s = 0;
				for (String name : studioNames) {
					if(name.equals(entry.studio)) {
						break;
					}
					s++;
				}
				if(s >= studioNames.size()) {
					studioNames.add(entry.studio);
					System.out.println("New Studio Found: " + entry.studio);
				}

				loc++;

				att[loc] = att[loc].trim();
				if(att[loc].equals("B & W")) att[loc] = "B&W";
				if(att[loc].equals("Color")) att[loc] = "Colour";
				if(att[loc].equals("Colour") || att[loc].equals("B&W")) {
					entry.colour = att[loc];
				}else {
					loc++;
					entry.colour = att[loc];
					//System.out.println("  " + att[loc]);
				}

				loc++;

				if(!att[loc].contains("reel") && !att[loc].contains("Reel")) {
					loc++;
					if(!att[loc].contains("reel") && !att[loc].contains("Reel")) {
						//System.out.println("  " + att[loc-1]);
						//System.out.println("    " + att[loc]);
					}
				}
				String reelOr = att[loc].trim();
				String[] r = reelOr.split(" ");
				int c = 0;
				for (String string : r) {
					if(string.equalsIgnoreCase("reel") || string.equalsIgnoreCase("reels")) {
						break;
					}
					c++;
				}
				if(c >= r.length) {
					System.out.println("  " + entry.title + ":" + reelOr);
				}else {
					try {
						int i = Integer.parseInt(r[c - 1]);
						if(i > 20) System.out.println("  " + i);
						entry.reel = i + "";
					}catch(Exception e) {
						System.out.println("  " + entry.title + ":" + reelOr);
					}
				}
			}
		}
	}

	public static class AnaEntry {
		public String romanized, title, year, translated, description, originDes;
		public String studio, colour, reel;

		@Override
		public String toString() {
			String out = "";
			if(this.translated.contains(",")) {
				out = romanized+","+title+","+year+",";
				out = out.concat("\""+translated+"\","+studio+","+colour+","+reel);
				out = out.concat(",\""+originDes+"\"\n");
			}else {
				out = romanized+","+title+","+year+",";
				out = out.concat(translated+","+studio+","+colour+","+reel);
				out = out.concat(",\""+originDes+"\"\n");
			}
			return out;
		}

		public AnaEntry(String csv, boolean noQuote) {
			String[] con = csv.split(",");

			this.romanized = con[0];
			this.title = con[1];
			this.year = con[2];
			this.translated = con[3];
			this.originDes = con.length > 4 ? con[4] : "";
			this.studio = "";
			this.colour = "";
			this.reel = "";

			this.description = originDes.replace('\n', ' ');
		}

		public AnaEntry(String csv) {
			String[] quotes = csv.split("\"");

			if(quotes.length < 3) {
				this.originDes = quotes[1];
				String[] commas = quotes[0].split(",");
				
				this.romanized = commas[0];
				this.title = commas[1];
				this.year = commas[2];
				this.translated = commas[3];
				this.studio = "";
				this.colour = "";
				this.reel = "";
			}else {
				this.originDes= quotes[3];
				this.translated = quotes[1];
				String[] commas = quotes[0].split(",");

				this.romanized = commas[0];
				this.title = quotes[1];
				this.year = commas[2];
				this.studio = "";
				this.colour = "";
				this.reel = "";
			}
			this.description = originDes.replace('\n', ' ');
			this.description =this.description.trim();
		}

		public void replaceAllCommas() {
			this.description = this.description.replace(",", ".");
		}
	}

	public static void writeEntries() throws IOException {
		File out = new File(ANA_PATH);
		BufferedWriter writer = new BufferedWriter(new FileWriter(out));
		for (AnaEntry anaEntry : entries) {
			writer.append(anaEntry.toString());
		}
		writer.close();
	}
}