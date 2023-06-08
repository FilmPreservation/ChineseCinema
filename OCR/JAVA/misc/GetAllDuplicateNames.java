package OCR.JAVA.misc;

import java.io.IOException;
import java.util.ArrayList;

import OCR.JAVA.Film;

public class GetAllDuplicateNames {

	public static void main(String[] args) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		ArrayList<String> keys = new ArrayList<String>();

		for (Film f : films) {
			if(!keys.contains(f.key)) {
				keys.add(f.key);
			} else {
				System.out.println(f.key);
			}
		}
		keys.clear();

		for(Film f : films) {
			if(!keys.contains(f.title)) {
				keys.add(f.title);
			} else {
				System.out.println(f.title);
			}
		}
		keys.clear();
	}
	
}
