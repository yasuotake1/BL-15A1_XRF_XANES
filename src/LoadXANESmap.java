import java.util.Arrays;
import ij.*;
import ij.plugin.*;
import ij.io.OpenDialog;
import ij.io.DirectoryChooser;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import java.io.File;

public class LoadXANESmap implements PlugIn {
	String dirImg = "";
	String prefix = "";

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}

	public ImagePlus[] method1(String[] listSuffixes, String[] listChName, XRFXANESProps prop) {
		boolean bRev = prop.stageConf != 0;

		IJ.run("Close All");

		OpenDialog.setDefaultDirectory(prop.defaultDir);
		DirectoryChooser dc = new DirectoryChooser("Choose directory for XANES mapping dataset...");
		if (dc.getDirectory() == null) {
			return null;
		}
		dirImg = dc.getDirectory();
		prop.defaultDir = dirImg;
		setupMapping.WriteProps(prop);

		String[] listAll = new File(dirImg).list();
		String[] listFiles = new String[listSuffixes.length];
		for (int i = 0; i < listFiles.length; i++) {
			listFiles[i] = "";
		}

		for (int i = 0; i < listSuffixes.length; i++) {
			if ((listSuffixes[i]).length() > 0) {
				for (int j = 0; j < listAll.length; j++) {
					if ((listAll[j]).endsWith(listSuffixes[i])) {
						if (listFiles.length == 0) {
							listFiles[i] = listAll[j];
						} else {
							listFiles[i] = listFiles[i] + "," + listAll[j];
						}
					}
				}
			}
		}

		int[] arrCount = new int[listFiles.length];
		for (int i = 0; i < listFiles.length; i++) {
			arrCount[i] = listFiles[i].length();

		}
		int max1 = arrCount[0];
		for (int i = 1; i < arrCount.length; i++) {
			int v = arrCount[i];
			if (v > max1) {
				max1 = v;
			}
		}
		if (max1 == 0) {
			IJ.error("No files to load.");
		}
		String FileTitle = "";

		double[] scale = { Double.NaN, Double.NaN, Double.NaN, Double.NaN };
		ImagePlus[] listImps = new ImagePlus[listChName.length];

		for (int i = 0; i < listSuffixes.length; i++) {
			if (listSuffixes.length > 0 && listFiles.length > 0) {
				if (listFiles[i] != "") {

					String[] sublist = (listFiles[i]).split(",", 0);
					int[] Subsublist = new int[sublist.length];

					for (int h = 1; h < sublist.length; h++) {

						sublist[h] = (sublist[h]).replace(listSuffixes[i], "");
						int StrIndex = (sublist[h]).lastIndexOf("_");
						int StrLength = (sublist[h]).length();
						Subsublist[h] = Integer.parseInt((sublist[h]).substring(StrIndex + 1, StrLength));
						FileTitle = (sublist[h]).substring(0, StrIndex + 1);

					}
					Subsublist[0] = 0;

					Arrays.sort(Subsublist);

					for (int k = 1; k < sublist.length; k++) {

						sublist[k] = FileTitle + String.format("%03d", Subsublist[k]) + listSuffixes[i];
					}

					prefix = FileTitle.replace("_qscan_", "");
					if (Double.isNaN(scale[0]) && Double.isNaN(scale[1]) && Double.isNaN(scale[2])
							&& Double.isNaN(scale[3]))
						scale = setupMapping.getScanInfo(dirImg, prefix + "_qscan_001", prop);
					ImageStack stack = new ImageStack();
					TextReader tr = new TextReader();
					ImageProcessor ip;
					for (int j = 1; j < sublist.length; j++) {
						String file = dirImg + sublist[j];
						ip = tr.open(file);
						if (j == 1) {
							stack = new ImageStack(ip.getWidth(), ip.getHeight());
						}
						stack.addSlice(ip);
					}
					listImps[i] = new ImagePlus(prefix + "_" + listChName[i], stack);
					listImps[i].show();
					if (!bRev) {
						IJ.run(listImps[i], "Flip Horizontally", "stack");
					}
					IJ.run(listImps[i], "Flip Vertically", "stack");
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
		}
		
		for (int i = 0; i < listImps.length; i++) {
			if (listImps[i] != null) {
				listImps[i].setTitle(prefix + "_" + listChName[i] + ".tif");
				FileSaver fs = new FileSaver(listImps[i]);
				fs.saveAsTiff(dirImg + listImps[i].getTitle());
				fs.saveAsJpeg(dirImg + listImps[i].getTitle().replace(".tif", ".jpg"));
			}
		}
		
		return listImps;
	}
	
	public ImagePlus[] method2(ImagePlus[] listImps, String[] listNormalizedName, XRFXANESProps prop) {
		ImagePlus[] listNormImps = new ImagePlus[listNormalizedName.length];
		ImageCalculator ic = new ImageCalculator();
		
		for (int i = 0; i < listNormImps.length; i++) {
			if (listImps[0] != null && listImps[i + 1] != null) {
				listNormImps[i] = ic.run("Divide create 32-bit stack", listImps[i + 1], listImps[0]);
				listNormImps[i].setTitle(prefix + "_" + listNormalizedName[i]);
				listNormImps[i].show();
				if (i == 0) {
					IJ.run(listNormImps[i], "Reciprocal", "stack");
					IJ.run(listNormImps[i], "Log", "stack");
				} else {
					IJ.run(listNormImps[i], "Multiply...", "value=1000000 stack");
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
				fs.saveAsTiff(dirImg + listNormImps[i].getTitle());
				fs.saveAsJpeg(dirImg + listNormImps[i].getTitle().replace(".tif", ".jpg"));
			}
		}

		return listNormImps;		
	}
}
