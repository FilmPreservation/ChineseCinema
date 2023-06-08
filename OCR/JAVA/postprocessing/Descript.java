package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Descript {
	public static final String TRAN_PATH = "translated.csv", DES_PATH = "descripted.csv", OCR_PATH = "OCR-DES.txt";
	private static ArrayList<Entry> entries = new ArrayList<Entry>();
	private static ArrayList<TempDes> temps = new ArrayList<TempDes>();
	
	public static void main(String[] args) throws IOException {
		readAllEntryCodes();
		CheckArrayList();
		System.out.println("Entries checked, input to continue...");
		System.in.read();
		appendDescription();
		AppendDescriptionToTranslatedEntries();
	}

	public static void readAllEntryCodes() throws IOException {
		File file = new File(TRAN_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = "";
		int withCode = 0;
		while((line = reader.readLine()) != null) {
			Entry entry = new Entry(line, true);
			entries.add(entry);

			//Check if starts with code
			char firstChara = entry.translated.charAt(0);
			if(Character.isDigit(firstChara)) {
				if(!entry.translated.contains(".")) {
					System.out.println("Ill-formatted code found for " + entry.translated);
					System.in.read();
				}
			}

			//Check if code is properly formatted
			if(entry.translated.contains(".")) {
				System.out.println(entry.translated);
				withCode++;
				String[] temp = entry.translated.split("\\.");

				try {
					int code = Integer.parseInt(temp[0]);
					entry.code = code;
				}catch(Exception e) {
					System.out.println("Unexpected period detected, not following an entry code...");
					System.in.read();
				}
				if(temp.length != 2) {
					System.out.println("Unexpected period detected, " + (temp.length-1) + " in one entry...");
					System.in.read();
				}
			}
		}
		System.out.println("Number included in Book: " + withCode);
		reader.close();
	}

	public static void CheckArrayList() throws IOException {
		int withCode = 0, withoutCode = 0;
		for (Entry entry : entries) {
			if(entry.code > 0) {
				withCode++;
			}else if(entry.code < 0) {
				withoutCode++;
			}else {
				System.err.println("Code cannot be 0.");
				System.in.read();
			}
		}
		System.out.println(withCode + " entires with code, " + withoutCode + " entires without code, "
			+ entries.size() + " in total, 657 expected.");
	}

	public static void appendDescription() throws IOException {
		File file = new File(OCR_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		int num = 1;
		int entrySt = 0;//0-Before first '(', 1-before first ')', 2-After ')'
		String brackted = "", engTitle = "", des = "";

		while((line = reader.readLine()) != null) {
			if(line.contains(".png") || line.isBlank()) {
				continue;
			}

			if(line.startsWith(num + ".") || line.startsWith(num + ",")) {
				//System.out.println("Entry #" + num + " at " + lineNum);
				num++;

				String estTitle = engTitle + " " + brackted;
				estTitle = estTitle.trim();
				//System.out.println("Estimated Title: " + estTitle);
				//System.out.println("Description:\n" + des + "\n");
				//System.in.read();
				TempDes d = new TempDes(des, estTitle, num-2);
				temps.add(d);
				if(d.code != temps.size() - 1) {
					System.err.println("Error size");
					System.in.read();
				}
				
				entrySt = 0;
				brackted = "";
				engTitle = "";
				des = "";
			}else if(line.startsWith(num + "/")) {
				//System.out.println("Entry #" + num + "&" + (num+1) + " at " + lineNum);
				num += 2;
				temps.add(new TempDes("skipped", "(skipped)", 157));
				temps.add(new TempDes("skipped", "(skipped)", 158));
				entrySt = 0;
				brackted = "";
				engTitle = "";
				des = "";
			}

			if(entrySt == 1) {
				brackted = brackted.concat(" ");
				if(line.contains(")")) {
					entrySt = 2;
					brackted = brackted.concat(line.substring(0, line.lastIndexOf(")") + 1));
					if(line.lastIndexOf(")") + 1 < line.length())
						des = des.concat(line.substring(line.lastIndexOf(")") + 1, line.length()));
				} else {
					brackted = brackted.concat(line);
				}
			}else if(entrySt == 0){
				if(line.contains("(")) {
					engTitle = engTitle.concat(line.substring(0, line.indexOf("(")));
					engTitle = engTitle.trim();

					if(line.contains(")")) {
						entrySt = 2;
						brackted = (line.substring(line.indexOf("("), line.lastIndexOf(")") + 1));
						if(line.lastIndexOf(")") + 1 < line.length())
							des = des.concat(line.substring(line.lastIndexOf(")") + 1, line.length()));
					} else {
						entrySt = 1;
						brackted = brackted.concat(line.substring(line.indexOf("("), line.length()));
					}
				}else {
					engTitle = engTitle.concat(line + " ");
				}
			}else if(entrySt == 2) {
				des = des.concat("\n" + line);
			}
		}
		reader.close();

		/*File file2 = new File(DES_PATH);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file2));
		writer.append("Code,Est.Title,Description\n");
		for (TempDes d : temps) {
			writer.append(d.code+","+d.estTitle+","+d.content+"\n");
		}
		writer.close();*/
	}

	public static class TempDes {
		String content, estTitle;
		int code;

		public TempDes(String content, String estimatedTitle, int code) {
			this.content = Descript.formatForCSV(content);
			this.estTitle = estimatedTitle;
			if(estTitle.contains(",")) {
				estTitle = "\"" + estTitle + "\"";
			}
			this.code = code;
		}
	}

	public static String formatForCSV(String input) {
		//String output = input.replace(',', '、');
		//output = output.replace('\n', '。');
		String output = input.replace('\"', '“');
		output = "\"" + output + "\"";
		return output;
	}

	public static void AppendDescriptionToTranslatedEntries() throws IOException {
		File file = new File(DES_PATH);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		for (Entry entry : entries) {
			if(entry.code > 0) {
				TempDes d = temps.get(entry.code);
				System.out.println("Checking Entry #" + entry.code + ": " + entry.title);
				if(d.code != entry.code) {
					System.err.println("Cannot map entry code with description!");
					System.in.read();
				}
				/*try {
					String yearString = d.content.substring(2, 6);
					int year = Integer.parseInt(yearString);
					int eYear = Integer.parseInt(entry.year);
					if(year != eYear) throw new Exception("Year corrputed.");
				}catch(Exception e) {
					e.printStackTrace();
					System.in.read();
				}*/
				entry.description = d.content;
			}
			writer.append(entry.toString() + "\n");
		}

		writer.close();
	}

}
