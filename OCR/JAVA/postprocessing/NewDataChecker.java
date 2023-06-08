package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import OCR.JAVA.Film;
import OCR.JAVA.Studio;

public class NewDataChecker {
	private static final String SRC = "OCR/JAVA/postprocessing/dadian_entries.txt"; // Source path

	private static boolean PRINT_DADIAN_MISSING = true;

	public static void main(String[] args) throws NumberFormatException, IOException {
		//checkEntryCompletionAgainstDadian();
		countDadianCompletion();
	}

	private static void countDadianCompletion() throws IOException {
		File source = new File(SRC);
		ArrayList<String> entriesNotInDadian = new ArrayList<String>();
		ArrayList<String> missingEntries = new ArrayList<String>();
		ArrayList<String> missingInBook = new ArrayList<String>();
		ArrayList<Film> films = Film.initAllFilms();
		ArrayList<Film> filmsInYear = new ArrayList<Film>();
		ArrayList<Film> filmsInExtra = Film.initAllFilmsInExtraMetadata();
		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = "";
		int year = 0;
		int count = 0, total = 0;
		int privateCount = 0, dadianHongKong = 0, nonMainlandStudio = 0;
		ArrayList<String> extraFromDadian = new ArrayList<String>();

		//Read all lines from the file
		while ((line = br.readLine()) != null) {
			if(line.isEmpty()) {
				year = Integer.parseInt(br.readLine());
				System.out.println("Year: " + (year-1) + " with entry count: " + count);
				count = 0;

				if(filmsInYear.size() > 0) {
					//Print all films that were not found
					for (Film film : filmsInYear) {
						entriesNotInDadian.add(film.title);
						if(PRINT_DADIAN_MISSING) {
							missingInBook.add(film.title + ", " + film.year);
						}
					}
				}

				filmsInYear.clear();
				for (Film film : films) {
					//Add films in the year to array
					if(film.year == year) {
						filmsInYear.add(film);
					}
				}
				//System.out.println("Films in year: " + filmsInYear.size() + " in year " + year + ".");
				//System.in.read();
			}else{
				//This is a film in Dadian
				total++;
				
				String title = line;
				if(title.contains("(Hong Kong)") || title.contains("(Taiwan)")) {
					//This entry can be confirmed to be shot in Hong Kong or Taiwan
					dadianHongKong++;
					nonMainlandStudio++;
					total--; //Do not count non-mainland entries in the count
					continue;
				}

				boolean found = false;
				for (Film film : filmsInYear) {
					if(film.title.equals(title)) {
						//This entry can be found in both Dadian and the project data
						for (Film f : filmsInExtra) {
							if(f.title.equals(title) && f.year == year) {
								//This entry is also in the extra metadata
								extraFromDadian.add(title);
								if(f.colour.isBlank()) {
									System.out.println(f.title + " missing colour.");
									System.in.read();
								}
								break;
							}
						}
						
						String[] cats = film.getCategory();
						for (String s : cats) {
							if(s.equals("Shanghai (private)")) {
								//This is a private film
								privateCount++;
								break;
							}
						}

						Studio[] st = film.production;
						for (Studio s : st) {
							//Some Hong Kong-based companies that produce films in Shanghai were categorized in Shanghai
							if(s.category.equals("Hong Kong") || s.name.contains("(Hong Kong)")) {
								nonMainlandStudio++;
							}
						}

						filmsInYear.remove(film);
						found = true;
						break;
					}
				}
				if(!found) {
					//This entry is only in Dadian but not in the project data
					missingEntries.add(title);
					if(title.equals("警惕") || title.equals("逃不了")) privateCount++;
				}
				count++;
			}
		}
		br.close();

		int thisProjPrivateCount = 0;
		for (Film film : films) {
			String[] cats = film.getCategory();
			for (String s : cats) {
				if(s.equals("Shanghai (private)")) {
					//This is a private film
					thisProjPrivateCount++;
					break;
				}
			}
		}

		int bianmuPrivateCount = 0;
		ArrayList<Film> bianmuFilms = Film.initAllFilmsInMainMetadata();
		for (Film film : bianmuFilms) {
			String[] cats = film.getCategory();
			for (String s : cats) {
				if(s.equals("Shanghai (private)")) {
					//This is a private film
					bianmuPrivateCount++;
					break;
				}
			}
		}

		//A film will be counted as "private" if one of its GeoCategories is "Shanghai (private)"
		System.out.println();
		System.out.println("Missing entries from Dadian: " + missingEntries.size());
		System.out.println("Entries not in Dadian: " + entriesNotInDadian.size());
		System.out.println("Total entries in Dadian: " + total);
		System.out.println("Private-studio entries in Dadian: " + privateCount);
		System.out.println("Hong Kong/Taiwan entries in Dadian: " + dadianHongKong);
		System.out.println("Total non-mainland studio entries in Dadian (some of whom were shot in Shanghai): " + nonMainlandStudio);
		System.out.println("Extra-meta entries in Dadian: " + extraFromDadian.size());
		System.out.println("Total entries in this porject: " + films.size());
		System.out.println("Private-studio entries in this project: " + thisProjPrivateCount);
		System.out.println("Total entries in bianmu: " + bianmuFilms.size());
		System.out.println("Private-studio entries in bianmu: " + bianmuPrivateCount);

		if(PRINT_DADIAN_MISSING) {
			System.out.println("\nPrinting film missed in Dadian...");
			for (String s : missingInBook) {
				System.out.println(s);
			}
		}
	}

	@SuppressWarnings("unused")
	private static void checkEntryCompletionAgainstDadian() throws IOException {
		File source = new File(SRC);
		ArrayList<String> entriesNotInDadian = new ArrayList<String>();
		ArrayList<String> missingEntries = new ArrayList<String>();
		ArrayList<Film> films = Film.initAllFilms();
		ArrayList<Film> filmsInYear = new ArrayList<Film>();
		ArrayList<String> duplicateInYear = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = "";
		int year = 0;
		int count = 0;
		//Read all lines from the file
		while ((line = br.readLine()) != null) {
			if(line.isEmpty()) {
				year = Integer.parseInt(br.readLine());
				duplicateInYear.clear();
				System.out.println("Year: " + (year-1) + " with entry count: " + count);
				count = 0;

				if(filmsInYear.size() > 0) {
					//Print all films that were not found
					for (Film film : filmsInYear) {
						entriesNotInDadian.add(film.title);
					}
				}

				filmsInYear.clear();
				for (Film film : films) {
					//Add films in the year to array
					if(film.year == year) {
						filmsInYear.add(film);
					}
				}
				//System.out.println("Films in year: " + filmsInYear.size() + " in year " + year + ".");
				//System.in.read();
			}else{
				String title = line;
				if(title.contains("(Hong Kong)") || title.contains("(Taiwan)")) {
					//Skip films that were actually shot in Hong Kong or Taiwan
					continue;
				}

				if(!duplicateInYear.contains(title))duplicateInYear.add(title);
					else {System.out.println(title + " is a duplicate in year " + (year) + "."); System.in.read();}
				//Iterate through filmsInYear to check if this title appears in that year
				boolean found = false;
				for (Film film : filmsInYear) {
					if(film.title.equals(title)) {
						filmsInYear.remove(film);
						found = true;
						break;
					}
				}
				if(!found) {
					missingEntries.add(title);
					System.out.println(title + " not found in year " + year + ".");
					System.in.read();
				}
				count++;
			}
		}
		br.close();

		System.out.println("Missing entries: " + missingEntries.size());
	}


}