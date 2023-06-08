package OCR.JAVA.romanizing.src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

public class NameRoman {
	private final static String JOBS = "OCR/JAVA/romanizing/translated-jobs.csv";
	private final static String NONHAN_NAMES = "OCR/JAVA/romanizing/roman-non-han_names.csv";
	private final static String ORGANIZATIONS = "OCR/JAVA/romanizing/translated-organizations.csv";
	private final static String TAR = "OCR/translated-names.tsv", LIST_TAR = "OCR/names-full.csv";
	private final static String NODES_LIST = "Network/csv/nodes/nodes-all.csv";

	private final static boolean IGNORE_ORGANIZATIONS_IN_FULL_NAMES = true;

	private static HashMap<String, String> jobToEng = new HashMap<String, String>();
	private static HashMap<String, String> nonHanNameToEng = new HashMap<String, String>();
	private static HashMap<String, String> organizations = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {
		//translateAllNames();
		writeAllNameAndTranslationList();
	}

	private static void writeAllNameAndTranslationList() throws IOException {
		HashMap<String, String> nameToTranslated = new HashMap<String, String>();
		if(nonHanNameToEng.isEmpty()) {
			//Init non-Han names
			File nonHanNames = new File(NONHAN_NAMES);
			BufferedReader reader1 = new BufferedReader(new FileReader(nonHanNames));
			String line = reader1.readLine();
			while((line = reader1.readLine()) != null) {
				String[] parts = line.split(",");
				nonHanNameToEng.put(parts[0], parts[1]);
			}
			reader1.close();
		}
		if(organizations.isEmpty()) {
			//Read all organization names and add to ArrayList
			File orgs = new File(ORGANIZATIONS);
			BufferedReader reader2 = new BufferedReader(new FileReader(orgs));
			String line = "";
			while((line = reader2.readLine()) != null) {
				String[] aspects = line.split(",");
				organizations.put(aspects[0], aspects[1]);
			}
			reader2.close();
			System.out.println("Finished reading indexes");
		}
		if(jobToEng.isEmpty()) {
			File jobs = new File(JOBS);
			BufferedReader reader3 = new BufferedReader(new FileReader(jobs));
			String line = "";
			while((line = reader3.readLine()) != null) {
				String[] parts = line.split(",");
				jobToEng.put(parts[0], parts[1]);
			}
			reader3.close();
		}

		int i = 0;

		ArrayList<Film> films = Film.initAllFilms();
		for (Film film : films) {
			String[] acting = film.getActingNameArray();
			String[] directing = film.getDirectorNameArray();
			String[] writing = film.getScriptwriterNameArray();
			HashMap<String, String> other = film.getOtherStaffNameArrayWithRole();

			for (String name : acting) {
				String roman = "";
				if(!nameToTranslated.containsKey(name)) {
					if(organizations.containsKey(name)) {
						roman = organizations.get(name);
					}else if(nonHanNameToEng.containsKey(name))
						roman = nonHanNameToEng.get(name);
					else
						roman = RomanizeMain.romanizeStandardHanName(name);
					nameToTranslated.put(name, roman);
				}
			}
			for (String name : directing) {
				String roman = "";
				if(!nameToTranslated.containsKey(name)) {
					if(organizations.containsKey(name)) {
						roman = organizations.get(name);
					}else if(nonHanNameToEng.containsKey(name))
						roman = nonHanNameToEng.get(name);
					else
						roman = RomanizeMain.romanizeStandardHanName(name);
					nameToTranslated.put(name, roman);
				}
			}
			for (String name : writing) {
				String roman = "";
				if(!nameToTranslated.containsKey(name)) {
					if(organizations.containsKey(name)) {
						roman = organizations.get(name);
					}else if(nonHanNameToEng.containsKey(name))
						roman = nonHanNameToEng.get(name);
					else
						roman = RomanizeMain.romanizeStandardHanName(name);
					nameToTranslated.put(name, roman);
				}
			}
			for (String name : other.keySet()) {
				String roman = "";
				if(!nameToTranslated.containsKey(name)) {
					if(organizations.containsKey(name)) {
						roman = organizations.get(name);
					}else if(nonHanNameToEng.containsKey(name))
						roman = nonHanNameToEng.get(name);
					else
						roman = RomanizeMain.romanizeStandardHanName(name);
					nameToTranslated.put(name, roman);
				}
			}
			i++;
			System.out.println("Translated " + i + "/" + films.size());
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(LIST_TAR));
		writer.append("key,full name,main region,main job,main job (eng)\n");
		i = 0;
		for (String name : nameToTranslated.keySet()) {
			i++;
			if(organizations.containsKey(name) && IGNORE_ORGANIZATIONS_IN_FULL_NAMES) continue;
			String[] getCatAndRol = getMainGeoCatAndRole(name, organizations.containsKey(name));
			writer.write(name + "," + name + "(" + nameToTranslated.get(name) + ")," + getCatAndRol[0] + "," + getCatAndRol[1] + "," + getCatAndRol[2]);
			writer.newLine();
			System.out.println("Wrote " + i + "/" + nameToTranslated.size());
		}
		writer.close();
	}

