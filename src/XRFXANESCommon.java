import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ij.*;
import ij.plugin.PlugIn;

public class XRFXANESCommon implements PlugIn {

	public static final double hc = 12398.52;
	public static final double SpacSi111 = 3.13551;
	public static final String keyEnergies = "Energies";

	public void run(String arg) {
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
		if (prop.stageConf == 0) {
			bRev = false;
		} else {
			bRev = true;
		}

		if (!(prop.scaleConf).equals("None")) {
			String path = dir + prefix + suffix;
			File file = new File(path);
			if (!file.exists()) {
				path = dir + prefix + "_qscan_1" + suffix;
				file = new File(path);
			}
			try {
				FileReader in = new FileReader(path);
				BufferedReader br = new BufferedReader(in);
				String line;

				while ((line = br.readLine()) != null) {
					linesInfo.add(line);
				}
				br.close();
				in.close();

				String[] arrInfoX = (linesInfo.get(1)).split(",", 0);
				String[] arrInfoY = (linesInfo.get(3)).split(",", 0);
				if (arrInfoX.length == 4 && arrInfoY.length == 4) {
					String[] arrxStep = arrInfoX[2].split("=", 0);
					xStep = Double.parseDouble(arrxStep[1]);
					String[] arryStep = arrInfoY[2].split("=", 0);
					yStep = Double.parseDouble(arryStep[1]);

					switch (prop.scaleConf) {
					case "pulse":
						if (!bRev) {
							xStep = (-1) * xStep;
							String[] arrxEnd = arrInfoX[1].split("=", 0);
							xOrigin = Double.parseDouble(arrxEnd[1]) / xStep * (-1);
						} else {
							String[] arrxStart = arrInfoX[0].split("=", 0);
							xOrigin = Double.parseDouble(arrxStart[1]) / xStep * (-1);
						}
						yStep = (-1) * yStep;
						String[] arryEnd = arrInfoY[1].split("=", 0);
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

			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

		scale[0] = xStep;
		scale[1] = yStep;
		scale[2] = xOrigin;
		scale[3] = yOrigin;
		return scale;
	}

	/**
	 * Reads photon energies from XAFS 9809 file format.
	 * @param path
	 * @return Array of photon energies or null if failed.
	 */
	public static double[] readEnergies9809(Path path) {
		List<String> rows = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(path.toString()));
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty())
					rows.add(line);
			}
			br.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
		boolean is9809 = (rows.get(0)).trim().startsWith("9809");
		if (is9809) {
			do {
				rows.remove(0);
			} while (!(rows.get(0)).startsWith("    Offset"));
		} else {
			return null;
		}
		double[] angles = new double[rows.size() - 1];
		double[] energies = new double[rows.size() - 1];
		for (int i = 0; i < angles.length; i++) {
			angles[i] = Double.parseDouble((rows.get(i + 1)).substring(0, 10).trim());
			energies[i] = AtoE(angles[i]);
		}
		return energies;
	}

	public static double AtoE(double angle) {
		return hc / (2 * SpacSi111 * Math.sin(angle / 180 * Math.PI));
	}

	public static double EtoA(double ene) {
		return Math.asin(hc / (2 * SpacSi111 * ene)) / Math.PI * 180;
	}

	public static boolean checkEnergyStack(ImagePlus imp) {
		if (imp.getNSlices() < 2) {
			IJ.error("This is not an imagestack.");
			return false;
		} else if (imp.getNSlices() != ImagingXAFSCommon.getPropEnergyPoints(imp)) {
			IJ.error("Energy is not correctly set.");
			return false;
		} else {
			return true;
		}
	}

	public static void setPropEnergies(ImagePlus imp, double[] energies) {
		String[] temp_energies = new String[energies.length];
		for (int i = 0; i < temp_energies.length; i++) {
			temp_energies[i] = String.format("%.2f", energies[i]);
		}
		imp.setProp(keyEnergies, String.join(",", temp_energies));
	}

	public static double[] getPropEnergies(ImagePlus imp) {
		String[] temp_energies = imp.getProp(keyEnergies).split(",", 1000);
		double[] energies = new double[temp_energies.length];
		for (int i = 0; i < energies.length; i++) {
			energies[i] = Double.parseDouble(temp_energies[i]);
		}
		return energies;
	}
}