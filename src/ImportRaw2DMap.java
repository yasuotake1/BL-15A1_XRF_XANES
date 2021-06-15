import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.io.OpenDialog;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ImportRaw2DMap implements PlugIn {
	FileWriter fw;
	
	public void run(String arg) {
		String[] labels;
		XRFXANESProps prop = setupMapping.ReadProps();

		OpenDialog od = new OpenDialog("Select raw 2D mapping data file.", prop.defaultDir, "");
		if(od.getPath() == null)
			return;
		String strPreview = "";
		try {
			BufferedReader br = Files.newBufferedReader(Paths.get(od.getPath()));
			strPreview = br.readLine();
			labels = strPreview.split(",");
			for(int i=0; i<labels.length; i++) {
				labels[i] = labels[i].trim();
			}
			strPreview += "\n";
			for(int i=1; i<5; i++) {
				strPreview += br.readLine() + "\n";
			}
			br.close();
		} catch(IOException e) {
			return;
		}
		boolean[] listUse = new boolean[labels.length];
		int offset = listUse.length - prop.listUse.length;
		for(int i=0; i<prop.listUse.length; i++) {
			listUse[i + offset] = prop.listUse[i];
		}
		
		GenericDialog gd = new GenericDialog("Raw 2D mapping data -> Text image");
		gd.addMessage("Content of " + od.getFileName() + ":");
		gd.addTextAreas(strPreview, null, 3, 90);
		gd.addChoice("Axis X: ", labels, labels[0]);
		gd.addChoice("Axis Y: ", labels, labels[1]);
		gd.addCheckboxGroup(2, 20, labels, listUse);
		gd.addCheckbox("Convert all imagestack files", false);
		gd.showDialog();

		if (gd.wasCanceled())
			return;
		
		int idxX = gd.getNextChoiceIndex();
		int idxY = gd.getNextChoiceIndex();
		for(int i=0; i<listUse.length; i++) {
			listUse[i] = gd.getNextBoolean();
		}
		boolean convAll = gd.getNextBoolean();

		for(int i=0; i<prop.listUse.length; i++) {
			prop.listUse[i] = listUse[i + offset];
		}
		prop.defaultDir = od.getDirectory();
		setupMapping.WriteProps(prop);
		
		int countSrc = 0;
		int countTgt = 0;
		String strScanInfo = "";
		if(convAll) {
			String basePath = od.getPath().substring(0, od.getPath().length() - 3);
			String currentPath = basePath + String.format("%03d", countSrc + 1);
			File f = new File(currentPath);
			while(f.exists()) {
				if(DataTable.Assign(currentPath, idxX, labels[idxX], idxY, labels[idxY])) {
					countSrc++;
					
					strScanInfo = DataTable.strScanInfo;
					for(int i=0; i<labels.length; i++) {
						if(listUse[i]) {
							writeTextFile(currentPath + "_" + labels[i] + ".txt", DataTable.getSpreadSheetString(i, ","));
							strScanInfo += "\n\"" + currentPath + "_" + labels[i] + ".txt\"";
							countTgt++;
						}
					}
					
					writeTextFile(currentPath + "_ScanInfo.txt", strScanInfo);

					IJ.showStatus("Converted " + Integer.toString(countSrc) + " file(s) into " + Integer.toString(countTgt) + " text image(s).");			
					
				} else {
					IJ.error("Invalid raw data file.");
				}
				

				currentPath = basePath + String.format("%03d", countSrc + 1);
				f = new File(currentPath);
			}
			
		} else {
			if(DataTable.Assign(od.getPath(), idxX, labels[idxX], idxY, labels[idxY])) {
				countSrc = 1;
				
				strScanInfo = DataTable.strScanInfo;
				for(int i=0; i<labels.length; i++) {
					if(listUse[i]) {
						writeTextFile(od.getPath() + "_" + labels[i] + ".txt", DataTable.getSpreadSheetString(i, ","));
						strScanInfo += "\n\"" + od.getPath() + "_" + labels[i] + ".txt\"";
						countTgt++;
					}
				}
				
				writeTextFile(od.getPath() + "_ScanInfo.txt", strScanInfo);
			} else {
				IJ.error("Invalid raw data file.");
			}
			
			IJ.showStatus("Converted " + Integer.toString(countSrc) + " file(s) into " + Integer.toString(countTgt) + " text image(s).");			
			
		}
	}
	
	void writeTextFile(String path, String content) {
		try {
			fw = new FileWriter(path);
			fw.write(content);
			fw.close();
		} catch (IOException e) {
			IJ.error("Failed to write a text file.");
		}			

		return;
	}
}
