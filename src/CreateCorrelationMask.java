import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import ij.WindowManager.*;

import java.io.*;
import java.util.*;

import ij.io.OpenDialog;
import ij.plugin.frame.*;
import ij.plugin.filter.*;

import ij.util.Tools;

import ij.macro.*;
import ij.measure.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;

/*

import ij.plugin.filter.Filler;
import ij.plugin.frame.*;
import ij.plugin.filter.*;
import ij.*;
import ij.IJ;
import ij.ImagePlus;
import ij.util.Tools;

import ij.plugin.*;
import ij.plugin.frame.*;
import ij.plugin.ContrastEnhancer;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;

import ij.gui.*;
import ij.gui.Plot;
import ij.gui.GenericDialog;
import ij.gui.PlotWindow;
import ij.gui.ProgressBar;

import ij.process.*;
import ij.WindowManager.*;
import ij.io.OpenDialog;
import ij.macro.*;
import ij.macro.Interpreter;
import ij.process.ImageProcessor;
import ij.measure.*;
import ij.measure.ResultsTable;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.util.Vector;

import java.util.ArrayList;
import javax.imageio.ImageIO;
*/
public class CreateCorrelationMask implements PlugIn {

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}


	public void method(ArrayList<Integer> idCorrSrc1, ArrayList<Integer> idCorrSrc2, double zoom){

		ImagePlus impR = WindowManager.getCurrentImage();

		//プロット画面か？
		if(!impR.getTitle().startsWith("Correlation plot ")){
			IJ.error("Select correlation plot window and make ROI.");
			return;
		}

		int idCorrImg = impR.getID();
		int idxCorr = Integer.parseInt(impR.getTitle().replace("Correlation plot ", ""));

		IJ.run("ROI Manager...", "");

		RoiManager rm = RoiManager.getInstance();

		//ROIが選択されているか？　
		if (impR.getRoi()==null){
			IJ.error("There is no ROI selected.");
			return;
		}
		else{
			rm.addRoi(impR.getRoi());
		}

		IJ.selectWindow(idCorrImg);

		//プロット画面からX軸Y軸の値を読み取る

		double xMin=0;
		double xMax=0;
		double yMin=0;
		double yMax=0;

		final Window win = impR.getWindow();
		final PlotWindow plotwin = (PlotWindow)win;
		Plot plot = plotwin.getPlot();
		double[] xyminmax = plot.getLimits();

		xMin = xyminmax[0];
		xMax = xyminmax[1];
		yMin = xyminmax[2];
		yMax = xyminmax[3];

		//マスク画面の作成

//		https://imagej.nih.gov/ij/developer/source/ij/plugin/Selection.java.html　を参照した
		createMask(impR);
		IJ.makeRectangle(60, 13, 400, 400);

		IJ.run("Crop", "");

		ImagePlus impM = WindowManager.getCurrentImage();
		int idMask = impM.getID();


		IJ.selectWindow(idCorrSrc1.get(idxCorr));
		ImagePlus imp1 = WindowManager.getImage(idCorrSrc1.get(idxCorr));

		String t1 = imp1.getTitle();
		t1 = t1.replace(".tif", "");

		//ウィンドウのサイズを取得
		int w1 = imp1.getWidth();
		int h1 = imp1.getHeight();

		//配列を作成
		double[] arr1 = new double[w1 * h1];

		//各ピクセルの値を取得し配列に入れる
		for(int i=0; i<h1; i++) {
			for(int j=0; j<w1; j++) {

				float pv1 = imp1.getProcessor().getPixelValue(j, i);
				arr1[(i * w1) + j]=(double)pv1;
			}
		}

		IJ.selectWindow(idCorrSrc2.get(idxCorr));
		ImagePlus imp2 = WindowManager.getImage(idCorrSrc2.get(idxCorr));

		String t2 = imp2.getTitle();
		t2 = t2.replace(".tif", "");

		//ウィンドウのサイズを取得
		int w2 = imp2.getWidth();
		int h2 = imp2.getHeight();

		//配列を作成
		double[] arr2 = new double[w2 * h2];

		//各ピクセルの値を取得し配列に入れる
		for(int i=0; i<h2; i++) {
			for(int j=0; j<w2; j++) {

				float pv2 = imp2.getProcessor().getPixelValue(j, i);
				arr2[(i * w2) + j]=(double)pv2;
			}
		}

		if(w1 != w2 || h1 != h2) {
			IJ.error("Selected images have different size.");
			return;
		}

		IJ.selectWindow(idMask);
		for(int i=0; i<arr1.length; i++) {
			int xInMask = (int)Math.round((arr1[i] - xMin) / (xMax - xMin) * 400);
			int yInMask = (int)Math.round((yMax - arr2[i]) / (yMax - yMin) * 400);
			if(impM.getProcessor().getPixelValue(xInMask, yInMask) == 0) {
				arr1[i] = Double.NaN;
				arr2[i] = Double.NaN;
			}
		}
		impM.changes = false;
		impM.close();

		ImagePlus imp_sti = IJ.createImage("corrMask", "8-bit white", w1, h1, 1);
		imp_sti.show();

		for(int i=0; i<h1; i++) {
			for(int j=0; j<w1; j++) {
				if(Double.isNaN(arr1[(i * w1) + j]))
					imp_sti.getProcessor().putPixel(j, i, 0);
			}
		}

		IJ.run("Invert LUT");
		IJ.run("Set... ", "zoom=" + zoom);
		IJ.run("Scale to Fit", "");

		ImagePlus imp1d =imp1.duplicate();
		imp1d.setTitle(t1 + "_corrMask");
		imp1d.show();

		for(int i=0; i<h1; i++) {
			for(int j=0; j<w1; j++) {
				imp1d.getProcessor().putPixelValue(j, i, arr1[(i * w1) + j]);
			}
		}
		IJ.run("Set... ", "zoom=" + zoom);
		IJ.run("Scale to Fit", "");
		IJ.run("Enhance Contrast...", "saturated=0.1");

		ImagePlus imp2d =imp2.duplicate();
		imp2d.setTitle(t2 + "_corrMask");
		imp2d.show();

		for(int i=0; i<h2; i++) {
			for(int j=0; j<w2; j++) {
				imp2d.getProcessor().putPixelValue(j, i, arr2[(i * w2) + j]);
			}
		}
		IJ.run("Set... ", "zoom=" + zoom);
		IJ.run("Scale to Fit", "");
		IJ.run("Enhance Contrast...", "saturated=0.1");


//		IJ.run("Tile");

	}
