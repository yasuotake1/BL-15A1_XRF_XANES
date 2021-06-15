
/*************************************************
���։��







*************************************************/

import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import ij.WindowManager.*;

import java.io.*;
import java.util.*;

import ij.io.OpenDialog;

import ij.macro.Interpreter;
import ij.measure.ResultsTable;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class CorrelationPlot implements PlugIn {

	public void run(String arg){
		// plugins ���j���[�ɂ͕\������Ȃ��N���X		
	}

	public void method(ArrayList<Integer> idCorrSrc1,ArrayList<Integer> idCorrSrc2){

		String[] wList = WindowManager.getImageTitles();

		GenericDialog gd = new GenericDialog("Correlation plot");
		gd.addChoice("Image 1: ", wList, wList[0]);
		gd.addChoice("Image 2: ", wList, wList[1]);
		gd.showDialog();

		ImagePlus imp1 = WindowManager.getImage(gd.getNextChoice());
		ImagePlus imp2 = WindowManager.getImage(gd.getNextChoice());

		//�ŏ��ɑI�������E�B���h�E��ID���擾���z��ɒǉ�;
		idCorrSrc1.add(imp1.getID());

		//�Q�ԖڂɂɑI�������E�B���h�E��ID���擾���z��ɒǉ�
		idCorrSrc2.add(imp2.getID());

		int idxCorr = idCorrSrc1.size() - 1;

		//�ŏ��ɑI�������E�B���h�E�̊g���q�����
		String t1 = imp1.getTitle();
		t1 = t1.replace(".tif", "");

		//�E�B���h�E�̃T�C�Y���擾
		int w1 = imp1.getWidth();
		int h1 = imp1.getHeight();

		//�z����쐬
		double[] arr1 = new double[w1 * h1];

		//�e�s�N�Z���̒l���擾���z��ɓ����
		for(int i=0; i<h1; i++) {
			for(int j=0; j<w1; j++) {

				float pv1 = imp1.getProcessor().getPixelValue(j, i);
				arr1[(i * w1) + j]=(double)pv1;
			}
		}

		//�Q�ԖڂɑI�������E�B���h�E�̊g���q�����
		String t2 = imp2.getTitle();
		t2 = t2.replace(".tif", "");

		//�E�B���h�E�̃T�C�Y���擾
		int w2 = imp2.getWidth();
		int h2 = imp2.getHeight();

		//�z����쐬
		double[] arr2 = new double[w2 * h2];

		//�e�s�N�Z���̒l���擾���z��ɓ����
		for(int i=0; i<h1; i++) {
			for(int j=0; j<w1; j++) {

				float pv2 = imp2.getProcessor().getPixelValue(j, i);
				arr2[(i * w2) + j]=(double)pv2;
			}
		}

		//�ŏ��ƂQ�Ԗڂ̃E�B���h�E�̃T�C�Y���قȂ鎞�́A
		//�e�z��̈�ԍŌ�̒l���폜���A���[�g���o��

		if(w1 != w2 || h1 != h2) {
			idCorrSrc1.set(idxCorr,0);
			idCorrSrc2.set(idxCorr,0);
			IJ.error("Selected images have different size.");
			return;
		}

		//�摜���̍ŏ��A�ő�l���擾����
//		ImageStatistics stats1 = imp1.getStatistics();
//		ImageStatistics stats2 = imp2.getStatistics();

		//�e�z����̍ŏ��A�ő�l���擾����
		double max1 = arr1[0];
		double min1 = arr1[0];
		for (int i = 1; i < arr1.length; i++) {
		    double v = arr1[i];
		    if (v > max1) {
		        max1 = v;
		    }
		    if (v < min1) {
		        min1 = v;
		    }
		}

		double max2 = arr2[0];
		double min2 = arr2[0];
		for (int i = 1; i < arr2.length; i++) {
		    double v2 = arr2[i];
		    if (v2 > max2) {
		        max2 = v2;
		    }
		    if (v2 < min2) {
		        min2 = v2;
		    }
		}

		//�\��
		Plot p = new Plot("Correlation plot " + String.valueOf(idxCorr) ,t1,t2);
		p.setFrameSize(400,400);
//		p.setLimits(stats1.min, stats1.max, stats2.min, stats2.max);
		p.setLimits(min1, max1, min2, max2);
		p.add("dots", arr1, arr2);
		p.show();

		//�t�@�C���ۑ��̊m�F
		GenericDialog gdd = new GenericDialog("Save ");
		gdd.addMessage("Save XY-data text file? ");
		gdd.showDialog();
		if (gdd.wasCanceled()){
			return;
		}

		//�ۑ�
		OpenDialog od = new OpenDialog("Save As ");
		String dir = od.getDirectory();
		String fileCorr = od.getFileName();
    		try{
			File file = new File(dir + fileCorr);
			FileWriter filewriter = new FileWriter(file);

			String WriteText = t1 + "\t" + t2 + "\r\n";
			filewriter.write(WriteText);

			for(int i=0; i<arr1.length; i++) {
				WriteText = arr1[i] + "\t" + arr2[i] + "\r\n";
				filewriter.write(WriteText);
			}
			filewriter.close();
		}catch(IOException e){
			System.out.println(e);
		}
	}

}


