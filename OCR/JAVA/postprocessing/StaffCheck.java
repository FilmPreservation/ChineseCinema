package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class StaffCheck {
	public static String input = "metadata-extra.csv", output = "namelist.csv";
	/*private static class Member {
		public int freq;
	}*/
	private static class Appearance {
		public String job, name, title;
		public Appearance(String job, String name, String title) {
			this.job = job;
			this.name = name;
			this.title = title;
		}
		@Override
		public String toString() {
			return name + "," + job + "," + title;
		}
	}
	public static void main(String[] args) throws IOException {
		File in = new File(input), out = new File(output);
		BufferedReader reader = new BufferedReader(new FileReader(in));
		BufferedWriter writer = new BufferedWriter(new FileWriter(out));
		String line = "";
		int lineNum = 0;

		while((line = reader.readLine()) != null) {
			lineNum++;
			int expApp = 0;
			String[] aspects = line.split(",");
			//System.out.println(aspects[1] + " : " + aspects[6] + " / " + aspects[7] + " / " + aspects[8]);

			ArrayList<Appearance> apps = new ArrayList<Appearance>();

			for(int i = 6; i<9; i++) {
				String[] names = aspects[i].split("/");
				for (String name : names) {
					if(name.isBlank()) continue;
					name = name.trim();

					//writer.write(name.trim() + "\n");
					expApp++;
					switch(i) {
						case 6:
							addAppearance(apps, new Appearance("Director", name, aspects[0]), lineNum);
							break;
						case 7:
							addAppearance(apps, new Appearance("Scriptwriter", name, aspects[0]), lineNum);
							break;
						case 8:
							addAppearance(apps, new Appearance("Actor/Actress", name, aspects[0]), lineNum);
							break;
					}
				}
			}

			if(!aspects[9].contains("(")) {
				System.out.println("No misc staff at " + lineNum);
			}else {
				String[] staff = aspects[9].split("/");
				for (String name : staff) {
					if(name.isBlank()) continue;

					if(name.contains("(") && name.contains(")")) {
						//writer.write(name.substring(0, name.indexOf("(")).trim()+"\n");
						expApp++;
						String job = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"));
						if(job.contains("(")) {
							System.err.println("Corrupted job: " + name + " at " + lineNum);
						}
						addAppearance(apps, new Appearance(job, name.substring(0, name.indexOf("(")).trim(), aspects[0]), lineNum);
					}else {
						System.err.println("Uncomplete bracket at " + lineNum);
					}
				}
			}
			System.out.println(expApp + " entries expected, " + apps.size() + " recorded at " + lineNum + ".");
			for (Appearance app : apps) {
				writer.write(app.toString() + "\n");
			}
		}
		reader.close();
		writer.close();
	}

	public static void addAppearance(ArrayList<Appearance> apps, Appearance app, int lineNum) {
		for (Appearance appearance : apps) {
			if(appearance.name.equals(app.name)) {
				//System.out.println("Duplicate: " + appearance.name + "("+appearance.job+")" + " : " + app.name + "("+app.job +")");
				if(appearance.job.equals(app.job)) {
					System.out.println("Duplicate: " + appearance.name + "("+appearance.job+")" + " : " + app.name + "("+app.job +") at " + lineNum);
					return;
				}
			}
		}
		apps.add(app);
	}
}