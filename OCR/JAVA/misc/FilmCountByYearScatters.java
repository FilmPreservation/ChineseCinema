package OCR.JAVA.misc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;
import OCR.JAVA.Studio;

public class FilmCountByYearScatters {
	private static final String GEOCAT_TAR = "GIS/statistics/geocat_film_counts_by_year.csv",
								STUDIO_TAR = "GIS/statistics/studio_film_counts_by_year.csv";
	private static final String GEOCAT_TOTAL_TAR = "GIS/statistics/geocat_total_film_counts.csv",
								STUDIO_TOTAL_TAR = "GIS/statistics/studio_total_film_counts.csv";
	private static final String GEOCAT_FEATURE_TAR = "GIS/statistics/geocat_feature_counts_by_year.csv",
								STUDIO_FEATURE_TAR = "GIS/statistics/studio_feature_counts_by_year.csv";
	private static final String GEOCAT_FEATURE_TOTAL_TAR = "GIS/statistics/geocat_feature_total_counts.csv",
								STUDIO_FEATURE_TOTAL_TAR = "GIS/statistics/studio_feature_total_counts.csv";

	private static HashMap<String, FilmYear> regionYearCount, studioYearCount;
	/**
	 * Maps a studio name to the total number of films it has produced throughout 1949-1966,
	 * so that minor studios can be filtered out.
	 */
	private static HashMap<String, FilmYear> studioFilmCount, regionFilmCount;
	/**
	 * If a studio has produced less than this many films throughout 1949-1966, it will be ignored.
	 * This is to prevent the scatter graph from being too crowded.
	 * Suggested values: 20 when FEATURES_ONLY is disabled, and 10 otherwise.
	 */
	private static final int STUDIO_THRESHOLD = 10,
							 REGION_THRESHOLD = 10;
	/**
	 * Enable this to keep Feature Films only.
	 * Use a lower threshold for STUDIO_THRESHOLD and REGION_THRESHOLD if this is enabled.
	 */
	private static boolean FEATURES_ONLY = false;
	
	public static void main(String args[]) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		regionYearCount = new HashMap<String, FilmYear>();
		studioYearCount = new HashMap<String, FilmYear>();
		studioFilmCount = new HashMap<String, FilmYear>();
		regionFilmCount = new HashMap<String, FilmYear>();

		for (Film film : films) {
			if(FEATURES_ONLY && !film.getFilmType().equalsIgnoreCase("Feature")) continue;

			String[] regions = film.getCategory();
			int year = film.year;

			for(String region : regions) {
				String key = region + "-" + year;
				if(regionYearCount.containsKey(key)) {
					FilmYear fy = regionYearCount.get(key);
					fy.count++;
					fy.addFilm(film);
					regionYearCount.put(key, fy);
				} else {
					FilmYear fy = new FilmYear(year, region, 1);
					fy.addFilm(film);
					regionYearCount.put(key, fy);
				}

				if(regionFilmCount.containsKey(region)) {
					FilmYear fy = regionFilmCount.get(region);
					fy.count++;
					fy.addFilm(film);
					regionFilmCount.put(region, fy);
				} else {
					FilmYear fy = new FilmYear(-1, region, 1);
					fy.addFilm(film);
					regionFilmCount.put(region, fy);
				}
			}

			Studio[] studios = film.production;
			for(Studio studio : studios) {
				String key = studio.name + "-" + year;
				if(studioYearCount.containsKey(key)) {
					FilmYear fy = studioYearCount.get(key);
					fy.count++;
					fy.addFilm(film);
					studioYearCount.put(key, fy);
				} else {
					FilmYear fy = new FilmYear(year, studio.name, 1);
					fy.addFilm(film);
					studioYearCount.put(key, fy);
				}

				if(studioFilmCount.containsKey(studio.name)) {
					FilmYear fy = studioFilmCount.get(studio.name);
					fy.count++;
					fy.addFilm(film);
					studioFilmCount.put(studio.name, fy);
				} else {
					FilmYear fy = new FilmYear(-1, studio.name, 1);
					fy.addFilm(film);
					studioFilmCount.put(studio.name, fy);
				}
			}
		}

