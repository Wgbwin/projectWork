/*
Copyright 2012 Aphid Mobile

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package com.aphidmobile.flip;

import java.lang.ref.WeakReference;

import android.graphics.*;
import android.util.Log;
import android.view.View;
import com.aphidmobile.utils.AphidLog;
import com.kodak.kodak_kioskconnect_n2r.QuickBookFlipperActivity;

public class GrabIt {
	
	private GrabIt() {
	}

	public static Bitmap takeScreenshot(View view) {
		if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
			Bitmap.Config config = Bitmap.Config.ARGB_8888;
			WeakReference<Bitmap> bitmap = null;
			try{
				bitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(view.getWidth(), view.getHeight(), config));
			} catch(OutOfMemoryError oom){
				/*config = Bitmap.Config.ARGB_4444;
				bitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(view.getWidth()/16, view.getHeight()/16, config));*/
				Log.e("---------", "out of memory !!!!!!!!!!!!!!");
				System.gc();
				//QuickBookFlipperActivity.restartQuickBook();
				return null;
			}
			Canvas canvas = new Canvas(bitmap.get());
			view.draw(canvas);
			//canvas.drawColor(Color.RED, PorterDuff.Mode.LIGHTEN); //XXX: debug option

			AphidLog.d("create bitmap %dx%d", view.getWidth(), view.getHeight());

			return bitmap.get();
		} else {
			return null;
		}
	}
}