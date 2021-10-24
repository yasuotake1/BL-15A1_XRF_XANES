import ij.*;
import ij.plugin.*;
import ij.io.FileSaver;
import ij.io.OpenDialog;
import java.io.File;

public class Loadsinglemap implements PlugIn {
	public String dir = "";
	public String prefix = "";

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}

	public ImagePlus[] method1(String[] listSuffixes, String[] listChName, XRFXANESProps prop) {
		boolean bRev = prop.stageConf != 0;

		OpenDialog od = new OpenDialog("Select one of the 2D ASCII data files.", prop.defaultDir, "");
		if (od.getDirectory() == null)
			return null;
		dir = od.getDirectory();
		prop.defaultDir = dir;
		setupMapping.WriteProps(prop);

		String nameTemp = od.getFileName();
		prefix = nameTemp;
		for (int i = 0; i < listSuffixes.length; i++) {
			if (listSuffixes[i].length() > 0 && nameTemp.endsWith(listSuffixes[i])) {
				prefix = nameTemp.replace(listSuffixes[i], "");
				break;
			}
		}
		if (prefix == nameTemp) {
			IJ.error("An invalid file is selected.");
			return null;
		}

		String[] listAll = new File(dir).list();
		String[] listFiles = new String[listSuffixes.length];
		for (int i = 0; i < listFiles.length; i++) {
			listFiles[i] = "";
		}
		for (int i = 0; i < listAll.length; i++) {
			for (int j = 0; j < listSuffixes.length; j++) {
				if ((listSuffixes[j].length() > 0) && (listAll[i].equals(prefix + listSuffixes[j]))) {
					listFiles[j] = listAll[i];
				}
			}
		}

		double[] scale = setupMapping.getScanInfo(dir, prefix, prop);
		ImagePlus[] listImps = new ImagePlus[listChName.length];

		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].length() > 0) {
				String file = dir + listFiles[i];
				listImps[i] = IJ.openImage(file);
				listImps[i].show();
				if (!bRev) {
					IJ.run("Flip Horizontally");
				}
				IJ.run("Flip Vertically");
				if (!Double.isNaN(scale[0]) && !Double.isNaN(scale[1]) && !Double.isNaN(scale[2])
						&& !Double.isNaN(scale[3])) {
					IJ.run("Properties...", "unit=" + prop.scaleConf + " pixel_width=" + scale[0] + " pixel_height="
							+ scale[1] + " origin=" + scale[2] + "," + scale[3]);
				} else {
					IJ.run("Properties...", "unit=pixel pixel_width=1 pixel_height=1 origin=0,0");
				}

				IJ.run("Set... ", "zoom=" + prop.zoom);
				IJ.run("Scale to Fit", "");
			} else {
				listImps[i] = null;
			}
		}
		
		for (int i = 0; i < listImps.length; i++) {
			if (listImps[i] != null) {
				listImps[i].setTitle(prefix + "_" + listChName[i] + ".tif");
				FileSaver fs = new FileSaver(listImps[i]);
				fs.saveAsTiff(dir + listImps[i].getTitle());
				fs.saveAsJpeg(dir + listImps[i].getTitle().replace(".tif", ".jpg"));
			}
		}
		
		return listImps;
	}
	
	public ImagePlus[] method2(ImagePlus[] listImps, String[] listNormalizedName, XRFXANESProps prop) {
		ImagePlus[] listNormImps = new ImagePlus[listNormalizedName.length];
		ImageCalculator ic = new ImageCalculator();

		for (int i = 0; i < listNormImps.length; i++) {
			if (listImps[0] != null && listImps[i + 1] != null) {
				listNormImps[i] = ic.run("Divide create 32-bit", listImps[i + 1], listImps[0]);
				listNormImps[i].setTitle(prefix + "_" + listNormalizedName[i]);
				listNormImps[i].show();
				if (i == 0) {
					IJ.run(listNormImps[i], "Reciprocal", "");
					IJ.run(listNormImps[i], "Log", "");
				} else {
					IJ.run(listNormImps[i], "Multiply...", "value=1000000");
				}
				IJ.run(listNormImps[i], "Set... ", "zoom=" + prop.zoom);
				IJ.run(listNormImps[i], "Scale to Fit", "");
				IJ.run(listNormImps[i], "Enhance Contrast...", "saturated=0.1");
				listNormImps[i].updateAndDraw();
			} else {
				listNormImps[i] = null;
			}
		}

		for (int i = 0; i < listNormImps.length; i++) {
			if (listNormImps[i] != null) {
				listNormImps[i].setTitle(prefix + "_" + listNormalizedName[i] + ".tif");
				FileSaver fs = new FileSaver(listNormImps[i]);
				fs.saveAsTiff(dir + listNormImps[i].getTitle());
				fs.saveAsJpeg(dir + listNormImps[i].getTitle().replace(".tif", ".jpg"));
			}
		}

		return listNormImps;
	}
}
