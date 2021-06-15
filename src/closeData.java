
/*************************************************
�uProcessedData�v�̃E�B���h�E�����

���݊J����Ă���E�B���h�E�̃��X�g����A
�����f�[�^�Z�b�g���Łumu�v�unorm�v�̕������܂�
�E�B���h�E��I��ŉB���B

*************************************************/

import ij.*;
//import ij.IJ;
import ij.ImagePlus.*;
import ij.WindowManager.*;
import ij.plugin.PlugIn;

class closeData implements PlugIn {

	public void run(String arg){
		// plugins ���j���[�ɂ͕\������Ȃ��N���X
	}

	public void method(String prefix, String[] list){

		String[] wlist = WindowManager.getImageTitles();		//���݊J����Ă���E�B���h�E�����擾
		
		for (int i=0; i<list.length; i++) {
			for (String str : wlist) {
				if(str.startsWith(prefix) && str.endsWith(list[i] + ".tif")) {
					ImagePlus imp = WindowManager.getImage(str);
					imp.hide();
				}
			}
		}
	}
}

