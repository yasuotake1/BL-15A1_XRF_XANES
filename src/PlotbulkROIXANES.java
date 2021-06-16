import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import java.io.*;
import java.util.*;

import ij.io.OpenDialog;
import ij.plugin.frame.RoiManager;
/*
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import ij.ImagePlus;
import ij.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.plugin.PlugIn;

import ij.process.ImageStatistics;
import ij.WindowManager.*;
import ij.gui.*;
import ij.gui.Plot;

*/
public class PlotbulkROIXANES implements PlugIn {

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}

	public void method(ImagePlus imp, String dir, String eneFile){

//		ImagePlus imp = WindowManager.getCurrentImage();
		int[] Dimensions = imp.getDimensions();

		int nSlices = Dimensions[3];

		ImageStatistics stats1;

		if(nSlices == 1){
			IJ.error("Current image is not a XANES map! ");
		}
		double[] arrInt = new double[nSlices];

		for(int i=0; i<nSlices; i++) {
			imp.setSlice(i + 1);
			ImagePlus impp = WindowManager.getCurrentImage();
			stats1 = impp.getStatistics();
			arrInt[i] = stats1.mean;
		}
		
		File file = new File(dir + eneFile);
		if(!file.exists()) {
			OpenDialog od = new OpenDialog("Choose an energy list file...");
			dir = od.getDirectory();
			if (dir==null){
				return;
			}
			eneFile = od.getFileName();
		}
		
		ArrayList<String> rows = new ArrayList<String>();

		try{	
			BufferedReader br = new BufferedReader(new FileReader(dir + eneFile));
			String line;
			while ((line = br.readLine()) != null) {
				rows.add(line);
			}
			br.close();

	    } catch(IOException e) {
	            System.err.println(e.getMessage());
	    }

		boolean is9809 = (rows.get(0)).trim().startsWith("9809");
		if(is9809){
			do{
				rows.remove(0);
			}while(!(rows.get(0)).startsWith("    Offset"));
		}
		float[] angles = new float[rows.size() - 1];
		float[] energies = new float[rows.size() - 1];

		for(int i=0; i<energies.length; i++) {
			if(is9809){
				angles[i] = Float.parseFloat((rows.get(i + 1)).substring(0, 10).trim());
				energies[i] = (float) (12398.52 / (2 * 3.13551 * Math.sin(angles[i] / 180 * Math.PI)));
			} else {
				String[] columns = (rows.get(i + 1)).split(",");
				energies[i] = Float.parseFloat(columns[0]);
			}
			if(Float.isNaN(energies[i])){
				IJ.error("Invalid energy list file.");
			}
		}

		double[] energiesInt = new double[energies.length];
		for(int i=0; i<energies.length; i++) {
			energiesInt[i] = (double)energies[i];
		}

		if(imp.getRoi()!=null) {
			IJ.run("ROI Manager...");
			RoiManager rm = RoiManager.getInstance();
			rm.addRoi(imp.getRoi());
		}
		Plot p = new Plot("Bulk/ROI XANES", "Photon energy (eV)", "Intensity");
		p.add("LINE", energiesInt, arrInt);
		p.show();

	}
}

