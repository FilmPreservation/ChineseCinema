package OCR.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Use this finalised utility class to store information about a film.
 * A Film with reels as negative number is one with no recordance of such information.
 */
public class Film {
	public String key, title, translated, colour, special, plot;
	@JsonIgnore
	public String director, scriptwriter, acting, staff;
	public int year, reels;
	public Studio[] production;

	public static final String METADATA_PATH = "metadata.csv", EXTRA_METADATA_PATH = "metadata-extra.csv", STAFF_PLOT_DATA_PATH = "metadata-staff_plot.csv";
	public static final String ORGANISATION_LIST_PATH = "OCR/organizations.csv", LONG_PERSON_NAME_LIST_PATH = "OCR/non-han_chn_names_or_special_authorship.csv";
	public static final String STUDIO_LIST_PATH = "OCR/studios.csv";
	public static final String TRANSLATED_NAMES = "OCR/translated-names.tsv", TRANSLATED_PLOTS = "OCR/translated-plot.tsv";

	public static HashMap<String, String> studioCategoryMap;

	public Film(String key, String title, int year, String translated, String production, String colour, int reels, String special, String director, String scriptwriter, String acting, String staff, String plot) throws IOException {
		this.key = key;
		this.title = title;
		this.translated = translated;
		this.production = productionAttributeToStudioNameArray(production);
		this.colour = colour;
		this.special = special;
		this.director = director;
		this.scriptwriter = scriptwriter;
		this.acting = acting;
		this.staff = staff;
		this.plot = plot;
		this.year = year;
		this.reels = reels;
	}

	public Film(String line, boolean isInMainMetadata) throws IOException {
		String[] values = line.split(",", -1);
		key = values[0];
		title = values[1];
		year = Integer.parseInt(values[2]);
		translated = values[3];
		production = productionAttributeToStudioNameArray(values[4]);
		colour = values[5];
		
		if(isInMainMetadata) {
			reels = Integer.parseInt(values[6]);
			special = values[7];
			loadStaffPlotInfoForMetadataFromKey(this.key);
		} else {
			reels = -1;
			special = values[6];
			director = values[7];
			scriptwriter = values[8];
			acting = values[9];
			staff = values[10];
			plot = values[11];
		}
	}

	//Return an empty object
	public Film(String key, String title) {
		this.key = key;
		this.title = title;
		this.translated = "";
		this.production = new Studio[0];
		this.colour = "";
		this.special = "";
		this.director = "";
		this.scriptwriter = "";
		this.acting = "";
		this.staff = "";
		this.plot = "";
		this.year = 0;
		this.reels = 0;
	}

	private void loadStaffPlotInfoForMetadataFromKey(String key) throws IOException {
		File spFile = new File(STAFF_PLOT_DATA_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(spFile));
		String line = null;
		while((line = reader.readLine()) != null) {
			String[] values = line.split(",");
			String spKey = values[0];
			if(spKey.equals(this.key)) {
				this.director = values[1];
				this.scriptwriter = values[2];
				this.acting = values[3];
				this.staff = values[4];
				this.plot = values[5];
				reader.close();
				return;
			}
		}
		reader.close();
		throw new IOException("Cannot find film with key: " + key);
	}

	public String[] getCategory() throws IOException {
		ArrayList<String> categories = new ArrayList<String>();
		for (Studio studio : production) {
			String cat = studioCategoryMap.get(studio.name);
			if(cat != null) {
				if(!categories.contains(cat))
					categories.add(cat);
			} else throw new IOException("Unexpected studio name: " + studio.name);
		}
		return categories.toArray(new String[categories.size()]);
	}

	public String getFilmType() throws IOException {
		File mFile = new File(METADATA_PATH);
		File eFile = new File(EXTRA_METADATA_PATH);
		BufferedReader mReader = new BufferedReader(new FileReader(mFile));
		BufferedReader eReader = new BufferedReader(new FileReader(eFile));
		String line = null;
		while((line = mReader.readLine()) != null) {
			String[] values = line.split(",");
			String mKey = values[0];
			if(mKey.equals(this.key)) {
				String type = OCR.JAVA.postprocessing.FilmTyping.getFilmType(line, false);
				mReader.close();
				eReader.close();
				return type;
			}
		}
		while((line = eReader.readLine()) != null) {
			String[] values = line.split(",");
			String eKey = values[0];
			if(eKey.equals(this.key)) {
				String type = OCR.JAVA.postprocessing.FilmTyping.getFilmType(line, true);
				mReader.close();
				eReader.close();
				return type;
			}
		}
		mReader.close();
		eReader.close();
		throw new IOException("Cannot find film with key: " + key);
	}

