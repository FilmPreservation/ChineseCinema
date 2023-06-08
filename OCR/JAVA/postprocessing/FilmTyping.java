package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class Film {
	public String id;
	public String title;
	public String year;
	public String translate;
	public String production;
	public String colour;
	public String reels;
	public String special;
	public String type;

	public Film(String id, String title, String year, String translate, String production, String colour, String reels, String special) {
		this.id = id;
		this.title = title;
		this.year = year;
		this.translate = translate;
		this.production = production;
		this.colour = colour;
		this.reels = reels;
		this.special = special;
		this.type = "Unkown";
	}
}

public class FilmTyping {
	private static final String MAIN = "metadata.csv", EXTRA = "metadata-extra.csv", ASPECTS = "OCR/special_aspects.csv";
	public static String[] SUGGESTED_TYPES = {"Feature", "Musical", "Performance", "Artistic Documentary", "Opera"};
	public static String[] FILM_TYPES = {"Feature", "Performance", "Artistic Documentary", "Opera"};
	//Performance: All non-operatic performances, including singing, dancing, and stage drama.

	public static void main(String[] args) throws IOException {
		loadSpecialAspects();
	}

	protected static void loadSpecialAspects() throws IOException {
		System.out.println("Hello World!");

		File mFile = new File(MAIN);
		File eFile = new File(EXTRA);
		BufferedReader mReader = new BufferedReader(new FileReader(mFile));
		mReader.readLine();
		BufferedReader eReader = new BufferedReader(new FileReader(eFile));
		eReader.readLine();

		String line;
		while((line = mReader.readLine()) != null) {
			String[] data = line.split(",", -1);
			Film film = new Film(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);

			if(film.special.isBlank()) continue;
			checkAspects(film.special);
		}

		while((line = eReader.readLine()) != null) {
			String[] data = line.split(",", -1);
			Film film = new Film(data[0], data[1], data[2], data[3], data[4], data[5], "", data[6]);

			if(film.special.isBlank()) continue;
			
			checkAspects(film.special);
		}

		mReader.close();
		eReader.close();
	}

	private static String[] checkAspects(String specialInData) throws IOException {
		String[] aspects = specialInData.split(";");
		for(int i=0; i<aspects.length; i++) {
			aspects[i] = aspects[i].trim();

			File asp = new File(ASPECTS);
			Boolean found = false;
			BufferedReader sReader = new BufferedReader(new FileReader(asp));
	
			String line;
			while((line = sReader.readLine()) != null) {
				String as = line.split(",")[0];
				if(as.equals(aspects[i])) {
					found = true;
					break;
				}
			}
			sReader.close();
			
			if(!found) {
				String unpublished = aspects[i].contains("Unpublished") ? "True" : "False";
				String scriptAdapted = aspects[i].contains("Adapted from") ? "True" : "False";
				String type = "";

				if(scriptAdapted.equals("False") && !aspects[i].contains("Also known as")) {
					if(aspects[i].contains("Opera")) {
						System.out.println("New aspect \"" + aspects[i] + "\" found. Added to Opera type.");
						System.console().readLine();
						type = SUGGESTED_TYPES[4];
					} else {
						System.out.println("New aspect \"" + aspects[i] + "\" found. Add to recordance by type:\n(-(-1):None, -0:Feature, -1:Musical, -2:Performance, -3:Artistic Documentary, -4:Opera");
						int input = Integer.parseInt(System.console().readLine());
						type = input > 0 ? SUGGESTED_TYPES[input] : "";
					}
					System.out.println();
				}
				
				File tar = new File(ASPECTS);
				BufferedWriter writer = new BufferedWriter(new FileWriter(tar, true));
				writer.append(aspects[i] + "," + type + "," + unpublished + "," + scriptAdapted + "\n");
				writer.close();
			}

		}

		return aspects;
	}

	@SuppressWarnings("unused")
	protected static void typeFilms() throws IOException {
		System.out.println("Hello World!");

		File mFile = new File(MAIN);
		File eFile = new File(EXTRA);
		BufferedReader mReader = new BufferedReader(new FileReader(mFile));
		BufferedReader eReader = new BufferedReader(new FileReader(eFile));

		String line;
		while((line = mReader.readLine()) != null) {
			String[] data = line.split(",");
			Film film = new Film(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
		}

		while((line = eReader.readLine()) != null) {
			String[] data = line.split(",");
			Film film = new Film(data[0], data[1], data[2], data[3], data[4], data[5], "", data[6]);
		}

		mReader.close();
		eReader.close();
	}

	public static String getFilmType(String line, boolean isExtra) throws IOException {
		String[] data = line.split(",", -1);
		Film film = new Film(data[0], data[1], data[2], data[3], data[4], data[5], isExtra ? "" : data[6], isExtra ? data[6] : data[7]);

		String[] aspects = film.special.split(";");
		for(int i=0; i<aspects.length; i++) {
			aspects[i] = aspects[i].trim();

			File asp = new File(ASPECTS);
			BufferedReader sReader = new BufferedReader(new FileReader(asp));
	
			String line2, cat = "";
			while((line2 = sReader.readLine()) != null) {
				String as = line2.split(",")[0];
				if(as.equals(aspects[i])) {
					cat = line2.split(",")[1];
					break;
				}
			}
			sReader.close();
			
			if(cat.isBlank()) continue;
			else {
				return cat;
			}
		}

		return "Feature";
	}

}