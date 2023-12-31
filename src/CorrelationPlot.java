
/*************************************************
相関解析







*************************************************/

import ij.*;
import ij.gui.*;
import ij.io.OpenDialog;
import ij.plugin.*;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.*;

public class CorrelationPlot implements PlugIn {

	private PlotWindow pw;
	public int id = 0;
	private ImagePlus imp1;
	private ImagePlus imp2;
	private double[] arr1;
	private double[] arr2;
	private int wid;
	private int hei;

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}

	public void method1(int index) {

		String[] wList = WindowManager.getImageTitles();

		GenericDialog gd = new GenericDialog("Correlation plot");
		gd.addChoice("Image 1: ", wList, wList[0]);
		gd.addChoice("Image 2: ", wList, wList[1]);
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		imp1 = WindowManager.getImage(gd.getNextChoice());
		imp2 = WindowManager.getImage(gd.getNextChoice());

		// ウィンドウIDを取得し配列に追加;

		// ウィンドウのサイズを取得
		wid = imp1.getWidth();
		hei = imp1.getHeight();
		if (wid != imp2.getWidth() || hei != imp2.getHeight()) {
			IJ.error("Selected images have different size.");
			return;
		}

		// 最初に選択したウィンドウの拡張子を取る
		String t1 = imp1.getTitle();
		t1 = t1.replace(".tif", "");

		// 配列を作成
		arr1 = new double[wid * hei];
		for (int i = 0; i < arr1.length; i++) {
			arr1[i] = (double) ((float[]) imp1.getProcessor().getPixels())[i];
		}

		// ２番目に選択したウィンドウの拡張子を取る
		String t2 = imp2.getTitle();
		t2 = t2.replace(".tif", "");

		// 配列を作成
		arr2 = new double[wid * hei];
		for (int i = 0; i < arr2.length; i++) {
			arr2[i] = (double) ((float[]) imp2.getProcessor().getPixels())[i];
		}

		// 各配列内の最小、最大値を取得する
		double max1 = arr1[0];
		double min1 = arr1[0];
		for (int i = 1; i < arr1.length; i++) {
			max1 = arr1[i] > max1 ? arr1[i] : max1;
			min1 = arr1[i] < min1 ? arr1[i] : min1;
		}
		double max2 = arr2[0];
		double min2 = arr2[0];
		for (int i = 1; i < arr2.length; i++) {
			max2 = arr2[i] > max2 ? arr2[i] : max2;
			min2 = arr2[i] < min2 ? arr2[i] : min2;
		}

		// 表示
		Plot p = new Plot("Correlation plot " + String.valueOf(index), t1, t2);
		p.setFrameSize(400, 400);
		p.setLimits(min1, max1, min2, max2);
		p.add("dots", arr1, arr2);
		pw = p.show();
		id = pw.getImagePlus().getID();

		// ファイル保存の確認
		GenericDialog gdd = new GenericDialog("Save ");
		gdd.addMessage("Save XY-data text file? ");
		gdd.showDialog();
		if (gdd.wasCanceled()) {
			return;
		}

		// 保存
		OpenDialog od = new OpenDialog("Save As ");
		String dir = od.getDirectory();
		String fileCorr = od.getFileName();
		try {
			File file = new File(dir + fileCorr);
			FileWriter filewriter = new FileWriter(file);

			String WriteText = t1 + "\t" + t2 + "\r\n";
			filewriter.write(WriteText);

			for (int i = 0; i < arr1.length; i++) {
				WriteText = arr1[i] + "\t" + arr2[i] + "\r\n";
				filewriter.write(WriteText);
			}
			filewriter.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void method2(double zoom) {
		if (pw == null) {
			return;
		}
		ImagePlus impPlot = pw.getImagePlus();
		Roi roi = impPlot.getRoi();
		if (roi == null) {
			IJ.error("There is no ROI selected.");
			return;
		}

		Plot plot = pw.getPlot();
		double[] xyMinMax = plot.getLimits();
		double xMin = xyMinMax[0];
		double xMax = xyMinMax[1];
		double yMin = xyMinMax[2];
		double yMax = xyMinMax[3];
		Rectangle rec = plot.getDrawingFrame();

		// マスク画面の作成
		boolean invertLut = Prefs.useInvertingLut;
		Prefs.useInvertingLut = false;
		ImagePlus impFlat = NewImage.createByteImage("Flat", impPlot.getWidth(), impPlot.getHeight(), 1,
				NewImage.FILL_BLACK);
		ImageProcessor ipFlat = impFlat.getProcessor();
		ipFlat.setRoi(roi);
		ipFlat.setValue(255);
		ipFlat.fill(ipFlat.getMask());
		Prefs.useInvertingLut = invertLut;

		// マスク内かどうかの判定
		boolean[] arrInMask = new boolean[arr1.length];
		for (int i = 0; i < arrInMask.length; i++) {
			int xInPlot = (int) ((arr1[i] - xMin) / (xMax - xMin) * rec.width) + rec.x;
			int yInPlot = (int) ((yMax - arr2[i]) / (yMax - yMin) * rec.height + rec.y);
			arrInMask[i] = ipFlat.getPixel(xInPlot, yInPlot) == 0;
		}

		// マスク画像
		ImagePlus impMask = NewImage.createByteImage("Correlation Mask", wid, hei, 1, NewImage.FILL_BLACK);
		byte[] pixels = (byte[]) impMask.getProcessor().getPixels();
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = (byte) (arrInMask[i] ? 255 : 0);
		}
		impMask.show();
		impMask.setActivated();
		IJ.run("Invert LUT");
		IJ.run("Set... ", "zoom=" + zoom);
		IJ.run("Scale to Fit", "");

		// マスクした画像
		if (imp1.getProcessor() != null) {
			ImagePlus imp1Mask = imp1.duplicate();
			imp1Mask.setTitle(imp1.getTitle().replace(".tif", "") + "_corrMask");
			for (int i = 0; i < hei; i++) {
				for (int j = 0; j < wid; j++) {
					if (arrInMask[i * wid + j]) {
						imp1Mask.getProcessor().putPixelValue(j, i, Double.NaN);
					}
				}
			}
			imp1Mask.show();
			imp1Mask.setActivated();
			IJ.run("Set... ", "zoom=" + zoom);
			IJ.run("Scale to Fit", "");
			IJ.run("Enhance Contrast...", "saturated=0.1");
		}
		if (imp2.getProcessor() != null) {
			ImagePlus imp2Mask = imp2.duplicate();
			imp2Mask.setTitle(imp2.getTitle().replace(".tif", "") + "_corrMask");
			for (int i = 0; i < hei; i++) {
				for (int j = 0; j < wid; j++) {
					if (arrInMask[i * wid + j]) {
						imp2Mask.getProcessor().putPixelValue(j, i, Double.NaN);
					}
				}
			}
			imp2Mask.show();
			imp2Mask.setActivated();
			IJ.run("Set... ", "zoom=" + zoom);
			IJ.run("Scale to Fit", "");
			IJ.run("Enhance Contrast...", "saturated=0.1");
		}
	}

}
