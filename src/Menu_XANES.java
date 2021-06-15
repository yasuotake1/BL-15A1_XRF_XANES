import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;

import ij.gui.GenericDialog;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import java.util.*;

/*
import ij.plugin.PlugIn;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.ArrayList;
*/
import java.awt.*;
import java.awt.event.*;

public class Menu_XANES extends JFrame implements WindowListener,PlugIn {


	String btRawName="Close Raw Data";
	String btNormalizedName="Close Normalized Data";
	String strDirAndName;
	String strDir;
	String strName;

	ImagePlus imp;

	public void run(String arg) {
		XRFXANESProps prop = setupMapping.ReadProps();

		LoadXANESmap xanesmap = new LoadXANESmap();
		strDirAndName = xanesmap.method(prop.listSuffixes, setupMapping.listChName, setupMapping.listNormalizedName, prop);
		if(strDirAndName==null){
			return;
		}
		strDir = strDirAndName.substring(0, strDirAndName.lastIndexOf(java.io.File.separator) + 1);
		strName = strDirAndName.substring(strDirAndName.lastIndexOf(java.io.File.separator) + 1);

		//フレームの準備
		final JFrame MenuFrame = new JFrame(strName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setAlwaysOnTop(true);
		MenuFrame.addWindowListener(this);
		MenuFrame.setBounds(150, 150, 295, 260);		//MenuFrame.setBounds(表示するX座標,表示するY座標,フレームの幅,フレームの高さ);
		MenuFrame.setLayout(null);

		final JButton btRaw = new JButton();
		btRaw.setText(btRawName);
		btRaw.setBounds(20, 20, 240, 30);
		btRaw.setFont(new Font("Arial", Font.PLAIN, 15));
		btRaw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				if(btRawName=="Show Raw Data"){
					//Show raw data
					btRawName="Close Raw Data";
					showData srd = new showData();
					srd.method(strDir, strName, setupMapping.listChName, prop);
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
				}else{
					//Close raw data
					btRawName="Show Raw Data";
					closeData crd = new closeData();
					crd.method(strName, setupMapping.listChName);
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
				}
				btRaw.setText(btRawName);
			}
	        });

		final JButton btNormalized = new JButton();
		btNormalized.setText(btNormalizedName);
		btNormalized.setBounds(20, 60, 240, 30);
		btNormalized.setFont(new Font("Arial", Font.PLAIN, 15));
		btNormalized.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				if(btNormalizedName=="Show Normalized Data"){
					//Show normalized data
					btNormalizedName="Close Normalized Data";
					showData snd = new showData();
					snd.method(strDir, strName, setupMapping.listNormalizedName, prop);
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
				}else{
					//Close normalized data
					btNormalizedName="Show Normalized Data";
					closeData cnd = new closeData();
					cnd.method(strName, setupMapping.listNormalizedName);
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
				}
				btNormalized.setText(btNormalizedName);
			}
	        });
		
		JButton bt8 = new JButton("Plot Bulk/ROI XANES");
		bt8.setBounds(20, 120, 240, 30);
		bt8.setFont(new Font("Arial", Font.PLAIN, 15));
		bt8.addActionListener(new ActionListener() {
	        
			public void actionPerformed(ActionEvent e){
				imp = WindowManager.getCurrentImage();
				PlotbulkROIXANES PlotbulkXANES = new PlotbulkROIXANES();
				PlotbulkXANES.method(imp, strDir, strName);
			}
	        });

		JButton bt9 = new JButton("Save Current Data for TXW");
		bt9.setBounds(20, 160, 240, 30);
		bt9.setFont(new Font("Arial", Font.PLAIN, 15));
		bt9.addActionListener(new ActionListener() {
	        
			public void actionPerformed(ActionEvent e){
				imp = WindowManager.getCurrentImage();
				SavecurrentXANESmap saveXANESmap = new SavecurrentXANESmap();
				saveXANESmap.method(imp, strDir, strName);
			}
	        });

		//フレームにボタンを追加
		MenuFrame.add(btRaw);
		MenuFrame.add(btNormalized);
		MenuFrame.add(bt8);
		MenuFrame.add(bt9);

		IJ.run("Main Window [enter]");
		MenuFrame.setVisible(true);

	}
	  public void windowOpened(WindowEvent e){
		  
	  }
	  
	  public void windowClosing(WindowEvent e){
		  if(btRawName=="Close Raw Data") {
			  closeData crd = new closeData();
			  crd.method(strName, setupMapping.listChName);
		  }
		  if(btNormalizedName=="Close Normalized Data"){
			  closeData cnd = new closeData();
			  cnd.method(strName, setupMapping.listNormalizedName);
		  }
	  }

	  public void windowClosed(WindowEvent e){
		  
	  }
	  
	  public void windowIconified(WindowEvent e){
		  
	  }
	  
	  public void windowDeiconified(WindowEvent e){
		  
	  }
	  
	  public void windowActivated(WindowEvent e){
		  
	  }
	  
	  public void windowDeactivated(WindowEvent e){
		  
	  }

}




