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

import static com.aphidmobile.flip.FlipRenderer.*;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.View;
import com.aphidmobile.utils.TextureUtils;
import com.aphidmobile.utils.UI;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;

import javax.microedition.khronos.opengles.GL10;
import java.lang.ref.WeakReference;

public class ViewDualCards {
	private int index = -1;
	private WeakReference<View> viewRef;
	private Texture texture;

	private Bitmap screenshot;
	private boolean dirty = true;

	private Card topCard = new Card();
	private Card bottomCard = new Card();

	public int getIndex() {
		return index;
	}

	public View getView() {
		return viewRef != null ? viewRef.get() : null;
	}

	public boolean setView(int index, View view) {
		UI.assertInMainThread();
		this.index = index;
		if (getView() == view && (screenshot != null || TextureUtils.isValidTexture(texture)))
			return false;
		viewRef = null;
		if (texture != null) {
			texture.postDestroy();
			texture = null;
		}
		if (view != null) {
			viewRef = new WeakReference<View>(view);
			UI.recycleBitmap(screenshot);
			screenshot = GrabIt.takeScreenshot(view);
		} else {
			UI.recycleBitmap(screenshot);
			Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ALPHA_8);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(0x3C3C3C);
			canvas.save();
			screenshot = bitmap;
		}
		return true;
	}

	public Texture getTexture() {
		return texture;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Card getTopCard() {
		return topCard;
	}

	public Card getBottomCard() {
		return bottomCard;
	}

	public void buildTexture(FlipRenderer renderer, GL10 gl) {
		if (screenshot != null) {
			if (texture != null)
				texture.destroy(gl);
			texture = Texture.createTexture(screenshot, renderer, gl);
			UI.recycleBitmap(screenshot);
			screenshot = null;

			topCard.setTexture(texture);
			bottomCard.setTexture(texture);

			final float viewHeight = texture.getContentHeight();
			final float viewWidth = texture.getContentWidth();
			final float textureHeight = texture.getHeight();
			final float textureWidth = texture.getWidth();
			Log.i("ViewDualCards", "viewHeight: " + viewHeight + ", viewWidth: " + viewWidth + ", textureHeight: " + textureHeight + ", textureWidth:" + textureWidth);

			/*topCard.setCardVertices(new float[]{
				0f, viewHeight, 0f,                     //top left
				0f, viewHeight / 2.0f, 0f,              //bottom left
				viewWidth, viewHeight / 2f, 0f, //bottom right
				viewWidth, viewHeight, 0f       //top right
			});

			topCard.setTextureCoordinates(new float[]{
				0f, 0f,
				0f, viewHeight / 2f / textureHeight,
				viewWidth / textureWidth, viewHeight / 2f / textureHeight,
				viewWidth / textureWidth, 0f
			});

			bottomCard.setCardVertices(new float[]{
				0f, viewHeight / 2f, 0f,                //top left
				0f, 0f, 0f,                             //bottom left
				viewWidth, 0f, 0f,                      //bottom right
				viewWidth, viewHeight / 2f, 0f  		//top right
			});

			bottomCard.setTextureCoordinates(new float[]{
				0f, viewHeight / 2f / textureHeight,
				0f, viewHeight / textureHeight,
				viewWidth / textureWidth, viewHeight / textureHeight,
				viewWidth / textureWidth, viewHeight / 2f / textureHeight
			});*/
			
			topCard.setCardVertices(new float[]{
				0f, viewHeight, 0f, 				//top left
				0f, 0f, 0f, 						//bottom left
				viewWidth/2f, 0f, 0f,				//bottom right
				viewWidth/2f, viewHeight, 0f		//top right
			});
			
			topCard.setTextureCoordinates(new float[]{
				0f, 0f,
				0f, viewHeight/textureHeight, 
				viewWidth/2f/textureWidth, viewHeight/textureHeight,
				viewWidth/2f/textureWidth, 0f
			});
			
			bottomCard.setCardVertices(new float[]{
				viewWidth/2f, viewHeight, 0f,		//top left
				viewWidth/2f, 0f, 0f,				//bottom left
				viewWidth, 0f, 0f,					//bottom right
				viewWidth, viewHeight, 0f			//top right	
			});
			
			bottomCard.setTextureCoordinates(new float[]{
				viewWidth/2f/textureWidth, 0f,
				viewWidth/2f/textureWidth, viewHeight/textureHeight,
				viewWidth/textureWidth, viewHeight/textureHeight,
				viewWidth/textureWidth, 0f
			});			

			checkError(gl);
		}
	}

	public void abandonTexture() {
		texture = null;
	}
}
