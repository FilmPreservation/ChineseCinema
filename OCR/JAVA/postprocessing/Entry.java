package OCR.JAVA.postprocessing;

public class Entry {
	public String romanized, title, year, translated, description;
	public int code = -1;

	public Entry(String firstThreeColumns) {
		String[] attributes = firstThreeColumns.split(",", 5);
		this.romanized = attributes[0];
		this.title = attributes[1];
		this.year = attributes[2];
		this.translated = attributes.length > 3 ? attributes[3] : "";
		this.description = attributes.length > 4 ? attributes[4] : "";
	}

	public Entry(String firstFourColumns, boolean includeComma) {
		if(includeComma) {
			if(firstFourColumns.contains("\"")) {
				String[] commas = firstFourColumns.split("\"", 3);
				String[] firstCommas = commas[0].split(",", 4);

				this.romanized = firstCommas[0];
				this.title = firstCommas[1];
				this.year = firstCommas[2];
				this.translated = commas[1];
				String[] thirdCommas = commas[2].split(",");
				this.description = thirdCommas.length > 0 ? thirdCommas[0] : "";
			}else {
				String[] attributes = firstFourColumns.split(",", 5);
				this.romanized = attributes[0];
				this.title = attributes[1];
				this.year = attributes[2];
				this.translated = attributes.length > 3 ? attributes[3] : "";
				this.description = attributes.length > 4 ? attributes[4] : "";
			}
		}else {
			String[] attributes = firstFourColumns.split(",", 5);
			this.romanized = attributes[0];
			this.title = attributes[1];
			this.year = attributes[2];
			this.translated = attributes.length > 3 ? attributes[3] : "";
			this.description = attributes.length > 4 ? attributes[4] : "";
		}
	}

	public void replaceAttribute(int column, String content) {
		if(column == 3) {
			this.translated = content;
		}else if(column == 4) {
			this.description = content;
		}else {
			System.err.println("Can only replace translated title and description.");
		}
	}

	@Override
	public String toString() {
		if(translated.contains(","))
			return romanized + "," + title + "," + year + ",\"" + translated + "\"," + description;
		else
			return romanized + "," + title + "," + year + "," + translated + "," + description;
	}

	public void tryWritingAttribute(int column, String content) {
		if(column == 3 && this.translated.isBlank()) {
			this.translated = content;
		}else if(column == 4 && this.description.isBlank()) {
			this.description = content;
		}else {
			System.err.println("The attribute already exists, failing to write.");
		}
	}

	public static class Temp {
		int estRow, estCol;
		String content;

		public Temp(int row, int column, String content) {
			this.estCol = column;
			this.estRow = row;
			this.content = content;
		}
	}

}