		//Filter all studios that have produced less than STUDIO_THRESHOLD films
		ArrayList<String> studiosToRemove = new ArrayList<String>();
		for(String key : studioFilmCount.keySet()) {
			if(studioFilmCount.get(key).count < STUDIO_THRESHOLD) {
				studiosToRemove.add(key);
			}
		}
		for(String key : studiosToRemove) {
			for(int year=1949; year<=1966; year++) {
				String yrKey = key + "-" + year;
				if(studioYearCount.containsKey(yrKey))
					studioYearCount.remove(yrKey);
			}
		}

		//Filter all regions that have produced less than REGION_THRESHOLD films
		ArrayList<String> regionsToRemove = new ArrayList<String>();
		for(String key : regionFilmCount.keySet()) {
			if(regionFilmCount.get(key).count < REGION_THRESHOLD) {
				regionsToRemove.add(key);
			}
		}
		for(String key : regionsToRemove) {
			for(int year=1949; year<=1966; year++) {
				String yrKey = key + "-" + year;
				if(regionYearCount.containsKey(yrKey))
					regionYearCount.remove(yrKey);
			}
		}

		//Add a clustered group of regions in one year named as "All" to the regionYearCount and studioYearCount maps
		for(int year=1949; year<=1966; year++) {
			String key = "All-" + year;
			regionYearCount.put(key, new FilmYear(year, "All", 0));
			for(Film film : films) {
				if(FEATURES_ONLY && !film.getFilmType().equalsIgnoreCase("Feature")) continue;
				if(film.year != year) continue;

				regionYearCount.get(key).count++;
				regionYearCount.get(key).addFilm(film);
			}
		}

		//Write the two maps to csv
		BufferedWriter writer = new BufferedWriter(new FileWriter(FEATURES_ONLY ? GEOCAT_FEATURE_TAR : GEOCAT_TAR));
		writer.write("GeoCategory,Year,Count,Colour,Length\n");
		for(String key : regionYearCount.keySet()) {
			FilmYear fy = regionYearCount.get(key);
			writer.write(fy.toString() + "\n");
		}
		writer.close();

		writer = new BufferedWriter(new FileWriter(FEATURES_ONLY ? STUDIO_FEATURE_TAR : STUDIO_TAR));
		writer.write("Studio,Year,Count,Colour,Length\n");
		for(String key : studioYearCount.keySet()) {
			FilmYear fy = studioYearCount.get(key);
			writer.write(fy.toString() + "\n");
		}
		writer.close();

		//Write the total counts to csv
		writer = new BufferedWriter(new FileWriter(FEATURES_ONLY ? GEOCAT_FEATURE_TOTAL_TAR : GEOCAT_TOTAL_TAR));
		writer.write("GeoCategory,Count,Colour,Length\n");
		for(String key : regionFilmCount.keySet()) {
			writer.write(regionFilmCount.get(key).toString() + "\n");
		}
		writer.close();

		writer = new BufferedWriter(new FileWriter(FEATURES_ONLY ? STUDIO_FEATURE_TOTAL_TAR : STUDIO_TOTAL_TAR));
		writer.write("Studio,Count,Colour,Length\n");
		for(String key : studioFilmCount.keySet()) {
			writer.write(studioFilmCount.get(key).toString() + "\n");
		}
		writer.close();
	}
}

class FilmYear {
	public int year;
	public String parent;
	public int count;
	ArrayList<Film> films;

	public FilmYear(int year, String filmKey, int count) {
		this.year = year;
		this.parent = filmKey;
		this.count = count;
		this.films = new ArrayList<Film>();
	}

	@Override
	public String toString() {
		if(films.size() != count) throw new RuntimeException("Film count mismatch: " + films.size() + " vs " + count + " for " + parent + " in " + year);
		return this.parent + "," + (year > 0 ? year + "," : "") + this.count + "," + this.getColourRaio() + "," + this.getAverageFilmReelLength();
	}

	public void addFilm(Film film) {
		this.films.add(film);
	}

	public double getColourRaio() {
		int colourCount = 0;
		int hasColour = count;
		for(Film film : this.films) {
			if(film.colour.equalsIgnoreCase("Colour") || film.colour.equalsIgnoreCase("Color")) {
				colourCount++;
			}else if(film.colour.isBlank()) {
				hasColour--;
			}
		}
		return (double)colourCount / (double)hasColour;
	}

	public double getAverageFilmReelLength() {
		int totalLength = 0;
		int hasLength = 0;
		for(Film film : this.films) {
			if(film.reels > 0) {
				totalLength += film.reels;
				hasLength++;
			}
		}
		return (double)totalLength / (double)hasLength;
	}
}