/**/
	void createMaskFromThreshold(ImagePlus imp) {
	        ImageProcessor ip = imp.getProcessor();
	        if (ip.getMinThreshold()==ImageProcessor.NO_THRESHOLD)
	            {IJ.error("Create Mask", "Area selection or thresholded image required"); return;}
	        double t1 = ip.getMinThreshold();
	        double t2 = ip.getMaxThreshold();
	        IJ.run("Duplicate...", "title=mask");
	        ImagePlus imp2 = WindowManager.getCurrentImage();
	        ImageProcessor ip2 = imp2.getProcessor();
	        ip2.setThreshold(t1, t2, ip2.getLutUpdateMode());
	        IJ.run("Convert to Mask");
	}

	void createMask(ImagePlus imp) {
	        Roi roi = imp.getRoi();
	        boolean useInvertingLut = Prefs.useInvertingLut;
	        Prefs.useInvertingLut = false;
	        boolean selectAll = roi!=null && roi.getType()==Roi.RECTANGLE && roi.getBounds().width==imp.getWidth()
	            && roi.getBounds().height==imp.getHeight() && imp.isThreshold();
	        if (roi==null || !(roi.isArea()||roi.getType()==Roi.POINT) || selectAll) {
	            createMaskFromThreshold(imp);
	            Prefs.useInvertingLut = useInvertingLut;
	            return;
	        }
	        ImagePlus maskImp = null;
	        Frame frame = WindowManager.getFrame("Mask");
	        if (frame!=null && (frame instanceof ImageWindow))
	            maskImp = ((ImageWindow)frame).getImagePlus();
	        if (maskImp==null) {
	            ImageProcessor ip = new ByteProcessor(imp.getWidth(), imp.getHeight());
	            if (!Prefs.blackBackground)
	                ip.invertLut();
	            maskImp = new ImagePlus("Mask", ip);
	            maskImp.show();
	        }
	        ImageProcessor ip = maskImp.getProcessor();
	        ip.setRoi(roi);
	        ip.setValue(255);
	        ip.fill(ip.getMask());
	        Calibration cal = imp.getCalibration();
	        if (cal.scaled()) {
	            Calibration cal2 = maskImp.getCalibration();
	            cal2.pixelWidth = cal.pixelWidth;
	            cal2.pixelHeight = cal.pixelHeight;
	            cal2.setUnit(cal.getUnit());
	        }
	        maskImp.updateAndRepaintWindow();
	        Prefs.useInvertingLut = useInvertingLut;
    	}

}

