package OCR.JAVA.networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GetCVTargets {
	private static final String TAR = "CV/targets.csv";
	private static final String MAIN_META = "metadata.csv", EXTRA_META = "metadata-extra.csv";
	private static final String STUDIOS = "OCR/studios.csv";
	private static final String TEMP = "OCR/JAVA/networking/CV_temp.csv";

	private static boolean countMultiStudioFilms = true;
	private static boolean countMultiStudioFilmsOnlyForPrimaryStudio = false; //Will have no effect if countMultiStudioFilms is false
	private static boolean keepMultiStudioFilmsMadeByStudiosInSameCategory = true; //Keep the two bools above as 1.true and 2.false and enable this

	public static void main(String args[]) throws IOException {
		File mainMeta = new File(MAIN_META);
		File extraMeta = new File(EXTRA_META);
		BufferedReader mReader = new BufferedReader(new FileReader(mainMeta));
		BufferedReader eReader = new BufferedReader(new FileReader(extraMeta));
		String line;
		int count = 0, count2 = 0, count3 = 0;

		mReader.readLine();
		eReader.readLine();

		while((line = eReader.readLine()) != null) {
			String studioString = line.split(",")[4];
			String[] studios;
			boolean studiosInSameCategory = true;

			if(studioString.contains("&")) {
				studios = studioString.split("&");
				for (int i=0; i<studios.length; i++) {
					studios[i] = studios[i].trim();
				}
				if(!countMultiStudioFilms) studios = new String[0];
				else if(countMultiStudioFilmsOnlyForPrimaryStudio) {
					studios = new String[1];
					studios[0] = studioString.split("&")[0].trim();
				}

				String[] cats = new String[studios.length];
				for (int i=0; i<studios.length; i++) {
					cats[i] = getCategory(studios[i]);
				}
				String primaryCat = cats[0];
				for (String cat : cats) {
					if(!cat.equals(primaryCat)) {
						//If another category appears for this film (which means that is made by studios in different categories)
						//However, films collaboratively made by studios in Hong Kong and Canton will be kept
						//Films collaboratively made by private and state studios in Shanghai will also be kept
						if((cat.equals("Canton") && primaryCat.equals("Hong Kong")) || (cat.equals("Hong Kong") && primaryCat.equals("Canton"))) {
							System.out.println("Hong Kong-Canton film: " + line.split(",")[0]);
							count2++;
							break;
						} else if((cat.equals("Shanghai (private)") && primaryCat.equals("Shanghai (state)")) || (cat.equals("Shanghai (state)") && primaryCat.equals("Shanghai (private)"))) {
							System.out.println("Shanghai private-state film: " + line.split(",")[0]);
							count3++;
							break;
						} else {
							studiosInSameCategory = false;
							System.out.println("Multi-category film: " + line.split(",")[0]);
							count++;
							break;
						}
					}
				}
			} else {
				studios = new String[1];
				studios[0] = studioString;
			}
			
			if(!keepMultiStudioFilmsMadeByStudiosInSameCategory) {
				for (String studio : studios) {
					appendTempCategoryPair(line.split(",")[0], line.split(",")[1], studio, getCategory(studio));
				}
			} else {
				if(studiosInSameCategory) {
					String production = "";
					String category = getCategory(studios[0]);
					for (int i=0; i<studios.length; i++) {
						production = production.concat(studios[i] + (i<(studios.length-1) ? " & " : ""));
					}
					appendTempCategoryPair(line.split(",")[0], line.split(",")[1], production, category);
				}
			}
		}

		while((line = mReader.readLine()) != null) {
			String studioString = line.split(",")[4];
			String[] studios;
			boolean studiosInSameCategory = true;

			if(studioString.contains("&")) {
				studios = studioString.split("&");
				for (int i=0; i<studios.length; i++) {
					studios[i] = studios[i].trim();
				}
				if(!countMultiStudioFilms) studios = new String[0];
				else if(countMultiStudioFilmsOnlyForPrimaryStudio) {
					studios = new String[1];
					studios[0] = studioString.split("&")[0].trim();
				}

				String[] cats = new String[studios.length];
				for (int i=0; i<studios.length; i++) {
					cats[i] = getCategory(studios[i]);
				}
				String primaryCat = cats[0];
				for (String cat : cats) {
					if(!cat.equals(primaryCat)) {
						//If another category appears for this film (which means that is made by studios in different categories)
						//However, films collaboratively made by studios in Hong Kong and Canton will be kept
						//Films collaboratively made by private and state studios in Shanghai will also be kept
						if((cat.equals("Canton") && primaryCat.equals("Hong Kong")) || (cat.equals("Hong Kong") && primaryCat.equals("Canton"))) {
							System.out.println("Hong Kong-Canton film: " + line.split(",")[0]);
							count2++;
							break;
						} else if((cat.equals("Shanghai (private)") && primaryCat.equals("Shanghai (state)")) || (cat.equals("Shanghai (state)") && primaryCat.equals("Shanghai (private)"))) {
							System.out.println("Shanghai private-state film: " + line.split(",")[0]);
							count3++;
							break;
						} else {
							studiosInSameCategory = false;
							System.out.println("Multi-category film: " + line.split(",")[0] + "(" + cat + " != " + primaryCat + ")");
							count++;
							break;
						}
					}
				}
			} else {
				studios = new String[1];
				studios[0] = studioString;
			}
			
			if(!keepMultiStudioFilmsMadeByStudiosInSameCategory) {
				for (String studio : studios) {
					appendTempCategoryPair(line.split(",")[0], line.split(",")[1], studio, getCategory(studio));
				}
			} else {
				if(studiosInSameCategory) {
					String production = "";
					String category = getCategory(studios[0]);
					for (int i=0; i<studios.length; i++) {
						production = production.concat(studios[i] + (i<(studios.length-1) ? " & " : ""));
					}
					appendTempCategoryPair(line.split(",")[0], line.split(",")[1], production, category);
				}
			}
		}

		if(keepMultiStudioFilmsMadeByStudiosInSameCategory) {
			System.out.println("Multi-category films: " + count);
			System.out.println("Hong Kong-Canton films: " + count2);
			System.out.println("Shanghai private-state films: " + count3);
		}

		mReader.close();
		eReader.close();
	}

	private static String getCategory(String studioName) throws IOException {
		File f = new File(STUDIOS);
		BufferedReader r = new BufferedReader(new FileReader(f));
		String l = "";

		while ((l = r.readLine()) != null) {
			if(l.split(",")[0].equals(studioName)) {
				r.close();
				return (l.split(",")[1]);
			}
		}
		r.close();
		return "Misc";
	}

	private static void appendTempCategoryPair(String filmKey, String chnTitle, String studioName, String category) throws IOException {
		File f = new File(keepMultiStudioFilmsMadeByStudiosInSameCategory ? TAR : TEMP);
		BufferedWriter w = new BufferedWriter(new FileWriter(f, true));
		w.append(filmKey + "," + chnTitle + "," + studioName + "," + category + "\n");
		w.close();
	}

}