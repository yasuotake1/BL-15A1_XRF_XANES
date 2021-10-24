import ij.*;
import ij.plugin.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

		// フレームの準備
		final JFrame MenuFrame = new JFrame(xanesmap.prefix);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setAlwaysOnTop(true);
		MenuFrame.addWindowListener(this);
		MenuFrame.setBounds(150, 150, 295, 260); // MenuFrame.setBounds(表示するX座標,表示するY座標,フレームの幅,フレームの高さ);
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
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
					IJ.run("Tile");
				} else {
					btRawName = "Show Raw Data";
					// Close raw data
					for (int i = 0; i < impsRaw.length; i++) {
						if (impsRaw[i] != null)
							impsRaw[i].hide();
					}
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
					IJ.run("Tile");
				}
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
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
					IJ.run("Tile");
				} else {
					btNormalizedName = "Show Normalized Data";
					// Close normalized data
					for (int i = 0; i < impsNorm.length; i++) {
						if (impsNorm[i] != null)
							impsNorm[i].hide();
					}
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
					IJ.run("Tile");
				}
				btNormalized.setText(btNormalizedName);
			}
		});

		JButton bt8 = new JButton("Plot Bulk/ROI XANES");
		bt8.setBounds(20, 120, 240, 30);
		bt8.setFont(new Font("Arial", Font.PLAIN, 15));
		bt8.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				imp = WindowManager.getCurrentImage();
				PlotbulkROIXANES PlotbulkXANES = new PlotbulkROIXANES();
				PlotbulkXANES.method(imp, xanesmap.dirImg, xanesmap.prefix);
			}
		});

		JButton bt9 = new JButton("Save Current Data for TXW");
		bt9.setBounds(20, 160, 240, 30);
		bt9.setFont(new Font("Arial", Font.PLAIN, 15));
		bt9.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				imp = WindowManager.getCurrentImage();
				SaveCurrentXANESmap saveXANESmap = new SaveCurrentXANESmap();
				saveXANESmap.method(imp, xanesmap.dirImg, xanesmap.prefix);
			}
		});

		// フレームにボタンを追加
		MenuFrame.add(btRaw);
		MenuFrame.add(btNormalized);
		MenuFrame.add(bt8);
		MenuFrame.add(bt9);

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
