package OCR.JAVA.networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

//Use this program to filter the name of private and state-owned film studios
public class PrivateFilter {
	private static final String MAIN_META = "metadata.csv", EXTRA_META = "metadata-extra.csv";
	private static final String STUDIOS = "OCR/studios.csv";

	// In the extra metadata, there are two exceptional entries made by state-owned studios: 
	// "中央电影摄影场一厂", "中央电影摄影场二厂".
	// These entries will be manually filtered.
	public static void main(String[] args) throws IOException {
		File mainMeta = new File(MAIN_META);
		File extraMeta = new File(EXTRA_META);
		BufferedReader mReader = new BufferedReader(new FileReader(mainMeta));
		BufferedReader eReader = new BufferedReader(new FileReader(extraMeta));
		String line;
		int count = 0;

		mReader.readLine();
		eReader.readLine();

		while((line = eReader.readLine()) != null) {
			String studioString = line.split(",")[4];
			String[] studios;

			if(studioString.contains("&")) {
				studios = studioString.split("&");
				for (int i=0; i<studios.length; i++) {
					studios[i] = studios[i].trim();
				}
			} else {
				studios = new String[1];
				studios[0] = studioString;
			}
			
			for (String studio : studios) {
				if(!isRecordedStudio(studio)) {
					System.out.println("New private studio \"" + studio + "\" found.");

					appendStudio(studio, "Shanghai (private)");
					count++;
				}
			}
		}

		System.out.println(count + " new private studios in extra list found.");
		count = 0;
		int count2 = 0;
		System.console().readLine();

		while((line = mReader.readLine()) != null) {
			String studioString = line.split(",")[4];
			String[] studios;

			if(studioString.contains("&")) {
				studios = studioString.split("&");
				for (int i=0; i<studios.length; i++) {
					studios[i] = studios[i].trim();
				}
			} else {
				studios = new String[1];
				studios[0] = studioString;
			}
			
			for (String studio : studios) {
				if(!isRecordedStudio(studio)) {
					System.out.println("New studio \"" + studio + "\" found. Add to recordance?\n(-b: Beijing, -s: Shanghai (state), -p: Shanghai (private), -c: Northeast, -g: Canton, -m: misc");
					String input = System.console().readLine();
					if(input.equalsIgnoreCase("b")) {
						count++;
						appendStudio(studio, "Beijing");
					} else if(input.equalsIgnoreCase("s")) {
						count++;
						appendStudio(studio, "Shanghai (state)");
					} else if(input.equalsIgnoreCase("p")) {
						count2++;
						appendStudio(studio, "Shanghai (private)");
					} else if(input.equalsIgnoreCase("c")) {
						count++;
						appendStudio(studio, "Northeast");
					} else if(input.equalsIgnoreCase("g")) {
						count++;
						appendStudio(studio, "Canton");
					} else {
						count++;
						appendStudio(studio, "Misc");
					}
				}
			}
		}

		System.out.println(count2 + " new private studios in main list found.");
		System.out.println(count + " new state studios in main list found.");

		eReader.close();
		mReader.close();
	}

	private static boolean isRecordedStudio(String name) throws IOException {
		File f = new File(STUDIOS);
		BufferedReader r = new BufferedReader(new FileReader(f));
		String l = "";

		while ((l = r.readLine()) != null) {
			if(l.split(",")[0].equals(name)) {
				r.close();
				return true;
			}
		}
		r.close();
		return false;
	}

	/*//Must be used for recorded studios
	private static boolean isPrivate(String name) throws IOException {
		File f = new File(STUDIOS);
		BufferedReader r = new BufferedReader(new FileReader(f));
		String l = "";

		while ((l = r.readLine()) != null) {
			if(l.split(",")[0].equals(name)) {
				r.close();
				return (l.split(",")[1].equals("Shanghai (private)"));
			}
		}
		r.close();
		return false;
	}*/
	
	private static void appendStudio(String name, String category) throws IOException {
		File org = new File(STUDIOS);
		BufferedWriter writer = new BufferedWriter(new FileWriter(org, true));
		writer.append(name + "," + category + "\n");
		writer.close();
	}

}