	public String productionToString() {
		String result = "";
		for (Studio studio : production) {
			result += studio.name + " & ";
		}
		return result.substring(0, result.length() - 3);
	}

	private static String[] staffAttributeToArray(String attribute) {
		if(attribute.isBlank()) return new String[0];

		String[] staffArray = attribute.split("/");
		for(int i = 0; i < staffArray.length; i++) {
			staffArray[i] = staffArray[i].trim();
		}
		return staffArray;
	}
	
	private static Studio[] productionAttributeToStudioNameArray(String production) throws IOException {
		if(production.isBlank()) return new Studio[0];

		String[] proArray = production.split("&");
		Studio[] proList = new Studio[proArray.length];
		for(int i = 0; i < proArray.length; i++) {
			proArray[i] = proArray[i].trim();
			proList[i] = new Studio(proArray[i]);
		}
		return proList;
	}

	public String[] getDirectorNameArray() {
		return staffAttributeToArray(this.director);
	}

	public String[] getScriptwriterNameArray() {
		return staffAttributeToArray(this.scriptwriter);
	}

	public String[] getActingNameArray() {
		return staffAttributeToArray(this.acting);
	}

	@JsonIgnore
	public String[] getOtherStaffNameArray() throws IOException {
		if(staff.isBlank()) return new String[0];

		String[] staffArray = this.staff.split("/");
		for(int i = 0; i < staffArray.length; i++) {
			if(!staffArray[i].contains("(")) {
				throw new IOException("A member of staff does not have a role: " + staffArray[i] + " in film: " + this.title + "(" + this.key + ")");
			}
			staffArray[i] = staffArray[i].substring(0, staffArray[i].indexOf("("));
			staffArray[i] = staffArray[i].trim();
		}
		return staffArray;
	}

	public HashMap<String, String> getOtherStaffNameArrayWithRole() throws IOException {
		HashMap<String, String> staffMap = new HashMap<String, String>();
		if(staff.isBlank()) return staffMap; //Return an empty map if there is no staff

		String[] staffArray = this.staff.split("/");
		for(int i = 0; i < staffArray.length; i++) {
			if(!staffArray[i].contains("(")) {
				throw new IOException("A member of staff does not have a role: " + staffArray[i] + " in film: " + this.title + "(" + this.key + ")");
			}
			String name = staffArray[i].substring(0, staffArray[i].indexOf("("));
			name = name.trim();
			String role = staffArray[i].substring(staffArray[i].indexOf("(") + 1, staffArray[i].indexOf(")"));
			staffMap.put(name, role);
		}
		return staffMap;
	}

	//Only returns true if a name is found in the director, scriptwriter, or other staff fields
	public boolean hasFilmmaker(String name) throws IOException {
		String[] directorArray = getDirectorNameArray();
		String[] scriptwriterArray = getScriptwriterNameArray();
		String[] otherStaffArray = getOtherStaffNameArray();
		for(String director : directorArray) {
			if(director.equals(name)) return true;
		}
		for(String scriptwriter : scriptwriterArray) {
			if(scriptwriter.equals(name)) return true;
		}
		for(String otherStaff : otherStaffArray) {
			if(otherStaff.equals(name)) return true;
		}
		return false;
	}

	//Only returns true if a name is found in the acting attribute
	public boolean hasActorOrActress(String name) throws IOException {
		String[] actingArray = getActingNameArray();
		for(String acting : actingArray) {
			if(acting.equals(name)) return true;
		}
		return false;
	}

	//Returns true if a name is found in the film
	public boolean hasName(String name) throws IOException {
		String[] directorArray = getDirectorNameArray();
		String[] scriptwriterArray = getScriptwriterNameArray();
		String[] otherStaffArray = getOtherStaffNameArray();
		String[] actingArray = getActingNameArray();
		for(String director : directorArray) {
			if(director.equals(name)) return true;
		}
		for(String scriptwriter : scriptwriterArray) {
			if(scriptwriter.equals(name)) return true;
		}
		for(String otherStaff : otherStaffArray) {
			if(otherStaff.equals(name)) return true;
		}
		for(String acting : actingArray) {
			if(acting.equals(name)) return true;
		}
		return false;
	}

