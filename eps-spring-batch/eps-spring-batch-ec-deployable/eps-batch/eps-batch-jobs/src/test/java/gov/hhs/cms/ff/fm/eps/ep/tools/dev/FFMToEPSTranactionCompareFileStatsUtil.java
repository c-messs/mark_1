package gov.hhs.cms.ff.fm.eps.ep.tools.dev;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;


/**
 * @author j.radziewski
 * 
 * Utility class to display the monthly FFM Insurance Plan Policies (IPPs) and
 * policy transactions in EPS.  Displays the first 3 and last 3 lines of each
 * file to visually confirm the start and end of each file are the same.  
 * Especially useful when files are so large and cannot be open by an editor.
 *
 */
public class FFMToEPSTranactionCompareFileStatsUtil {

	private static String PATH_FFM = "/Prototype834files/FFMToEPS/ffm";
	private static String PATH_EPS = "/Prototype834files/FFMToEPS/eps";
	
	public static void main(String[] args) {

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	
		System.out.println("\nFFMToEPSTranactionCompareFileStatsUtil: STARTED\n- - - - - - - - - - - - - - - - - - - - - - - \n\nComparing line counts of both the EPS and FFM file.");

		File dirEPS = new File(PATH_EPS);
		File dirFFM = new File(PATH_FFM);

		File[] epsFiles = dirEPS.listFiles();
		File fileEPS = null;

		File[] ffmFiles = dirFFM.listFiles();
		File fileFFM = null;

		String lineEPS = null;
		String lineFFM = null;
		String prvLineEPS = null;
		String prvLineFFM = null;

		HashMap<String, String> fileMapEPS = new HashMap<String, String>();
		HashMap<String, String> fileMapFFM = new HashMap<String, String>();
		int mapSizeEPS = 0;
		int mapSizeFFM = 0;

		int cntEPSLine = 0;
		int cntFFMLine = 0;
		int cntEPSLnBrk = 0;
		int cntFFMOtherLine = 0;

		BufferedReader br = null;

		if (epsFiles.length > 0) {
			fileEPS = epsFiles[0];
			try {
				System.out.println("\n - - - EPS - - -\n\nReading EPS file: " + fileEPS.getName());
				br = new BufferedReader(new FileReader(fileEPS));
				lineEPS = br.readLine();
				// EPS file does not have header row.
				while (lineEPS != null) {
					cntEPSLine++;
					if (!lineEPS.isEmpty()) {
						fileMapEPS.put(String.valueOf(cntEPSLine), lineEPS);
						prvLineEPS = lineEPS;
					} else {
						cntEPSLnBrk++;
					}
					lineEPS = br.readLine();
				}
				mapSizeEPS = fileMapEPS.size();
				System.out.println("\nEPS total Policy transaction line item count: " + nf.format(mapSizeEPS) + "\n");
				System.out.println("First 3 of " + nf.format(mapSizeEPS) + " lines of EPS file:\n");
				for (int i = 1; i <= 3; ++i) {
					System.out.println("\tLine " + nf.format(i) + ") " + fileMapEPS.get(String.valueOf(i)));
				}
				System.out.println("\nLast 3 of " + nf.format(fileMapEPS.size()) + " lines of EPS file:\n");

				for (int i = mapSizeEPS - 2; i <= mapSizeEPS; ++i) {
					System.out.println("\tLine " + nf.format(i) + ") " + fileMapEPS.get(String.valueOf(i)));
				}

			} catch (IOException ioEx) {
				System.out.println("\tOops, no file or something: " + ioEx.getMessage());
			} catch (Exception ex) {

				System.out.println("\tOops: \nPrevious EPS Line: " + prvLineEPS + "\n" + nf.format(cntEPSLine  + cntEPSLnBrk) + ") lineEPS: " + lineEPS + 

						"\n" + nf.format(cntFFMLine) + ") lineFFM: " + lineFFM);
				System.out.println(ex.getMessage());
			}
		} else {
			System.out.println("\n - - - EPS - - -\n\nReading EPS file: No file to read.");
		}
		
		if (ffmFiles.length > 0) {
			fileFFM = ffmFiles[0];

		try{
			System.out.println("\n\n - - - FFM - - -\n\nReading FFM file: " + fileFFM.getName());
			br = new BufferedReader(new FileReader(fileFFM));
			lineFFM = br.readLine();
			if (lineFFM.indexOf(":") == -1) {
				System.out.println("Excluding FIRST row from FFM file:\t" + lineFFM);
				lineFFM = br.readLine();
			}
			while (lineFFM != null) {
				cntFFMLine++;
				if (!lineFFM.isEmpty()) {
					if (lineFFM.indexOf("LastModified") != -1) {
						cntFFMOtherLine++;
					}
					fileMapFFM.put(String.valueOf(cntFFMLine), lineFFM);
					prvLineFFM = lineFFM;
				} else {
					cntFFMOtherLine++;
				}
				lineFFM = br.readLine();
			}
			mapSizeFFM = fileMapFFM.size();
			int totalFFMIPPs = mapSizeFFM - cntFFMOtherLine;
			System.out.println("\nFFM total IPP line item count: " + nf.format(totalFFMIPPs) + " (excluding " + cntFFMOtherLine + " blank and/or footer lines)\n");
			System.out.println("First 3 of " + nf.format(mapSizeFFM) + " lines of FFM file:\n");
			for (int i = 1; i <= 3; ++i) {
				System.out.println("\tLine " + nf.format(i) + ") " + fileMapFFM.get(String.valueOf(i)));
			}
			System.out.println("\nLast 4 of " + nf.format(mapSizeFFM) + " lines of FFM file:\n");
			for (int i = mapSizeFFM - 3; i <= mapSizeFFM; ++i) {
				System.out.println("\tLine " + nf.format(i) + ") " + fileMapFFM.get(String.valueOf(i)));
			}

			System.out.println("\n - - - DIFF - - -\n\nDifference of FFM IPPs to EPS Policy transactions based in file line items: " + 
					(nf.format(totalFFMIPPs) + " - " + nf.format(mapSizeEPS) + " = " + nf.format(totalFFMIPPs - mapSizeEPS)));


		} catch (IOException ioEx) {
			System.out.println("\tOops, no file or something: " + ioEx.getMessage());
		} catch (Exception ex) {

			System.out.println("\tOops: \nPrevious FFM Line: " + prvLineFFM + "\n" + nf.format(cntEPSLine  + cntEPSLnBrk) + ") lineEPS: " + lineEPS + 

					"\n" + nf.format(cntFFMLine) + ") lineFFM: " + lineFFM);
			System.out.println(ex.getMessage());
		}
		} else {
			System.out.println("\n - - - FFM - - -\n\nReading FFM file: No file to read.");
		}


		System.out.println("\n- - - - - - - - - END - - - - - - - - -");
	}

}
