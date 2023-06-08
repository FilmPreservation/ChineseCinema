package CV.JAVA;
//This program is used to clear imagaes where the person presented is beyond a wanted threshold

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class ClearPeopleThreshold {
	private static final String SRC = "CV/crowd/found_frames.csv";
	private static final int THRESHOLD = 10;
	
	public static void main(String[] args) throws IOException {
		File source = new File(SRC);
		BufferedReader br = new BufferedReader(new FileReader(source));
		String line = br.readLine();

		String newFile = line + "\n";
		int i = 0;

		while((line = br.readLine()) != null) {
			String[] values = line.split(",");
			String videoName = values[0];
			String frame = values[1];
			int count = Integer.parseInt(values[2]);
			
			if(count < THRESHOLD) {
				String thumb = "CV/thumbnails/cache/" + videoName + "-t" + frame + "_" + count + ".jpg";
				if(new File(thumb).delete()) {
					System.out.println("Deleted " + thumb);
				}else{
					br.close();
					throw new IOException("Failed to delete " + thumb);
				}
				String framePath = "CV/Temp/" + videoName + "-" + frame + "_" + count + ".jpg";
				if(new File(framePath).delete()) {
					System.out.println("Deleted " + framePath);
				}else{
					br.close();
					throw new IOException("Failed to delete " + framePath);
				}
			}else{
				newFile += line + "\n";
			}

			i ++;
			System.out.println(i + "/" + 26572);
		}
		br.close();
		
		System.out.println("Writing new file");
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(source));
		bw.write(newFile);
		bw.close();
	}

}