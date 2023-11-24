import ij.*;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.*;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Menu_XANES extends JFrame implements WindowListener, PlugIn {
	ImagePlus[] impsRaw;
	ImagePlus[] impsNorm;

	String btRawName = "Close Raw Data";
	String btNormalizedName = "Close Normalized Data";

	ImagePlus imp;

	public void run(String arg) {
		XRFXANESProps prop = setupMapping.ReadProps();

		LoadXANESmap xanesmap = new LoadXANESmap();
		impsRaw = xanesmap.method1(prop.listSuffixes, setupMapping.listChName, prop);
		if (checkNull(impsRaw))
			return;
		impsNorm = xanesmap.method2(impsRaw, setupMapping.listNormalizedName, prop);
		IJ.run("Tile");

		final JFrame MenuFrame = new JFrame(xanesmap.prefix);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setAlwaysOnTop(true);
		MenuFrame.addWindowListener(this);
		MenuFrame.setBounds(150, 150, 295, 260);
		MenuFrame.setLayout(null);

		final JButton btRaw = new JButton();
		btRaw.setText(btRawName);
		btRaw.setBounds(20, 20, 240, 30);
		btRaw.setFont(new Font("Arial", Font.PLAIN, 15));
		btRaw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btRawName == "Show Raw Data") {
					btRawName = "Close Raw Data";
					// Show raw data
					for (int i = 0; i < impsRaw.length; i++) {
						if (impsRaw[i] != null)
							impsRaw[i].show();
					}
				} else {
					btRawName = "Show Raw Data";
					// Close raw data
					for (int i = 0; i < impsRaw.length; i++) {
						if (impsRaw[i] != null)
							impsRaw[i].hide();
					}
				}
				IJ.run("Main Window [enter]");
				MenuFrame.toFront();
				if (WindowManager.getWindowCount() > 0)
					IJ.run("Tile");
				btRaw.setText(btRawName);
			}
		});

		final JButton btNormalized = new JButton();
		btNormalized.setText(btNormalizedName);
		btNormalized.setBounds(20, 60, 240, 30);
		btNormalized.setFont(new Font("Arial", Font.PLAIN, 15));
		btNormalized.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btNormalizedName == "Show Normalized Data") {
					btNormalizedName = "Close Normalized Data";
					// Show normalized data
					for (int i = 0; i < impsNorm.length; i++) {
						if (impsNorm[i] != null)
							impsNorm[i].show();
					}
				} else {
					btNormalizedName = "Show Normalized Data";
					// Close normalized data
					for (int i = 0; i < impsNorm.length; i++) {
						if (impsNorm[i] != null)
							impsNorm[i].hide();
					}
				}
				IJ.run("Main Window [enter]");
				MenuFrame.toFront();
				if (WindowManager.getWindowCount() > 0)
					IJ.run("Tile");
				btNormalized.setText(btNormalizedName);
			}
		});

		JButton btPlot = new JButton("Plot Bulk/ROI XANES");
		btPlot.setBounds(20, 120, 240, 30);
		btPlot.setFont(new Font("Arial", Font.PLAIN, 15));
		btPlot.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				imp = WindowManager.getCurrentImage();
				if (imp.getNSlices() < 2) {
					IJ.error("This is not an imagestack.");
					return;
				}
				int currentSliceNumber = imp.getSlice();
				RoiManager roiManager = RoiManager.getInstance();
				Roi roi;
				ImagingXAFSPlot.clear();

				if (roiManager == null || roiManager.getCount() == 0) {
					roi = imp.getRoi();
					if (roi == null) {
						IJ.error("No selection in current image.");
						return;
					}
					IJ.showStatus("Plotting spectrum at current ROI...");
					PlotbulkROIXANES.addPlots(imp, 0,xanesmap.dirImg, xanesmap.prefix);

				} else {
					IJ.showStatus("Plotting spectrum at ROIs stored in ROI Manager...");
					for (int idx = 0; idx < roiManager.getRoisAsArray().length; idx++) {
						roi = roiManager.getRoisAsArray()[idx];
						if (roi != null) {
							imp.setRoi(roi);
							PlotbulkROIXANES.addPlots(imp, idx,xanesmap.dirImg, xanesmap.prefix);
						}
					}
					roiManager.runCommand("Show All");
				}

				ImagingXAFSPlot.show(true);
				imp.setSlice(currentSliceNumber);
			}
		});

		JButton btTXW = new JButton("Save Current Data for TXW");
		btTXW.setBounds(20, 160, 240, 30);
		btTXW.setFont(new Font("Arial", Font.PLAIN, 15));
		btTXW.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				imp = WindowManager.getCurrentImage();
				if (imp.getNSlices() < 2) {
					IJ.error("This is not an imagestack.");
					return;
				}
				SaveCurrentXANESmap saveXANESmap = new SaveCurrentXANESmap();
				saveXANESmap.method(imp, xanesmap.dirImg, xanesmap.prefix);
			}
		});

		MenuFrame.add(btRaw);
		MenuFrame.add(btNormalized);
		MenuFrame.add(btPlot);
		MenuFrame.add(btTXW);

		IJ.run("Main Window [enter]");
		MenuFrame.setVisible(true);

	}

	public void windowOpened(WindowEvent e) {

	}

	public void windowClosing(WindowEvent e) {
		for (int i = 0; i < impsRaw.length; i++) {
			if (impsRaw[i] != null)
				impsRaw[i].close();
		}
		for (int i = 0; i < impsNorm.length; i++) {
			if (impsNorm[i] != null)
				impsNorm[i].close();
		}
	}

	public void windowClosed(WindowEvent e) {

	}

	public void windowIconified(WindowEvent e) {

	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowActivated(WindowEvent e) {

	}

	public void windowDeactivated(WindowEvent e) {

	}

	private boolean checkNull(ImagePlus[] imps) {
		boolean b = true;
		if (imps == null)
			return b;
		for (int i = 0; i < imps.length; i++) {
			if (imps[i] != null)
				b = false;
		}
		return b;
	}

}
