import java.awt.Rectangle;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.*;
import ij.plugin.*;

public class XRFXANESTest implements PlugIn {

	public void run(String arg) {
		ImagePlus impFlat = NewImage.createByteImage("Flat", 100, 100, 1, 0);
		impFlat.show();
		
	}
}