	//Returns a list of roles that a name has in a film
	public ArrayList<String> getRolesOfAName(String name) throws IOException {
		String[] directorArray = getDirectorNameArray();
		String[] scriptwriterArray = getScriptwriterNameArray();
		HashMap<String, String> otherStaffArrayWithRole = getOtherStaffNameArrayWithRole();
		String[] otherStaffArray = otherStaffArrayWithRole.keySet().toArray(new String[0]);
		String[] actingArray = getActingNameArray();
		ArrayList<String> roles = new ArrayList<String>();
		for(String director : directorArray) {
			if(director.equals(name)) roles.add("Director");
		}
		for(String scriptwriter : scriptwriterArray) {
			if(scriptwriter.equals(name)) roles.add("Scriptwriter");
		}
		for(String otherStaff : otherStaffArray) {
			if(otherStaff.equals(name)) roles.add(otherStaffArrayWithRole.get(name));
		}
		for(String acting : actingArray) {
			if(acting.equals(name)) roles.add("Acting");
		}
		return roles;
	}

	//Use this method to get an array of all entry films
	public static ArrayList<Film> initAllFilms() throws IOException {
		File mFile = new File(METADATA_PATH);
		File eFile = new File(EXTRA_METADATA_PATH);
		BufferedReader mReader = new BufferedReader(new FileReader(mFile));
		BufferedReader eReader = new BufferedReader(new FileReader(eFile));
		String line = null;
		ArrayList<Film> films = new ArrayList<Film>();

		mReader.readLine();
		eReader.readLine();

		while((line = mReader.readLine()) != null)
			films.add(new Film(line, true));
		while((line = eReader.readLine()) != null)
			films.add(new Film(line, false));
			
		mReader.close();
		eReader.close();

		//Init studios map
		studioCategoryMap = new HashMap<String, String>();
		File studioFile = new File(STUDIO_LIST_PATH);
		BufferedReader sReader = new BufferedReader(new FileReader(studioFile));
		sReader.readLine(); //Skip header
		while((line = sReader.readLine()) != null) {
			String[] lineArray = line.split(",");
			studioCategoryMap.put(lineArray[0], lineArray[1]);
		}
		sReader.close();

		return films;
	}

	//Do not call this without calling initAllFilms first
	public static ArrayList<Film> initAllFilmsInExtraMetadata() throws IOException {
		File eFile = new File(EXTRA_METADATA_PATH);
		BufferedReader eReader = new BufferedReader(new FileReader(eFile));
		String line = null;
		ArrayList<Film> films = new ArrayList<Film>();
		eReader.readLine();

		while((line = eReader.readLine()) != null)
			films.add(new Film(line, false));;
		eReader.close();

		if(studioCategoryMap == null) {
			studioCategoryMap = new HashMap<String, String>();
			File studioFile = new File(STUDIO_LIST_PATH);
			BufferedReader sReader = new BufferedReader(new FileReader(studioFile));
			sReader.readLine(); //Skip header
			while((line = sReader.readLine()) != null) {
				String[] lineArray = line.split(",");
				studioCategoryMap.put(lineArray[0], lineArray[1]);
			}
			sReader.close();
		}

		return films;
	}

	//Do not call this without calling initAllFilms first
	public static ArrayList<Film> initAllFilmsInMainMetadata() throws IOException {
		File mFile = new File(METADATA_PATH);
		BufferedReader mReader = new BufferedReader(new FileReader(mFile));
		String line = null;
		ArrayList<Film> films = new ArrayList<Film>();
		mReader.readLine();

		while((line = mReader.readLine()) != null)
			films.add(new Film(line, true));;
		mReader.close();

		if(studioCategoryMap == null) {
			studioCategoryMap = new HashMap<String, String>();
			File studioFile = new File(STUDIO_LIST_PATH);
			BufferedReader sReader = new BufferedReader(new FileReader(studioFile));
			sReader.readLine(); //Skip header
			while((line = sReader.readLine()) != null) {
				String[] lineArray = line.split(",");
				studioCategoryMap.put(lineArray[0], lineArray[1]);
			}
			sReader.close();
		}

		return films;
	}

