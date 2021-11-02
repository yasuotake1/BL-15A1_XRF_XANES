import ij.*;
import ij.plugin.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class Menu_XRF extends JFrame implements WindowListener, PlugIn {
	ImagePlus[] impsRaw;
	ImagePlus[] impsNorm;
	ArrayList<CorrelationPlot> corrPlots = new ArrayList<CorrelationPlot>();
	ArrayList<Integer> corrIDs = new ArrayList<Integer>();

	String btRawName = "Close Raw Data";
	String btNormalizedName = "Close Normalized Data";

	public void run(String arg) {
		XRFXANESProps prop = setupMapping.ReadProps();
		Loadsinglemap singlemap = new Loadsinglemap();
		impsRaw = singlemap.method1(prop.listSuffixes, setupMapping.listChName, prop);
		if (checkNull(impsRaw))
			return;
		impsNorm = singlemap.method2(impsRaw, setupMapping.listNormalizedName, prop);
		IJ.run("Tile");

		// フレームの準備
		final JFrame MenuFrame = new JFrame(singlemap.prefix);
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
						if (impsRaw[i] != null) {
							impsRaw[i].show();
							impsRaw[i].setActivated();
							IJ.run("Set... ", "zoom=" + prop.zoom);
						}
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
						if (impsNorm[i] != null) {
							impsNorm[i].show();
							impsNorm[i].setActivated();
							IJ.run("Set... ", "zoom=" + prop.zoom);
						}
					}
				} else {
					btNormalizedName = "Show Normalized Data";
					// Close processed data
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

		JButton bt3 = new JButton("Correlation Plot...");
		bt3.setBounds(20, 120, 240, 30);
		bt3.setFont(new Font("Arial", Font.PLAIN, 15));
		bt3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CorrelationPlot corr = new CorrelationPlot();
				corr.method1(corrPlots.size());
				if(corr.id<0) {
					corrPlots.add(corr);
					corrIDs.add(corr.id);
				}
			}
		});

		JButton bt4 = new JButton("Create Correlation Mask...");
		bt4.setBounds(20, 160, 240, 30);
		bt4.setFont(new Font("Arial", Font.PLAIN, 15));
		bt4.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int idx = -1;
				for(int j = 0;j<corrIDs.size();j++) {
					if(corrIDs.get(j)==WindowManager.getCurrentImage().getID()) {
						idx=j;
						break;
					}
				}
				if(idx==-1) {
					return;
				}
				corrPlots.get(idx).method2(prop.zoom);
			}
		});

		MenuFrame.add(btRaw);
		MenuFrame.add(btNormalized);
		MenuFrame.add(bt3);
		MenuFrame.add(bt4);

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
