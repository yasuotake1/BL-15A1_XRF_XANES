import ij.*;
import ij.plugin.*;
import java.io.*;
import java.util.*;
import ij.io.*;

public class SaveCurrentXANESmap implements PlugIn {

	public void run(String arg) {
		// plugins メニューには表示されないクラス
	}

	public void method(ImagePlus imp, String dir, String eneFile){

		String fName = imp.getTitle();
		int[] Dimensions = imp.getDimensions();
		int nSlices = Dimensions[3];

		if(nSlices < 2 || (fName).indexOf("_mu") == -1 && (fName).indexOf("_SCA") == -1 && (fName).indexOf("_AUX") == -1){
			IJ.error("Current image is not a XANES map! ");
			return;
		}

		String[] labels = new String[nSlices];
		ImageStack stack = new ImageStack();
		
		for(int i=0; i<nSlices; i++) {
			imp.setSlice(i + 1);
			stack = imp.getStack();
			labels[i] = stack.getSliceLabel(imp.getCurrentSlice());
		}

		File file = new File(dir + eneFile);
		if(!file.exists()) {
			OpenDialog od = new OpenDialog("Choose an energy list file...");
			dir = od.getDirectory();
			if (dir==null){
				return;
			}
			eneFile = od.getFileName();
		}

		ArrayList<String> rows = new ArrayList<String>();

		try{	
			FileReader in = new FileReader(dir + eneFile);
			BufferedReader br = new BufferedReader(in);
			String line;
			
			while ((line = br.readLine()) != null) {
				if(!line.trim().isEmpty())
					rows.add(line);
			}
			br.close();
			in.close();
		} catch(IOException e) {
	            System.err.println(e.getMessage());
	            IJ.error(e.getMessage());
	            return;
		}
		boolean is9809 = (rows.get(0)).trim().startsWith("9809");
		if(is9809){
			do{
				rows.remove(0);
			}while(!(rows.get(0)).startsWith("    Offset"));
		}
		float[] angles = new float[rows.size() - 1];
		float[] energies = new float[rows.size() - 1];

		for(int i=0; i<energies.length; i++) {
			if(is9809){
				angles[i] = Float.parseFloat((rows.get(i + 1)).substring(0, 10).trim());
				energies[i] = (float) (12398.52 / (2 * 3.13551 * Math.sin(angles[i] / 180 * Math.PI)));
			} else {
				String[] columns = (rows.get(i + 1)).split(",");
				energies[i] = Float.parseFloat(columns[0]);
			}
			if(Float.isNaN(energies[i])){
				IJ.error("Invalid energy list file.");
			}
		}

		if(labels.length != energies.length){
			IJ.error("Invalid energy list file. ");
		}
		String dirTXW = dir + "TXW" + File.separator;
		File target = new File(dirTXW);
		if(!target.exists()){
			if(!target.mkdir())
				IJ.error("Unable to create directory!");
		}

		String prefix = fName.replace(".tif", "") + "_";

		String suffix = "_eV.tif";
//		IJ.run("Duplicate...", "title=_" + fName + " duplicate range=1-" + nSlices);
//		IJ.run("Stack to Images");

		for(int i=0; i<labels.length; i++) {
			ImagePlus imp2 = new ImagePlus(stack.getSliceLabel(i + 1), stack.getProcessor(i + 1));
			FileSaver fs = new FileSaver(imp2);
			fs.saveAsTiff(dirTXW + prefix + String.format("%08.2f", energies[i]) + suffix);
			imp2.close();
		}

	}
}

