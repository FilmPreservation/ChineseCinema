package OCR.JAVA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Studio {
	public static final String STUDIOS_LIST = "OCR/studios.csv",
		TRANSLATED_STUDIOS_LIST = "OCR/JAVA/romanizing/translated-studios.csv";

	public String name;
	public String category;
	private int orderInList; //Recorded to be Hashcode
	public String chn, eng;
	
	public Studio(String name, String category, int orderInList, String chn, String eng) {
		this.name = name;
		this.category = category;
		this.orderInList = orderInList;
		this.chn = chn;
		this.eng = eng;
	}

	public Studio(String name) throws IOException {
		this.name = name;
		getCategoryAndOrder();
		getBilingualNames();
	}

	public static String getStudioCategory(String studio) throws IOException {
		File f = new File(STUDIOS_LIST);
		BufferedReader r = new BufferedReader(new FileReader(f));
		String l = "";

		while ((l = r.readLine()) != null) {
			if(l.split(",")[0].equals(studio)) {
				String category = l.split(",")[1];
				r.close();
				return category;
			}
		}
		r.close();
		throw new IOException("Unexpected studio found: \"" + studio + "\" in the film metadata.");
	}

	private void getCategoryAndOrder() throws IOException {
		File f = new File(STUDIOS_LIST);
		BufferedReader r = new BufferedReader(new FileReader(f));
		String l = "";
		int line = 0;

		while ((l = r.readLine()) != null) {
			if(l.split(",")[0].equals(name)) {
				this.category = l.split(",")[1];
				this.orderInList = line;
				r.close();
				return;
			}
			line++;
		}
		r.close();
		throw new IOException("Unexpected studio found: \"" + name + "\" in the film metadata.");
	}

	private void getBilingualNames() throws IOException {
		File f = new File(TRANSLATED_STUDIOS_LIST);
		BufferedReader r = new BufferedReader(new FileReader(f));
		String l = "";

		while ((l = r.readLine()) != null) {
			String[] att = l.split(",");
			if(att[0].equals(name)) {
				this.eng = att[1];
				this.chn = att[2];
				r.close();
				return;
			}
		}
		r.close();
		throw new IOException("Unexpected studio found: \"" + name + "\" in the translated studio data.");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
            return true;
        if (!(obj instanceof Studio))
            return false;

        Studio studio = (Studio)obj;
        return this.name.equals(studio.name);
	}

	@Override
	public int hashCode() {
		return orderInList;
	}
	
}
