package com.kodak.flip;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * class to record how the image is edited.
 */
public class EditParam {

	// only with paper size know can we generate the same image.
	// note that the paper means virtual paper in CropView.
	// it helps us to scale the 'transX' & 'transY' param.
	//public float paperHeight = 10.0f;
	public float transX = 0.0f;
	public float transY = 0.0f;
	public float scale = 1.0f;
	public float rotate = 0;
	public Matrix matrix = new Matrix();
	public Matrix rotateMatrix = null;
	public float offSetX = 0.0f;
	public float offSetY = 0.0f;
	public float imageEditPanFactorX = 0.0f;
	public float imageEditPanFactorY = 0.0f;
	private boolean valid = false;
	public RectF dst = new RectF();

	public void setValid(boolean b) {
		valid = b;
	}

	public boolean isValid() {
		return valid;
	}

	public void clone(EditParam ep) {
		//ep.paperHeight = paperHeight;
		ep.transX = transX;
		ep.transY = transY;
		ep.scale = scale;
		ep.matrix = matrix;
		ep.valid = valid;
		ep.offSetX = offSetX;
		ep.offSetY = offSetY;
		ep.rotateMatrix = rotateMatrix;
	}

}