	private static String[] getMainGeoCatAndRole(String name, boolean isOrg) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(NODES_LIST));
		String line = reader.readLine();
		while((line = reader.readLine()) != null) {
			String[] parts = line.split(",");
			if(name.equals(parts[1].replaceAll("\"", ""))) {
				String[] output = new String[3];
				output[0] = parts[2].replaceAll("\"", "");
				String[] jobs = parts[4].replaceAll("\"", "").split(" | ");
				for (String string : jobs) {
					string = string.trim();
					if(string.equals("Acting")) {
						output[1] = "演员";
						output[2] = "actor/actress";
						reader.close();
						return output;
					}else if(string.equals("Director")) {
						output[1] = "导演";
						output[2] = "director";
						reader.close();
						return output;
					}else if(string.equals("Scriptwriter")) {
						output[1] = "编剧";
						output[2] = "scriptwriter";
						reader.close();
						return output;
					}else{
						output[1] = string;
						if(jobToEng.containsKey(output[1])) {
							output[2] = jobToEng.get(output[1]);
						} else {reader.close(); throw new IOException("Job not found in jobs list: " + output[1]);}
						reader.close();
						return output;
					}
				}
			}
		}
		reader.close();
		if(isOrg) {
			return new String[] {"", "", ""};
		}else{
			throw new IOException("Name not found in nodes list: " + name);
		}
	}

	@SuppressWarnings("unused")
	private static void translateAllNames() throws IOException {
		File nonHanNames = new File(NONHAN_NAMES);
		File jobs = new File(JOBS);
		BufferedReader reader1 = new BufferedReader(new FileReader(nonHanNames));
		String line = reader1.readLine();
		while((line = reader1.readLine()) != null) {
			String[] parts = line.split(",");
			nonHanNameToEng.put(parts[0], parts[1]);
		}
		reader1.close();
		BufferedReader reader2 = new BufferedReader(new FileReader(jobs));
		line = "";
		while((line = reader2.readLine()) != null) {
			String[] parts = line.split(",");
			jobToEng.put(parts[0], parts[1]);
		}
		reader2.close();
		File orgs = new File(ORGANIZATIONS);
		BufferedReader reader3 = new BufferedReader(new FileReader(orgs));
		line = "";
		while((line = reader3.readLine()) != null) {
			String[] asp = line.split(",");
			organizations.put(asp[0], asp[1]);
		}
		reader3.close();
		System.out.println("Finished reading indexes");

		int i = 0;

		ArrayList<Film> films = Film.initAllFilms();
		ArrayList<TranslateTemp> translated = new ArrayList<TranslateTemp>();
		for (Film film : films) {
			String[] acting = film.getActingNameArray();
			String[] directing = film.getDirectorNameArray();
			String[] writing = film.getScriptwriterNameArray();
			HashMap<String, String> other = film.getOtherStaffNameArrayWithRole();

			String act = "", dir = "", script = "", otherStaff = "";
			for (String name : acting) {
				String roman = "";
				if(organizations.containsKey(name))
					roman = organizations.get(name);
				else if(nonHanNameToEng.containsKey(name))
				//Get specifically translated name (by Google Translate due to lack of appropriate method) if it is not applicable as a standard Han name
					roman = nonHanNameToEng.get(name);
				else
				//Otherwise, romanize the name as a standard Han name (pinyin)
					roman = RomanizeMain.romanizeStandardHanName(name);
				act += roman + ", ";
			}
			if(act.endsWith(", "))
				act = act.substring(0, act.length() - 2);
			for (String name : directing) {
				String roman = "";
				if(organizations.containsKey(name))
					roman = organizations.get(name);
				else if(nonHanNameToEng.containsKey(name))
					roman = nonHanNameToEng.get(name);
				else
					roman = RomanizeMain.romanizeStandardHanName(name);
				dir += roman + ", ";
			}
			if(dir.endsWith(", "))
				dir = dir.substring(0, dir.length() - 2);
			for (String name : writing) {
				String roman = "";
				if(organizations.containsKey(name))
					roman = organizations.get(name);
				else if(nonHanNameToEng.containsKey(name))
					roman = nonHanNameToEng.get(name);
				else
					roman = RomanizeMain.romanizeStandardHanName(name);
				script += roman + ", ";
			}
			if(script.endsWith(", "))
				script = script.substring(0, script.length() - 2);
			for (String name : other.keySet()) {
				String title = other.get(name);
				String roman = "";
				
				if(organizations.containsKey(name))
					roman = organizations.get(name);
				else if(nonHanNameToEng.containsKey(name))
					roman = nonHanNameToEng.get(name);
				else
					roman = RomanizeMain.romanizeStandardHanName(name);

				String engTitle = "";
				if(jobToEng.containsKey(title)) {
					engTitle = jobToEng.get(title);
				} else {
					engTitle = title;
					throw new RuntimeException("Unrecognized job title: " + title);
				}

				roman += " (" + engTitle + ")";
				otherStaff += roman + ", ";
			}
			if(otherStaff.endsWith(", "))
				otherStaff = otherStaff.substring(0, otherStaff.length() - 2);

			translated.add(new TranslateTemp(film.key, dir, script, act, otherStaff));

			i++;
			System.out.println("Translated " + i + "/" + films.size());
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(TAR));
		i = 0;
		for (TranslateTemp t : translated) {
			writer.write(t.toString());
			writer.newLine();
			i++;
			System.out.println("Wrote " + i + "/" + translated.size());
		}
		writer.close();
	}

	private static class TranslateTemp {
		public String act, dir, script, other;
		public String key;

		public TranslateTemp(String key, String dir, String script, String act, String other) {
			this.key = key;
			this.act = act;
			this.dir = dir;
			this.script = script;
			this.other = other;
		}

		@Override
		public String toString() {
			return key + ";" + dir + ";" + script + ";" + act + ";" + other;
		}
	}

}
