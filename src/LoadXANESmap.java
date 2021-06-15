import java.io.*;
import java.util.Arrays;

import ij.io.OpenDialog;
import ij.io.DirectoryChooser;
import ij.io.FileSaver;
import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;

import ij.WindowManager.*;
import ij.gui.ProgressBar;
import ij.gui.GenericDialog;
import ij.ImageStack;
import ij.plugin.ContrastEnhancer;
import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImagePlus;
import java.io.File;

public class LoadXANESmap implements PlugIn {

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}

	public String method(String[] listSuffixes, String[] listChName, String[] listNormalizedName, XRFXANESProps prop){

		boolean bRev = false;

		if(prop.stageConf==0){
			bRev = false;
		}else{
			bRev = true;
		}

		IJ.run("Close All");
		
		OpenDialog.setDefaultDirectory(prop.defaultDir);
		DirectoryChooser dc = new DirectoryChooser("Choose directory for XANES mapping dataset...");
		if(dc.getDirectory() == null){
			return null;
		}
		String dirImg = dc.getDirectory();
		prop.defaultDir = dirImg;
		setupMapping.WriteProps(prop);

		String prefix = "";

		String[] listAll = new File(dirImg).list();
		String[] listFiles = new String[listSuffixes.length];
		for(int  i=0; i<listFiles.length; i++) {
			listFiles[i] = "";
		}

		for(int i=0; i<listSuffixes.length; i++) {
			if((listSuffixes[i]).length() > 0) {
				for(int j=0; j<listAll.length; j++) {
					if((listAll[j]).endsWith(listSuffixes[i])) {
						if(listFiles.length == 0){
							listFiles[i] = listAll[j];
						}
						else{
							listFiles[i] = listFiles[i] + "," + listAll[j];
						}
					}
				}
			}
		}

		int[] arrCount = new int[listFiles.length];
		for(int i=0; i<listFiles.length; i++) {
			arrCount[i] = listFiles[i].length();

		}
		int max1 = arrCount[0];
		for (int i = 1; i < arrCount.length; i++) {
		    int v = arrCount[i];
		    if (v > max1) {
		        max1 = v;
		    }
		}
		if(max1 == 0){
			IJ.error("No files to load.");
		}
		String FileTitle = "";

		double[] scale = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};

		for(int i=0; i<listSuffixes.length; i++) {

			if(listSuffixes.length > 0 && listFiles.length > 0) {

				if(listFiles[i] != ""){

					String[] sublist = (listFiles[i]).split(",",0);
					int[] Subsublist = new int[sublist.length];

					for(int h=1; h<sublist.length; h++){

							sublist[h]=(sublist[h]).replace(listSuffixes[i], "");
							int StrIndex = (sublist[h]).lastIndexOf("_");
							int StrLength = (sublist[h]).length();
							Subsublist[h] = Integer.parseInt((sublist[h]).substring(StrIndex+1, StrLength));
							FileTitle = (sublist[h]).substring(0, StrIndex+1);

					}
					Subsublist[0] = 0;

					Arrays.sort(Subsublist);

					for(int k=1; k<sublist.length; k++){

						sublist[k] = FileTitle + String.format("%03d", Subsublist[k]) + listSuffixes[i];
					}
					
					prefix = FileTitle.replace("_qscan_", "");
					if(Double.isNaN(scale[0]) && Double.isNaN(scale[1]) && Double.isNaN(scale[2]) && Double.isNaN(scale[3]))
						scale = setupMapping.getScanInfo(dirImg, prefix + "_qscan_001", prop);
					ImageStack stack = new ImageStack();
					TextReader tr = new TextReader();
					
					for(int j=1; j<sublist.length; j++) {
						String file = dirImg + sublist[j];
						ImageProcessor impX = tr.open(file);
						if(j==1) {
							stack = new ImageStack(impX.getWidth(), impX.getHeight());
						}
						stack.addSlice(impX);
					}
					ImagePlus imp0 = new ImagePlus(prefix + "_" + listChName[i], stack);
					imp0.show();
					if(!bRev){
						IJ.run(imp0,"Flip Horizontally", "stack");
					}
					IJ.run(imp0,"Flip Vertically", "stack");
					if(!Double.isNaN(scale[0]) && !Double.isNaN(scale[1]) && !Double.isNaN(scale[2]) && !Double.isNaN(scale[3])){
						IJ.run("Properties...", "unit=" + prop.scaleConf + " pixel_width=" + scale[0] + " pixel_height=" + scale[1] + " origin=" + scale[2] + "," + scale[3]);
					} else {
						IJ.run("Properties...", "unit=pixel pixel_width=1 pixel_height=1 origin=0,0");
					}
					IJ.run("Set... ", "zoom=" + prop.zoom);
					IJ.run("Scale to Fit", "");
				}
			}
		}

  		String window0 = prefix + "_" + listChName[0];
   		String window1 = prefix + "_" + listChName[1];

		ImagePlus img0 = WindowManager.getImage(prefix + "_" + listChName[0]);
		ImagePlus img1 = WindowManager.getImage(prefix + "_" + listChName[1]);

		String[] wlist = WindowManager.getImageTitles();

		for (int i=0; i<wlist.length; i++) {
			if((wlist[i]).equals(window0)){
				for (int j=0; j<wlist.length; j++) {
					if((wlist[j]).equals(window1)){
						ImageCalculator ic = new ImageCalculator();
						ImagePlus img = ic.run("Divide create 32-bit stack", img0, img1);
						if (img!=null) img.show();
						img.setTitle(prefix + "_mu");
						IJ.selectWindow(prefix + "_mu");

						IJ.run("Log", "stack");
						IJ.run("Set... ", "zoom=" + prop.zoom);
						IJ.run("Scale to Fit", "");
						IJ.run("Enhance Contrast...", "saturated=0.1");
					}
				}
			}
		}
		wlist = WindowManager.getImageTitles();

		for(int j=0; j<wlist.length; j++) {
			if((wlist[j]).equals(window0)){
				for(int h=0; h<wlist.length; h++) {
					for(int i=2; i<listChName.length; i++) {
						String windowi = prefix + "_" + listChName[i];
						if((wlist[h]).equals(windowi)){
							ImagePlus imgi = WindowManager.getImage(prefix + "_" + listChName[i]);
							ImageCalculator ic = new ImageCalculator();
							ImagePlus img2 =ic.run("Divide create 32-bit stack", imgi, img0);
							if (img2!=null) img2.show();
							IJ.selectWindow("Result of " + prefix + "_" + listChName[i]);
							img2.setTitle(prefix + "_" + listChName[i] + "norm");
							IJ.selectWindow(prefix + "_" + listChName[i] + "norm");
							IJ.run("Multiply...", "value=1000000 stack");
							IJ.run("Set... ", "zoom=" + prop.zoom);
							IJ.run("Scale to Fit", "");
							IJ.run("Enhance Contrast...", "saturated=0.1");
						}
					}
				}
			}
		}
		IJ.run("Tile");
		
		String name;
		wlist = WindowManager.getImageTitles();
		
		for(int i=0; i<listChName.length; i++) {
			name = prefix + "_" + listChName[i];
			for(int j=0; j<wlist.length; j++) {
				if((wlist[j]).equals(name)) {
					ImagePlus impp = WindowManager.getImage(name);
					FileSaver fs = new FileSaver(impp);
					fs.saveAsTiff(dirImg + impp.getTitle() + ".tif");
					impp.setTitle(name + ".tif");
				}
			}
		}
		for(int i=0; i<listNormalizedName.length; i++) {
			name = prefix + "_" + listNormalizedName[i];
			for(int j=0; j<wlist.length; j++) {
				if((wlist[j]).equals(name)) {
					ImagePlus impp = WindowManager.getImage(name);
					FileSaver fs = new FileSaver(impp);
					fs.saveAsTiff(dirImg + impp.getTitle() + ".tif");
					impp.setTitle(name + ".tif");
				}
			}
		}
		return dirImg + prefix;
	}

}

