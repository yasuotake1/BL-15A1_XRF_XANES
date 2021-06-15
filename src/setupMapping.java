import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class setupMapping implements PlugIn {

//	String[] listSuffixes = {"_Cnt0.txt", "_Cnt1.txt", "_Cnt2.txt", "_Cnt3.txt", "_Cnt4.txt", "_Cnt5.txt", "_Cnt6.txt", "_Cnt7.txt","_Cnt8.txt","_Cnt9.txt","","","","","",""};
	public static String[] listSuffixes = {"_CH0.txt", "_CH1.txt", "_CH2.txt", "_CH3.txt", "_CH4.txt", "_CH5.txt", "_CH6.txt", "_CH7.txt","_CH8.txt","_CH9.txt","","","","","",""};
	public static String[] listChName = {"i0","i1","SCA1","SCA2","SCA3","SCA4","SCA5","SCA6","SCA7","ICR","AUX1","AUX2","AUX3","AUX4","AUX5","AUX6"};
	public static String[] listNormalizedName = {"mu","SCA1norm","SCA2norm","SCA3norm","SCA4norm","SCA5norm","SCA6norm","SCA7norm","ICRnorm","AUX1norm","AUX2norm","AUX3norm","AUX4norm","AUX5norm","AUX6norm"};
	public static String[] listScale = {"None", "pulse", "mm", "um"};
	public static int stageConf = 0;
	public static String scaleConf = "pulse";
	public static double pulsePerMMX = 2000;
	public static double pulsePerMMY = 2000;
	public static double zoom = 200; 
	public static boolean[] listUse = {true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
	public static String defaultDir = "";
	public static String PropPath = "plugins/BL-15A1_XRF_XANES/prop.config";

	public void run(String arg) {
		String[] listStage = {"Default", "Reversed"};

		XRFXANESProps readProps = setupMapping.ReadProps();
		
		GenericDialog gd = new GenericDialog("Setup XRF/XANES mapping");

		String strGuide1="Enter the file suffixes.";
		strGuide1+="\r\nLeave the field empty to ignore its channel.";

		gd.addMessage(strGuide1);

		for(int i=0; i<16; i++) {
			gd.addStringField(listChName[i] + ": ", readProps.listSuffixes[i],10);
		}

		String strGuide3="Note: i0 and i1 will be used to calculate mu = ln(i0/i1).";
		strGuide3+="\r\nSCA1-8 and AUX1-6 will be normalized by i/i0.";

		gd.addMessage(strGuide3);

		if(readProps.stageConf == 1){
			gd.addRadioButtonGroup("Stage configuration: ",listStage,1,2,listStage[1]);
		}
		else{
			gd.addRadioButtonGroup("Stage configuration: ",listStage,1,2,listStage[0]);
		}
		
		gd.addChoice("Image scaling: ", listScale, readProps.scaleConf);

		gd.addNumericField("Default zoom (%): ", readProps.zoom,1);

		gd.showDialog();
		if (gd.wasCanceled())
			return;
		
		XRFXANESProps target = new XRFXANESProps();
		for(int i=0; i<16; i++) {
			target.listSuffixes[i] = gd.getNextString();
		}
		if(gd.getNextRadioButton() == listStage[0]){
			target.stageConf = 0;
		}
		else{
			target.stageConf = 1;
		}
		
		target.scaleConf = gd.getNextChoice();
		target.pulsePerMMX = readProps.pulsePerMMX;
		target.pulsePerMMY = readProps.pulsePerMMY;
		target.zoom = gd.getNextNumber();
		target.listUse = readProps.listUse;
		
		setupMapping.WriteProps(target);
	}

	public static XRFXANESProps ReadProps() {
		Properties prop = new Properties();

		InputStream is;
		XRFXANESProps target = new XRFXANESProps();
		try {
			is = new FileInputStream(new File(PropPath));
			prop.load(is);
			is.close();

			target.listSuffixes = prop.getProperty("listSuffixes").split(",", 16);
			target.stageConf = Integer.parseInt(prop.getProperty("stageConf"));
			target.scaleConf = prop.getProperty("scaleConf");
			target.pulsePerMMX = Double.parseDouble(prop.getProperty("pulsePerMMX"));
			target.pulsePerMMY = Double.parseDouble(prop.getProperty("pulsePerMMY"));
			target.zoom = Double.parseDouble(prop.getProperty("zoom"));
			String[] temp_listUse = prop.getProperty("listUse").split(",", 16);
			for(int i=0; i<temp_listUse.length; i++) {
				target.listUse[i] = Boolean.parseBoolean(temp_listUse[i]);
			}
			target.defaultDir = prop.getProperty("defaultDir");
			
		} catch (FileNotFoundException e) {
			// デフォルト値を使用する
			target.listSuffixes = listSuffixes;
			target.stageConf = stageConf;
			target.scaleConf = scaleConf;
			target.pulsePerMMX = pulsePerMMX;
			target.pulsePerMMY = pulsePerMMY;
			target.zoom = zoom;
			target.listUse = listUse;
			target.defaultDir = defaultDir;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return target;

	}
	
	public static void WriteProps(XRFXANESProps target) {
		Properties prop = new Properties();
		String[] temp_listUse = new String[target.listUse.length];
		for(int i=0; i<temp_listUse.length; i++) {
			temp_listUse[i] = String.valueOf(target.listUse[i]);
		}

		prop.setProperty("listSuffixes", String.join(",", target.listSuffixes));
		prop.setProperty("stageConf", String.valueOf(target.stageConf));
		prop.setProperty("scaleConf", String.valueOf(target.scaleConf));
		prop.setProperty("pulsePerMMX", String.valueOf(target.pulsePerMMX));
		prop.setProperty("pulsePerMMY", String.valueOf(target.pulsePerMMY));
		prop.setProperty("zoom", String.valueOf(target.zoom));
		prop.setProperty("listUse", String.join(",", temp_listUse));
		prop.setProperty("defaultDir", String.valueOf(target.defaultDir));
		try {
			Prefs.savePrefs(prop, setupMapping.PropPath);
		} catch (IOException e) {
			IJ.error("Failed to write properties.");
		}
	}
	
	public static double[] getScanInfo(String dir, String prefix, XRFXANESProps prop) {
		double[] scale = new double[4];
		double xStep = Double.NaN;
		double yStep = Double.NaN;
		double xOrigin = Double.NaN;
		double yOrigin = Double.NaN;
		
		String suffix = "_ScanInfo.txt";
		ArrayList<String> linesInfo = new ArrayList<String>();
		boolean bRev = false;
		if(prop.stageConf==0){
			bRev=false;
		}else{
			bRev=true;
		}
		
		if(!(prop.scaleConf).equals("None")) {
			String path = dir + prefix + suffix;
			File file = new File(path);
			if (!file.exists()) {
				path = dir + prefix + "_qscan_1" + suffix;
				file = new File(path);
			}
			try{	
				FileReader in = new FileReader(path);
				BufferedReader br = new BufferedReader(in);
				String line;
						
				while ((line = br.readLine()) != null) {
					linesInfo.add(line);
				}
				br.close();
				in.close();
				
				String[] arrInfoX = (linesInfo.get(1)).split(",",0);
				String[] arrInfoY = (linesInfo.get(3)).split(",",0);
				if(arrInfoX.length == 4 && arrInfoY.length == 4) {
					String[] arrxStep = arrInfoX[2].split("=",0);
					xStep = Double.parseDouble(arrxStep[1]);
					String[] arryStep = arrInfoY[2].split("=",0);
					yStep = Double.parseDouble(arryStep[1]);	
					
					switch (prop.scaleConf) {
					case "pulse":
						if(!bRev) {
							xStep = (-1) * xStep;
							String[] arrxEnd = arrInfoX[1].split("=",0);
							xOrigin = Double.parseDouble(arrxEnd[1]) / xStep * (-1);
						} else {
							String[] arrxStart = arrInfoX[0].split("=",0);
							xOrigin = Double.parseDouble(arrxStart[1]) / xStep * (-1);
						}
						yStep = (-1) * yStep;	
						String[] arryEnd = arrInfoY[1].split("=",0);
						yOrigin = Double.parseDouble(arryEnd[1]) / yStep * (-1);
						break;
					
					case "mm":
						xStep = Math.abs(xStep) / prop.pulsePerMMX;
						yStep = Math.abs(yStep) / prop.pulsePerMMY;
						xOrigin = 0;
						yOrigin = 0;
						break;
					
					case "um":
						xStep = Math.abs(xStep) / prop.pulsePerMMX * 1000;
						yStep = Math.abs(yStep) / prop.pulsePerMMY * 1000;
						xOrigin = 0;
						yOrigin = 0;
						break;
					}
				}

	        } catch(IOException e) {
				System.err.println(e.getMessage());
	        }
		}
		
		scale[0] = xStep;
		scale[1] = yStep;
		scale[2] = xOrigin;
		scale[3] = yOrigin;
		return scale;
	}
	
}
