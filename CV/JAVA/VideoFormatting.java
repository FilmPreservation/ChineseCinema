package CV.JAVA;

import java.io.File;
import java.io.IOException;

//This script converts downloaded source videos to mp4 format
public class VideoFormatting {
	private static final String TEMP_VIDEO_DIR = "../Temp";

	public static void main(String[] args) {
		File dir = new File(TEMP_VIDEO_DIR);
		File[] files = dir.listFiles();
		try{
			for (File file : files) {
				if(file.getName().equals(".DS_Store")) continue;

				if(file.getName().endsWith(".mp4")) {
					//System.out.println("File: " + file.getName() + " is already in mp4 format");
					continue;
				}

				//Rename to .mp4 if not
				File rename = new File(file.getAbsolutePath() + ".mp4");

				if (rename.exists())
					throw new java.io.IOException("File exists: " + rename.getName());
				boolean success = file.renameTo(rename);
				if (!success) {
					throw new java.io.IOException("Could not rename file: " + file.getName() + " to " + rename.getName());
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
