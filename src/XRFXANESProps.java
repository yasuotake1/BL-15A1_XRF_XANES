import ij.plugin.PlugIn;

public class XRFXANESProps implements PlugIn {
	public String[] listSuffixes = new String[16];
	public int stageConf;
	public String scaleConf;
	public double pulsePerMMX;
	public double pulsePerMMY;
	public double zoom;
	public boolean[] listUse = new boolean[16];
	public String defaultDir;
	public double[] normalizationParam;

	public void run(String arg) {
	}

	public double getPreStart() {
		return normalizationParam[0];
	}

	public double getPreEnd() {
		return normalizationParam[1];
	}

	public double getPostStart() {
		return normalizationParam[2];
	}

	public double getPostEnd() {
		return normalizationParam[3];
	}
}
