import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import ij.io.OpenDialog;
import ij.measure.CurveFitter;
import ij.measure.Measurements;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;


public class PlotbulkROIXANES implements PlugIn {

	static boolean hasData = false;
	static Plot plot = new Plot("Bulk/ROI XANES", "Photon energy (eV)", "Intensity or absorption");
	static XANESMapPlotWindow window;
	static final Color[] colors = { new Color(0x8b0000), new Color(0x8b8b00), new Color(0x008b00), new Color(0x008b8b),
			new Color(0x00008b), new Color(0x8b008b), Color.DARK_GRAY, Color.BLACK };
	static int idxColor = 0;
	static final String styleData = "connected circle";
	static final String styleFit = "line";
	static List<String> labels = new ArrayList<String>();
	static List<Integer> idxColors = new ArrayList<Integer>();
	static List<double[]> energies = new ArrayList<double[]>();
	static List<double[]> intensities = new ArrayList<double[]>();
	static List<double[]> normalized = new ArrayList<double[]>();
	static List<double[]> coefs = new ArrayList<double[]>();
	static CurveFitter cf;

	public void run(String arg) {

	}

	public static void addPlots(ImagePlus imp, int idxOffset, String dir, String eneFile) {
		int slc = imp.getNSlices();
		double[] arrEnergy = XRFXANESCommon.getPropEnergies(imp);
		if (arrEnergy == null) {
			File file = new File(dir + eneFile);
			if (!file.exists()) {
				OpenDialog od = new OpenDialog("Choose an energy list file...");
				dir = od.getDirectory();
				if (dir == null) {
					return;
				}
				eneFile = od.getFileName();
			}
			arrEnergy = XRFXANESCommon.readEnergies9809(Paths.get(dir + eneFile));
			if (arrEnergy == null) {
				ArrayList<String> rows = new ArrayList<String>();
				try {
					BufferedReader br = new BufferedReader(new FileReader(dir + eneFile));
					String line;
					while ((line = br.readLine()) != null) {
						if (!line.isBlank())
							rows.add(line);
					}
					br.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				arrEnergy = new double[rows.size() - 1];
				for (int i = 0; i < arrEnergy.length; i++) {
					String[] columns = (rows.get(i + 1)).split(",");
					arrEnergy[i] = Double.parseDouble(columns[0]);
					if (Double.isNaN(arrEnergy[i])) {
						IJ.error("Invalid energy list file.");
						return;
					}
				}
			}
		}
		
		if (imp.getRoi().isArea()) {
			double[] arrInt = new double[slc];
			for (int i = 0; i < slc; i++) {
				imp.setSlice(i + 1);
				ImageStatistics stats = imp.getStatistics(Measurements.MEAN);
				arrInt[i] = stats.mean;
			}
			addData(arrEnergy, arrInt, "ROI " + String.valueOf(1 + idxOffset));

		} else {
			Point[] points = imp.getRoi().getContainedPoints();
			for (int j = 0; j < points.length; j++) {
				double[] arrInt = new double[slc];
				for (int i = 0; i < slc; i++) {
					imp.setSlice(i + 1);
					arrInt[i] = (double) imp.getProcessor().getPixelValue(points[j].x, points[j].y);
				}
				addData(arrEnergy, arrInt, "Point " + String.valueOf(1 + idxOffset + j));
			}
		}
	}
	
	public static boolean hasData() {
		return hasData;
	}

	private static void addData(double[] energy, double[] intensity, String label) {
		hasData = true;
		plot.restorePlotObjects();

		labels.add(label);
		idxColors.add(idxColor);
		energies.add(energy);
		intensities.add(intensity);

		plot.setColor(colors[idxColor], colors[idxColor]);
		idxColor = (idxColor + 1) % colors.length;
		plot.add(styleData, energy, intensity);

		plot.savePlotObjects();
	}

	public static void redrawData() {
		plot.restorePlotObjects();
		show(true);
	}

	static void addNormalizationLines() {
		if (!hasData)
			return;
		plot.restorePlotObjects();

		double eStart, eEnd;
		for (int i = 0; i < labels.size(); i++) {
			eStart = energies.get(i)[0];
			eEnd = energies.get(i)[energies.get(i).length - 1];
			double[] xPrePost = { eStart, eEnd };
			double[] yPre = { getPreEdgeLineValue(i, eStart), getPreEdgeLineValue(i, eEnd) };
			double[] yPost = { getPostEdgeLineValue(i, eStart), getPostEdgeLineValue(i, eEnd) };
			plot.setColor(colors[idxColors.get(i)]);
			plot.drawLine(xPrePost[0], yPre[0], xPrePost[1], yPre[1]);
			plot.drawLine(xPrePost[0], yPost[0], xPrePost[1], yPost[1]);
		}
		show(true);

	}

	static void drawSubtractedData() {
		if (!hasData)
			return;
		plot.restorePlotObjects();

		for (int i = 0; i < labels.size(); i++) {
			double[] intensity = new double[energies.get(i).length];
			for (int j = 0; j < intensity.length; j++) {
				intensity[j] = intensities.get(i)[j] - getPreEdgeLineValue(i, energies.get(i)[j]);
			}
			plot.setColor(colors[idxColors.get(i)], colors[idxColors.get(i)]);
			plot.replace(i, styleData, energies.get(i), intensity);

		}
		show(true);

	}

	static void drawNormalizedData() {
		if (!hasData)
			return;
		plot.restorePlotObjects();

		normalized.clear();
		for (int i = 0; i < labels.size(); i++) {
			double[] intensity = new double[energies.get(i).length];
			for (int j = 0; j < intensity.length; j++) {
				intensity[j] = (intensities.get(i)[j] - getPreEdgeLineValue(i, energies.get(i)[j]))
						/ (getPostEdgeLineValue(i, energies.get(i)[j]) - getPreEdgeLineValue(i, energies.get(i)[j]));
			}
			normalized.add(intensity);
			plot.setColor(colors[idxColors.get(i)], colors[idxColors.get(i)]);
			plot.replace(i, styleData, energies.get(i), intensity);

		}
		show(true);

	}

	static boolean drawLinearCombination() {
		if (!hasData)
			return false;

		if (!ImagingXAFSSVD.setStandards(energies.get(0),false))
			return false;
		ImagingXAFSSVD.setDataMatrix(normalized);
		ImagingXAFSSVD.doSVD(false);

		IJ.log("Linear combination result:");
		String log = "               ";// fifteen blanks
		for (int i = 0; i < ImagingXAFSSVD.numComponents(); i++) {
			log += String.format("%-15s", ImagingXAFSSVD.getNames(15).get(i));
		}
		IJ.log(log);
		double[] weight;
		for (int i = 0; i < labels.size(); i++) {
			log = String.format("%-15s", labels.get(i));
			weight = ImagingXAFSSVD.getCoefsAt(i);
			for (int j = 0; j < weight.length; j++) {
				log += String.format("%15.3f", weight[j]);
			}
			IJ.log(log);
			plot.setColor(colors[idxColors.get(i)]);
			plot.add(styleFit, energies.get(i), ImagingXAFSSVD.getCurveAt(i));
		}
		plot.setLimitsToFit(true);
		return true;
	}

	static void calcCoefs(double[] normalizationParam) {
		if (!hasData || normalizationParam.length != 4)
			return;

		coefs.clear();
		for (int i = 0; i < labels.size(); i++) {
			int[] indices = ImagingXAFSCommon.searchNormalizationIndices(energies.get(i), normalizationParam[0],
					normalizationParam[1], normalizationParam[2], normalizationParam[3]);
			if (indices == null)
				return;
			double[] coef = new double[4];
			cf = new CurveFitter(Arrays.copyOfRange(energies.get(i), indices[0], indices[1]),
					Arrays.copyOfRange(intensities.get(i), indices[0], indices[1]));
			cf.doFit(CurveFitter.STRAIGHT_LINE);
			coef[0] = cf.getParams()[0];
			coef[1] = cf.getParams()[1];
			cf = new CurveFitter(Arrays.copyOfRange(energies.get(i), indices[2], indices[3]),
					Arrays.copyOfRange(intensities.get(i), indices[2], indices[3]));
			cf.doFit(CurveFitter.STRAIGHT_LINE);
			coef[2] = cf.getParams()[0];
			coef[3] = cf.getParams()[1];
			coefs.add(coef);
		}

	}

	static double getPreEdgeLineValue(int idx, double ene) {
		return coefs.get(idx)[0] + coefs.get(idx)[1] * ene;
	}

	static double getPostEdgeLineValue(int idx, double ene) {
		return coefs.get(idx)[2] + coefs.get(idx)[3] * ene;
	}

	public static void resetIdxColor() {
		idxColor = 0;
	}

	public static void show(boolean enableNormalization) {
		plot.setColor(Color.black);
		plot.addLegend(String.join("\t", labels));

		if (window == null) {
			window = new XANESMapPlotWindow(plot, enableNormalization);
		} else {
			plot.update();
		}
		plot.setLimitsToFit(true);
	}

	public static void clear() {
		if (window != null) {
			window.close();
			window = null;
		}
		plot = new Plot("ImagingXAFS plot", "Photon energy (eV)", "Intensity or absorption");
		hasData = false;
		idxColor = 0;
		labels.clear();
		idxColors.clear();
		energies.clear();
		intensities.clear();
		normalized.clear();
		coefs.clear();
	}

	@SuppressWarnings("serial")
	static class XANESMapPlotWindow extends PlotWindow implements ItemListener {
		private static Choice choice;
		private final String[] choices = { "Raw data", "Show pre- and post-edge lines", "Subtract pre-edge",
				"Normalized", "Linear combination of standards" };
		private static int selectedIndexOld;
		private final Label labelPreStart = new Label("Pre-edge from");
		private final Label labelPreEnd = new Label("  to");
		private final Label labelPostStart = new Label("       Post-edge from");
		private final Label labelPostEnd = new Label("  to");
		private static TextField tfPreStart, tfPreEnd, tfPostStart, tfPostEnd;

		XANESMapPlotWindow(Plot plot, boolean enableNormalization) {
			super(plot.getImagePlus(), plot);
			if (enableNormalization)
				addNormalizationPanel();
		}

		private void addNormalizationPanel() {
			ImagingXAFSProps prop = ImagingXAFSCommon.ReadProps();
			Panel panel = new Panel();
			panel.setLayout(new BorderLayout());
			choice = new Choice();
			for (int i = 0; i < choices.length; i++) {
				choice.add(choices[i]);
			}
			panel.add("North", choice);
			choice.addItemListener(this);
			Panel panel2nd = new Panel();
			panel2nd.setLayout(new FlowLayout(FlowLayout.LEFT));
			panel2nd.add(labelPreStart);
			tfPreStart = new TextField(String.format("%.2f", prop.normalizationParam[0]), 7);
			panel2nd.add(tfPreStart);
			panel2nd.add(labelPreEnd);
			tfPreEnd = new TextField(String.format("%.2f", prop.normalizationParam[1]), 7);
			panel2nd.add(tfPreEnd);
			panel2nd.add(labelPostStart);
			tfPostStart = new TextField(String.format("%.2f", prop.normalizationParam[2]), 7);
			panel2nd.add(tfPostStart);
			panel2nd.add(labelPostEnd);
			tfPostEnd = new TextField(String.format("%.2f", prop.normalizationParam[3]), 7);
			panel2nd.add(tfPostEnd);
			panel.add("South", panel2nd);
			add(panel);
			pack();

			selectedIndexOld = 0;

		}

		public void itemStateChanged(ItemEvent e) {
			int selectedIndex = choice.getSelectedIndex();

			if (selectedIndex == 0) {
				redrawData();

			} else {
				Double preStart = getPreStart();
				Double preEnd = getPreEnd();
				Double postStart = getPostStart();
				Double postEnd = getPostEnd();
				if (preStart.isNaN() || preEnd.isNaN() || postStart.isNaN() || postEnd.isNaN()) {
					choice.select(selectedIndexOld);
					return;
				}
				if (preStart > preEnd || preEnd > postStart || postStart > postEnd) {
					IJ.error("Invalid pre-edge and post-edge region.");
					choice.select(selectedIndexOld);
					return;
				}

				ImagingXAFSProps prop = ImagingXAFSCommon.ReadProps();
				prop.normalizationParam = new double[] { preStart, preEnd, postStart, postEnd };
				calcCoefs(prop.normalizationParam);
				if (coefs.size() == 0) {
					choice.select(selectedIndexOld);
					return;
				}

				if (selectedIndex == 1) {
					addNormalizationLines();
				}

				if (selectedIndex == 2) {
					drawSubtractedData();
				}

				if (selectedIndex == 3) {
					drawNormalizedData();
				}

				if (selectedIndex == 4) {
					drawNormalizedData();
					if (!drawLinearCombination()) {
						choice.select(selectedIndexOld);
						return;
					}
				}

				if (selectedIndex > 4) {
					choice.select(selectedIndexOld);
					return;
				}
			}
			selectedIndexOld = selectedIndex;

		}

		static Double getValue(String str) {
			Double d;
			if (str == null)
				d = Double.NaN;
			try {
				d = Double.parseDouble(str);
			} catch (NumberFormatException e) {
				d = Double.NaN;
			}
			return d;
		}

		static Double getPreStart() {
			Double d;
			if (tfPreStart == null)
				d = Double.NaN;
			else
				d = getValue(tfPreStart.getText());
			return d;
		}

		static Double getPreEnd() {
			Double d;
			if (tfPreEnd == null)
				d = Double.NaN;
			else
				d = getValue(tfPreEnd.getText());
			return d;
		}

		static Double getPostStart() {
			Double d;
			if (tfPostStart == null)
				d = Double.NaN;
			else
				d = getValue(tfPostStart.getText());
			return d;
		}

		static Double getPostEnd() {
			Double d;
			if (tfPostEnd == null)
				d = Double.NaN;
			else
				d = getValue(tfPostEnd.getText());
			return d;
		}
	}
}
