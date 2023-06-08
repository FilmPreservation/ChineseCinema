package OCR.JAVA;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONConverter {
	public ArrayList<Film> films;

	public JSONConverter() {
		this.films = new ArrayList<Film>();
	}

	public void removeCodeFromFilmography() {
		for (Film film : films) {
			if(Character.isDigit(film.translated.charAt(0))) {
				if(film.translated.contains(". "))
					film.translated = film.translated.substring(film.translated.indexOf(". ") + 2);
				else {
					System.out.println(film.key + ", mal-formattd code.");
					try {
						System.in.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if(film.translated.contains("[") || film.translated.contains("]")) {
				if(film.translated.contains("]") && film.translated.contains("[")) {
					String leading = film.translated.substring(film.translated.indexOf("[") + 1, film.translated.indexOf("[") + 5);
					if(!(leading.equals("Part") || leading.substring(0, 2).equals("19"))) {
						film.translated = film.translated.substring(film.translated.indexOf("[") + 1, film.translated.lastIndexOf("]"));
					}
				} else {
					System.out.println(film.key + ", mal-formattd bracket.");
					try {
						System.in.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			film.translated = film.translated.replaceAll("\\{COMMA\\}", ", ");
			film.plot = film.plot.replaceAll("\\{QUOTE\\}", "“");
			film.plot = film.plot.replaceAll("\\{LINE_CUT\\}", "\n");
		}
	}
	
	public static void main(String[] args) {
		try {
			writeMetadataJSON();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeMetadataJSON() throws IOException {
		JSONConverter jsonOutput = new JSONConverter();
		jsonOutput.films = Film.initAllFilms();
		jsonOutput.removeCodeFromFilmography();
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonOutput);
		BufferedWriter writer = new BufferedWriter(new FileWriter("metadata-all.json"));
		writer.write(json);
		writer.close();
	}

}