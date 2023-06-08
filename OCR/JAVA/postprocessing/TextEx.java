package OCR.JAVA.postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TextEx {
	private static final String OCR_PATH = "OCR.txt", TAR_PATH = "target.csv", CONV_PATH = "converted.csv";
	private static int allocatedEntries = 0;
	private static ArrayList<Entry.Temp> temps = new ArrayList<Entry.Temp>();

	public static void main(String[] args) {
		try {
			splitEntries();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void removePageNumber() throws IOException {
		File file = new File(OCR_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		reader.close();
	}

	public static void splitEntries() throws IOException {
		File file = new File(OCR_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		int num = 1, lineNum = 0;
		int entrySt = 0;//0-Before first '(', 1-before first ')', 2-After ')'
		String brackted = "", engTitle = "";

		while((line = reader.readLine()) != null) {
			lineNum++;
			if(line.contains(".png")) {
				continue;
			}

			if(line.startsWith(num + ".") || line.startsWith(num + ",")) {
				System.out.println("Entry #" + num + " at " + lineNum);
				num++;

				if(brackted.isBlank())
					warnBracket(num);
				
				entrySt = 0;
				brackted = "";
				engTitle = "";
			}else if(line.startsWith(num + "/")) {
				System.out.println("Entry #" + num + "&" + (num+1) + " at " + lineNum);
				System.out.println(line);
				System.out.println("Special format found, wait for input...");
				System.in.read();
				num += 2;

				if(brackted.isBlank())
					warnBracket(num);

				entrySt = 0;
				brackted = "";
				engTitle = "";
			}

			if(entrySt == 1) {
				brackted = brackted.concat(" ");
				if(line.contains(")")) {
					entrySt = 2;
					brackted = brackted.concat(line.substring(0, line.lastIndexOf(")") + 1));
					showBracket(brackted, engTitle);
				} else {
					brackted = brackted.concat(line);
				}
			}else if(entrySt == 0){
				if(line.contains("(")) {
					engTitle = engTitle.concat(line.substring(0, line.indexOf("(")));
					engTitle = engTitle.trim();
					checkEnglishEstimation(engTitle, num);

					if(line.contains(")")) {
						entrySt = 2;
						brackted = (line.substring(line.indexOf("("), line.lastIndexOf(")") + 1));
						showBracket(brackted, engTitle);
					} else {
						entrySt = 1;
						brackted = brackted.concat(line.substring(line.indexOf("("), line.length()));
					}
				}else {
					engTitle = engTitle.concat(line + " ");
				}
			}
		}

		reader.close();
		System.out.println(allocatedEntries + " entries allocated in target list.");
		convertTargetList(temps);
	}

	private static void showBracket(String bracketed, String translatedTitle) throws IOException {
		System.out.println(bracketed);
		/*System.out.println("Check pinyin and input...");
		System.in.read();*/
		checkLineNumberInTarget(bracketed, translatedTitle);
	}

	private static void warnBracket(int num) throws IOException {
		if(num < 3) return;
		System.out.println("No bracket found for entry #" + (num - 2) + ", input to continue...");
		System.in.read();
	}

	private static void checkEnglishEstimation(String content, int num) throws IOException {
		if(num < 2) return;
		//if(!isAlpha(content)) {
		if(content.isBlank()) {
			System.out.println("Error in estimated English translation \"" + content + "\"\n found for entry #" + (num - 1) + ", input to continue...");
			System.in.read();
		}else {
			System.out.println("Est.Eng: " + content);
		}
	}

	private static void checkLineNumberInTarget(String romanized, String translatedTitle) throws IOException {
		if(romanized.length() < 3) return;
		String pinyin = romanized.substring(1, romanized.length() - 1);

		File file = new File(TAR_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		int lineNum = 0;

		while((line = reader.readLine()) != null) {
			lineNum++;
			String[] attributes = line.split(",");
			if(attributes.length > 0) {
				if(attributes[0].equalsIgnoreCase(pinyin)) {
					System.out.println("Found in target list at line " + lineNum);
					allocatedEntries++;
					temps.add(new Entry.Temp(lineNum, 3, translatedTitle));
					//System.in.read();
					break;
				}
			}
		}

		reader.close();
	}

	public static boolean isExpectedCharInTitle(String name) {
		char[] chars = name.toCharArray();
		for (char c : chars) {
			if(!(Character.isLetter(c) || c == ' ' || c == ',' || c == '.' || c == '’' || c == '\'' || Character.isDigit(c))) {
				return false;
			}
		}
		return true;
	}

	public static void convertTargetList(ArrayList<Entry.Temp> temps) throws IOException {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		File tar = new File(TAR_PATH), conv = new File(CONV_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(tar));
		String line;
		while((line = reader.readLine()) != null) {
			entries.add(new Entry(line));
		}
		reader.close();

		for (Entry.Temp temp : temps) {
			entries.get(temp.estRow - 1).tryWritingAttribute(temp.estCol, temp.content);
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(conv));
		for (Entry entry : entries) {
			writer.append(entry.toString() + '\n');
		}
		writer.close();
	}

}