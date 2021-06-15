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

public class Menu_XRF extends JFrame implements WindowListener,PlugIn {


	ArrayList<Integer> idCorrSrc1 = new ArrayList<Integer>();
	ArrayList<Integer> idCorrSrc2 = new ArrayList<Integer>();

	String btRawName="Close Raw Data";
	String btNormalizedName="Close Normalized Data";
	String strDirAndName;
	String strDir;
	String strName;

	ImagePlus imp;

	public void run(String arg) {
		XRFXANESProps prop = setupMapping.ReadProps();
		
		Loadsinglemap singlemap = new Loadsinglemap();
		strDirAndName = singlemap.method(prop.listSuffixes, setupMapping.listChName, setupMapping.listNormalizedName, prop);
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
					//Close processed data
					btNormalizedName="Show Normalized Data";
					closeData cnd = new closeData();
					cnd.method(strName, setupMapping.listNormalizedName);
					IJ.run("Main Window [enter]");
					MenuFrame.toFront();
				}
				btNormalized.setText(btNormalizedName);
			}
	        });
		
		JButton bt3 = new JButton("Correlation Plot...");
		bt3.setBounds(20, 120, 240, 30);
		bt3.setFont(new Font("Arial", Font.PLAIN, 15));
		bt3.addActionListener(new ActionListener() {
	        
			public void actionPerformed(ActionEvent e){
				CorrelationPlot Correlation = new CorrelationPlot();
				Correlation.method(idCorrSrc1,idCorrSrc2);
			}
	        });

		JButton bt4 = new JButton("Create Correlation Mask...");
		bt4.setBounds(20, 160, 240, 30);
		bt4.setFont(new Font("Arial", Font.PLAIN, 15));
		bt4.addActionListener(new ActionListener() {
	        
			public void actionPerformed(ActionEvent e){
				CreateCorrelationMask CorrelationM = new CreateCorrelationMask();
//				CorrelationM.method(idCorrSrc1,idCorrSrc2,arr1,arr2,value);
				CorrelationM.method(idCorrSrc1,idCorrSrc2,prop.zoom);
			}
	        });

		MenuFrame.add(btRaw);
		MenuFrame.add(btNormalized);
		MenuFrame.add(bt3);
		MenuFrame.add(bt4);

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




