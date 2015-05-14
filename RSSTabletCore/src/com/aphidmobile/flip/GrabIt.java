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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.aphidmobile.utils.AphidLog;
import com.kodak.rss.core.util.Log;

public class GrabIt {
  private static final String TAG = "GrabIt";

  private GrabIt() {
  }

	/**
	 * Note:This method is not perfect for background, but is enough for current requirements(background is repeat horizontal)
	 * @param view
	 * @param config
	 * @param bgDrawable
	 * @param offsets
	 * @return
	 */
	public synchronized static Bitmap takeScreenshot(View view, Bitmap.Config config, Drawable bgDrawable, int[] offsets) {
		int width = view.getWidth();
		int height = view.getHeight();

		if (view != null && width > 0 && height > 0) {
			try {
				if(!Bitmap.Config.ARGB_8888.equals(config)){
					//make width to multiply 4
					//This can fix skew issue for opengl.
					//Or you can add gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1) in opengl.
					//but it will add run-time
					//The value 1 for GL10.GL_UNPACK_ALIGNMENT means the width should be multiply 1 not 4.
					if(width %4 != 0){
						width = width + 4 -width%4;
					}
				}
				Bitmap bitmap = Bitmap.createBitmap(width, height, config);
				Canvas canvas = new Canvas(bitmap);
				if (bgDrawable != null) {
					// has background drawable, then draw it on the canvas
					canvas.save();
					canvas.translate(- (offsets[0]<0? 0 : offsets[0]), -offsets[1]);
					bgDrawable.draw(canvas);
					
					canvas.restore();
					
					int bgWidth = bgDrawable.getBounds().width();
					if(bgWidth < width){
						canvas.save();
						//bgWidth/2 is not exact, but is enough
						canvas.translate(bgWidth/2, -offsets[1]);
						bgDrawable.draw(canvas);
						canvas.restore();
					}
				} else {
					// does not have background drawable, then draw color on the
					// canvas
					canvas.drawColor(FlipViewController.BACKGROUND_COLOR);
				}

				if (view.getBackground() != null) {
					view.getBackground().draw(canvas);
				}

				view.draw(canvas);

				// canvas.drawColor(Color.RED, PorterDuff.Mode.DARKEN); //NOTES:
				// debug option

				if (AphidLog.ENABLE_DEBUG) {
					AphidLog.d("create bitmap %dx%d, format %s", width, height,
							config);
				}
				
				return bitmap;

			} catch (OutOfMemoryError oom) {
				Log.e(TAG, "Out of memeory when takeScreenShot for photobook flip", oom);
				System.gc();
				return null;
			}

		} else {
			return null;
		}
	}
  
	public synchronized static Bitmap takeScreenshot(View view,	Bitmap.Config config) {
		int width = view.getWidth();
		int height = view.getHeight();

		if (view != null && width > 0 && height > 0) {
			try {
				Bitmap bitmap = Bitmap.createBitmap(width, height, config);
				Canvas canvas = new Canvas(bitmap);
				Drawable bgDrawable = view.getBackground();
				if (bgDrawable != null)
					// has background drawable, then draw it on the canvas
					bgDrawable.draw(canvas);
				else
					// does not have background drawable, then draw color on the
					// canvas
					canvas.drawColor(FlipViewController.BACKGROUND_COLOR);
				view.draw(canvas);

				// canvas.drawColor(Color.RED, PorterDuff.Mode.DARKEN); //NOTES:
				// debug option

				if (AphidLog.ENABLE_DEBUG) {
					AphidLog.d("create bitmap %dx%d, format %s", width, height,
							config);
				}
				return bitmap;

			} catch (OutOfMemoryError oom) {
				Log.e(TAG, oom);
				return null;
			}

		} else {
			return null;
		}
	}
  
}