	//Use this function to check if a name signed in a certain role is an organisation or a person
	public static boolean isOrganisation(String name) throws IOException {
		if(name.length() <= 3) return false;

		File orgFile = new File(ORGANISATION_LIST_PATH);
		File pFile = new File(LONG_PERSON_NAME_LIST_PATH);
		BufferedReader orgReader = new BufferedReader(new FileReader(orgFile));
		BufferedReader pReader = new BufferedReader(new FileReader(pFile));
		String line = null;

		while((line = orgReader.readLine()) != null) {
			if(line.equals(name)) {
				pReader.close();
				orgReader.close();
				return true;
			}
		}

		while((line = pReader.readLine()) != null) {
			if(line.equals(name)) {
				pReader.close();
				orgReader.close();
				return false;
			}
		}
			
		pReader.close();
		orgReader.close();
		throw new IOException("Unexpected filmmaker name: \"" + name + "\"");
	}

	@JsonIgnore
	public String[] getAllNamesArrayWithoutDuplication() throws IOException {
		ArrayList<String> names = new ArrayList<String>();
		String[][] allStaff = new String[4][];
		allStaff[0] = getDirectorNameArray();
		allStaff[1] = getScriptwriterNameArray();
		allStaff[2] = getOtherStaffNameArray();
		allStaff[3] = getActingNameArray();

		for(int i = 0; i < allStaff.length; i++) {
			for(int j = 0; j < allStaff[i].length; j++) {
				if(!names.contains(allStaff[i][j]))
					names.add(allStaff[i][j]);
			}
		}

		return names.toArray(new String[names.size()]);
	}

	public String getTranslatedPlotSummary() throws IOException {
		File file = new File(TRANSLATED_PLOTS);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;

		while((line = reader.readLine()) != null) {
			String[] lineArray = line.split("\\t");
			if(lineArray[0].equals(this.key)) {
				reader.close();
				String plot = lineArray[1];
				if(plot.endsWith("\"") && plot.startsWith("\""))
					plot = plot.substring(1, plot.length() - 1);
				return plot;
			}
		}
			
		reader.close();
		throw new IOException("Unexpected film key in translated plots: \"" + this.key + "\"");
	}

	public static void WriteAllFilmTypes() throws IOException {
		File tar = new File("OCR/categorized_types.csv");
		BufferedWriter writer = new BufferedWriter(new FileWriter(tar));
		ArrayList<Film> films = initAllFilms();
		for(Film film : films) {
			String line = film.key + "," + film.title + "," + film.getFilmType() + ",";
			for (Studio s : film.production) {
				line += s.name + " & ";
			}
			line = line.substring(0, line.length() - 3) + ",";
			for (String c : film.getCategory()) {
				line += c + " / ";
			}
			line = line.substring(0, line.length() - 3);
			writer.append(line + "\n");
		}
		writer.close();
	}

	@SuppressWarnings("unused")
	@Deprecated
	private static void DEBUG_PrintAllPlots(String args[]) {
		try {
			ArrayList<Film> films = initAllFilms();
			for(Film film : films) {
				//System.out.println(film.key + " " + film.title + "\n" + film.plot + "\n");
				String plot = film.plot;
				char[] plotArray = plot.toCharArray();
				boolean isDigit = false;
				for(int i = 0; i < plotArray.length; i++) {
					if(Character.isDigit(plotArray[i])) {
						if(!isDigit)
							System.out.println(film.key + " " + film.title + "\n" + film.plot);
						isDigit = true;
						System.out.print(plotArray[i]);
					}
				}
				if(isDigit) System.out.println();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//1-directors, 2-scriptwriters, 3-acting members, 4-other staff
	@JsonIgnore
	private String getTranslatedMembership(int col) throws IOException {
		File file = new File(TRANSLATED_NAMES);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = reader.readLine()) != null) {
			String[] lineArray = line.split(";", -1);
			if(lineArray[0].equals(this.key)) {
				reader.close();
				String directors = lineArray[col];
				return directors;
			}
		}
		reader.close();
		throw new IOException("Unexpected film key in translated names: \"" + this.key + "\"");
	}
	
	public String getTranslatedDirectors() throws IOException {
		return getTranslatedMembership(1);
	}

	public String getTranslatedScriptwriters() throws IOException {
		return getTranslatedMembership(2);
	}

	public String getTranslatedActing() throws IOException {
		return getTranslatedMembership(3);
	}

	public String getTranslatedOtherStaff() throws IOException {
		return getTranslatedMembership(4);
	}

}
