import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import ij.WindowManager.*;

import ij.io.OpenDialog;
import java.io.File;

public class showData implements PlugIn {

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}

	public void method(String dir, String prefix, String[] list, XRFXANESProps prop){

		String[] wlist = WindowManager.getImageTitles();

		boolean bRev = false;
		if(prop.stageConf==0){
			bRev=false;
		}else{
			bRev=true;
		}
		
		double[] scale = setupMapping.getScanInfo(dir, prefix, prop);

		for(int i=0; i<list.length; i++) {
			boolean match = false;
			for(String str : wlist) {
				if (str.startsWith(prefix) && str.endsWith(list[i] + ".tif")){
					match = true;
				}
			}
			if (match==false){
				String path = dir + prefix + "_" + list[i] + ".tif";
				File file = new File(path);
				if(file.exists()) {
					ImagePlus impp = IJ.openImage(path);
					impp.show();
					if(!Double.isNaN(scale[0]) && !Double.isNaN(scale[1]) && !Double.isNaN(scale[2]) && !Double.isNaN(scale[3])){
						IJ.run("Properties...", "unit=" + prop.scaleConf + " pixel_width=" + scale[0] + " pixel_height=" + scale[1] + " origin=" + scale[2] + "," + scale[3]);
					} else {
						IJ.run("Properties...", "unit=pixel pixel_width=1 pixel_height=1 origin=0,0");
					}
					
					IJ.run("Set... ", "zoom=" + prop.zoom);
					IJ.run("Scale to Fit", "");
				}
			}
		}
		
		IJ.run("Tile");
	}
}

