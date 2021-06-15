import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import ij.WindowManager.*;
import ij.io.FileSaver;
import ij.io.OpenDialog;
import java.io.*;
import java.util.*;

public class Loadsinglemap implements PlugIn {

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}

	public String method(String[] listSuffixes, String[] listChName, String[] listNormalizedName, XRFXANESProps prop){

		OpenDialog od = new OpenDialog("Select one of the 2D ASCII data files.", prop.defaultDir, "");

		if(od.getDirectory() == null) return null;

		String dir = od.getDirectory();
//		String filepath = od.getPath();
		String nameTemp = od.getFileName();
		String[] listAll = new File(dir).list();
		String nameStrip = nameTemp;
		
		prop.defaultDir = dir;
		setupMapping.WriteProps(prop);

		boolean bRev = false;
		if(prop.stageConf==0){
			bRev=false;
		}else{
			bRev=true;
		}

		for(int i=0; i<listSuffixes.length; i++) {
			if(listSuffixes[i].length() > 0 && nameTemp.endsWith(listSuffixes[i])){
				nameStrip = nameTemp.replaceAll(listSuffixes[i], "");
			}
		}

		if(nameStrip == nameTemp){
			IJ.error("An invalid file is selected.");
			return null;
		}

		String[] listFiles = new String[listSuffixes.length];
		for(int i=0; i<listFiles.length; i++) {
			listFiles[i] = "";
		}

		for(int i=0; i<listAll.length; i++) {
			for(int j=0; j<listSuffixes.length; j++) {
				if((listSuffixes[j].length() > 0) && ((listAll[i]).equals(nameStrip + listSuffixes[j]))){
					listFiles[j] = listAll[i];
				}
			}
		}

		String prefix = nameStrip;

		double[] scale = setupMapping.getScanInfo(dir, prefix, prop);

		for(int i=0; i<listFiles.length; i++) {

			if(listFiles[i].length() > 0) {
				String file = dir + listFiles[i];

//				File f = new File(file);
				ImagePlus impp = IJ.openImage(file);
				impp.show();				

				if(!bRev){
					IJ.run("Flip Horizontally");
				}
				IJ.run("Flip Vertically");
				if(!Double.isNaN(scale[0]) && !Double.isNaN(scale[1]) && !Double.isNaN(scale[2]) && !Double.isNaN(scale[3])){
					IJ.run("Properties...", "unit=" + prop.scaleConf + " pixel_width=" + scale[0] + " pixel_height=" + scale[1] + " origin=" + scale[2] + "," + scale[3]);
				} else {
					IJ.run("Properties...", "unit=pixel pixel_width=1 pixel_height=1 origin=0,0");
				}

				IJ.run("Set... ", "zoom=" + prop.zoom);
				IJ.run("Scale to Fit", "");

				impp.setTitle(prefix + "_" + listChName[i]);
				
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
						ImagePlus img3 = ic.run("Divide create 32-bit",  img0, img1);
						if (img3!=null) img3.show();
						IJ.selectWindow("Result of " +  window0);
						img3.setTitle(prefix + "_mu");
						IJ.selectWindow(prefix + "_mu");
						IJ.run("Log");
						IJ.run("Set... ", "zoom=" + prop.zoom);
						IJ.run("Scale to Fit", "");
						IJ.run(img3,"Enhance Contrast...", "saturated=0.1");
						img3.updateAndDraw();
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
							ImageCalculator ic2 = new ImageCalculator();
							ImagePlus img4 = ic2.run("Divide create 32-bit", imgi, img0);
							if (img4!=null) img4.show();
							IJ.selectWindow("Result of " + windowi);
							img4.setTitle(prefix + "_" + listChName[i] + "norm");
							IJ.selectWindow(prefix + "_" + listChName[i] + "norm");
							IJ.run("Multiply...", "value=1000000");
							IJ.run("Set... ", "zoom=" + prop.zoom);
							IJ.run("Scale to Fit", "");
							(new ContrastEnhancer()).stretchHistogram(img4,0.1);
							IJ.run(img4,"Enhance Contrast...", "saturated=0.1");
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
					fs.saveAsTiff(dir + impp.getTitle() + ".tif");
					fs.saveAsJpeg(dir + impp.getTitle() + ".jpg");
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
					fs.saveAsTiff(dir + impp.getTitle() + ".tif");
					fs.saveAsJpeg(dir + impp.getTitle() + ".jpg");
					impp.setTitle(name + ".tif");
				}
			}
		}
		
		return dir + prefix;
	}
}

