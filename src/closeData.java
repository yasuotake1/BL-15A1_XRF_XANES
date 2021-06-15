
/*************************************************
「ProcessedData」のウィンドウを閉じる

現在開かれているウィンドウのリストから、
同じデータセット名で「mu」「norm」の文字を含む
ウィンドウを選んで隠す。

*************************************************/

import ij.*;
//import ij.IJ;
import ij.ImagePlus.*;
import ij.WindowManager.*;
import ij.plugin.PlugIn;

class closeData implements PlugIn {

	public void run(String arg){
		// plugins メニューには表示されないクラス
	}

	public void method(String prefix, String[] list){

		String[] wlist = WindowManager.getImageTitles();		//現在開かれているウィンドウ名を取得
		
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

