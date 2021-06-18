import ij.*;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class CreateColorBar implements PlugIn {
	ImagePlus impCurrent;
	ImagePlus impBar;
	ImagePlus impFlat;
	
	public static int width = 30;
	public static int height = 256;
	
	public void run(String arg) {
		impCurrent = WindowManager.getCurrentImage();
		if(impCurrent.getBitDepth() == 24)
			IJ.error("Current image is not a single channel (value) image.");
		
		IJ.log("Current image range: min = " + String.valueOf(impCurrent.getDisplayRangeMin()) + ", max = " + String.valueOf(impCurrent.getDisplayRangeMax()));
		
		impBar = NewImage.createImage("ColorBar", width, height, 1, 32, NewImage.FILL_BLACK);
		float[] pixels = (float[])impBar.getProcessor().getPixels();
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				pixels[width * i + j] = (float)(height - 1 - i);
			}
		}
		impBar.setLut(impCurrent.getProcessor().getLut());
		impBar.setDisplayRange(0, height - 1);
		impFlat = impBar.flatten();
		impFlat.setTitle("ColorBar_" + impCurrent.getTitle());
		impFlat.show();
	}
}