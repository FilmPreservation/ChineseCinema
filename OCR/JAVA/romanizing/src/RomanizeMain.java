package OCR.JAVA.romanizing.src;

/* 
 * Library:
 * -Pinyin4j
 * https://pinyin4j.sourceforge.net
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.sourceforge.pinyin4j.*;
import net.sourceforge.pinyin4j.format.*;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class RomanizeMain {
	public static String inputPath = "OCR_CHRONICAL.csv", outputPath = "OCR_PINYIN.csv";
	public static String listPath = "Romanized_List.csv";
	public static boolean numeric_accent = false;

	public static void main(String[] args) throws IOException
	{
		//romanize();
		GenerateRomanizedList();
	}

	protected static String romanizeStandardHanName(String name) {
		char[] chineseCharacters = name.toCharArray();
		String output = "";

		int surname = 0; //0-Surname, 1-First letter in give name, 2- other chars in name
		for(char c : chineseCharacters){
			String p = chineseCharToPinyin(c);
			if(p.isBlank())
				p = c + "";
			else
				p = p.trim();

			if(surname == 0) {
				if(Character.isLetter(p.charAt(0)))
					p = p.substring(0, 1).toUpperCase() + p.substring(1);
				p += " ";
				surname = 1;
			}else if(surname == 1) {
				if(Character.isLetter(p.charAt(0)))
					p = p.substring(0, 1).toUpperCase() + p.substring(1);
				surname = 2;
			}
			output += p;
		}
		return output;
	}

	protected static void romanize() throws IOException {
		File file = new File(inputPath);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath, true));

		String line = "";
		while((line = br.readLine()) != null){
			String py = "";

			char[] chineseCharacters = line.toCharArray();
			for(char c : chineseCharacters){
				String p = chineseCharToPinyin(c);
				if(!p.isBlank()) {
					py += p;
				}else {
					py += c;
				}
			}

			writer.write(py + '\n');
		}

		br.close();
		writer.close();
	}

	private static String chineseCharToPinyin(char chineseCharacter)
	{
		HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();

		outputFormat.setToneType(numeric_accent ? HanyuPinyinToneType.WITH_TONE_NUMBER : HanyuPinyinToneType.WITHOUT_TONE);
		outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
		outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);

		String[] pinyinArray = null;
		try
		{
			pinyinArray = PinyinHelper.toHanyuPinyinStringArray(chineseCharacter, outputFormat);
		} catch (BadHanyuPinyinOutputFormatCombination e1)
		{
			e1.printStackTrace();
		}
		String outputString = concatPinyinStringArray(pinyinArray);

		return outputString;
	}

	private static String concatPinyinStringArray(String[] pinyinArray)
	{
		StringBuffer pinyinStrBuf = new StringBuffer();

		if ((null != pinyinArray) && (pinyinArray.length > 0))
		{
			/*for (int i = 0; i < pinyinArray.length; i++)
			{
				pinyinStrBuf.append(pinyinArray[i]);
				pinyinStrBuf.append(System.getProperty("line.separator"));
			}*/
			pinyinStrBuf.append(pinyinArray[0]);
		}
		String outputString = pinyinStrBuf.toString() + " ";
		return outputString;
	}

	public static void GenerateRomanizedList() throws IOException {
		File file = new File(inputPath);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		BufferedWriter writer = new BufferedWriter(new FileWriter(listPath, true));

		String line = "";
		writer.write("Romanized,Title,Year\n");
		boolean firstRow = true;

		while((line = br.readLine()) != null){
			if(firstRow) {
				firstRow = false;
				continue;
			}

			String[] entries = line.split(",");
			if(entries.length < 2) continue;
			int num = 0;
			String year = "";

			for (String title : entries) {
				if(num == 0) {
					num++;
					year = title.substring(0, title.length() - 1);
					continue;
				}
				String py = "";

				char[] chineseCharacters = title.toCharArray();
				for(char c : chineseCharacters){
					String p = chineseCharToPinyin(c);
					if(!p.isBlank()) {
						py += p;
					}else {
						py += c;
					}
				}
				if(py.endsWith(" ")) py = py.substring(0, py.length() - 1);
				writer.write(py + ',' + title + ',' + year + '\n');
				num++;
				System.out.println(year + ":" + num);
			}
		}

		br.close();
		writer.close();	
	}
}