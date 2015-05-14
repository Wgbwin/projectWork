/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.graphics.Camera;
import android.graphics.Matrix;

/**
 * An animation that rotates the view on the Y axis between two specified
 * angles. This animation also adds a translation on the Z axis (depth) to
 * improve the effect.
 */
public class Rotate3dAnimation extends Animation {
	private final float mFromDegrees;
	private final float mToDegrees;
	private final float mCenterX;
	private final float mCenterY;
	private final float mDepthZ;
	private Camera mCamera;
	private int mStepFrom;
	private int mSetpTo;
	private int mViewNo;
	private int mModel;

	/**
	 * Creates a new 3D rotation on the Y axis. The rotation is defined by its
	 * start angle and its end angle. Both angles are in degrees. The rotation
	 * is performed around a center point on the 2D space, definied by a pair of
	 * X and Y coordinates, called centerX and centerY. When the animation
	 * starts, a translation on the Z axis (depth) is performed. The length of
	 * the translation can be specified, as well as whether the translation
	 * should be reversed in time.
	 * 
	 * @param fromDegrees
	 *            the start angle of the 3D rotation
	 * @param toDegrees
	 *            the end angle of the 3D rotation
	 * @param centerX
	 *            the X center of the 3D rotation
	 * @param centerY
	 *            the Y center of the 3D rotation
	 * @param reverse
	 *            true if the translation should be reversed, false otherwise
	 * @param stepFrom
	 *            the step of the page from
	 * @param setpTo
	 *            the step of the page to
	 * @param viewNo
	 *            the number of the view
	 * @param model
	 *            the model of the page will show.
	 */
	public Rotate3dAnimation(float fromDegrees, float toDegrees, float centerX,
			float centerY, float depthZ, int stepFrom, int setpTo, int viewNo,
			int model) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mCenterX = centerX;
		mCenterY = centerY;
		mDepthZ = depthZ;
		mStepFrom = stepFrom;
		mSetpTo = setpTo;
		mViewNo = viewNo;
		mModel = model;
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final float fromDegrees = mFromDegrees;
		float degrees = fromDegrees
				+ ((mToDegrees - fromDegrees) * interpolatedTime);
		final float centerX = mCenterX;
		final float centerY = mCenterY;
		final Camera camera = mCamera;
		final Matrix matrix = t.getMatrix();

		camera.save();

		camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));

		if ((mStepFrom == 1 && mSetpTo == 4)
				|| (mStepFrom == 4 && mSetpTo == 1) || mModel == 2 || mModel == 5) {
			camera.rotateY(degrees);
		} else {
			camera.rotateX(degrees);
		}

		camera.getMatrix(matrix);
		camera.restore();
		if (mStepFrom == 1 && mSetpTo == 2) {
			if (mModel == 1) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);

					break;
				case 2:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;

				case 3:
					//matrix.postTranslate(centerX, centerX*2);
					matrix.postSkew(-0.15f, 0.0f, centerX*2, centerY*2);
					break;
				}
			} else if (mModel == 2) {
				switch (mViewNo) {
				case 1:

					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(0, centerY);

					break;
				case 2:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(0, centerY);
					break;

				case 3:
					matrix.postTranslate(centerX, 0);
					break;
				}
			} 
			else if (mModel == 4 || mModel == 5) {
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
			}

		} else if (mStepFrom == 1 && mSetpTo == 3) {
			if (mModel == 1) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:
					matrix.preTranslate(300, 260);
					break;
				case 3:
					break;
				}
			} else if (mModel == 2) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					matrix.postSkew(0.0f, 0.15f, centerX * 2, centerY * 2);
					break;
				case 3:
					break;
				case 4:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				}
			}

		} else if (mStepFrom == 1 && mSetpTo == 4) {
			switch (mViewNo) {
			case 1:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 2:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				matrix.postSkew(-0.1f, 0.0f, 0, 0);
				break;
			case 3:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				matrix.postSkew(-0.1f, 0.0f, 0, 0);
				break;
			case 4:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);

				break;
			}

		} else if (mStepFrom == 2 && mSetpTo == 1) {
			if (mModel == 1) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 3:
					matrix.setScale(1f, 0.75f);
					matrix.postSkew(0.15f, 0.0f, 0, 0);
					break;
				}
			} else if (mModel == 2) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 3:
					matrix.setScale(0.75f, 1f);
					matrix.postSkew(0.0f, -0.15f, 0, 0);
					break;
				}
			} else if (mModel == 4 || mModel == 5) {
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
			}

		} else if (mStepFrom == 2 && mSetpTo == 3) {
			if (mModel == 1) {
			} else if (mModel == 2) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:
					matrix.postSkew(0.0f, 0.15f, centerX * 2, centerY * 2);
					break;
				case 3:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 4:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				}
			}

		} else if (mStepFrom == 2 && mSetpTo == 4) {
			switch (mViewNo) {
			case 1:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 2:
				matrix.postSkew(-0.15f, 0.0f, centerX, centerY);
				break;
			case 3:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 4:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			}
		} else if (mStepFrom == 3 && mSetpTo == 1) {
			if (mModel == 1) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:

					break;
				case 3:
					matrix.setScale(1f, 0.75f);
					matrix.postSkew(0.15f, 0.0f, 0, 0);
					break;
				}
			} else if (mModel == 2) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:

					break;
				case 3:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					matrix.postSkew(0.0f, -0.15f, 0, 0);
					matrix.postScale(0.75f, 1f);
					/*matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					matrix.setScale(0.75f, 1f);
					matrix.postSkew(0.0f, -0.15f, 0, 0);*/
					break;
				case 4:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				}
			}

		} else if (mStepFrom == 3 && mSetpTo == 2) {
			if (mModel == 1) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:

					break;
				case 3:
					matrix.postSkew(-0.15f, 0.0f, centerX, centerY);
					break;
				}
			} else if (mModel == 2) {
				switch (mViewNo) {
				case 1:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 2:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				case 3:
					matrix.postTranslate(centerX, 0);
					break;
				case 4:
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					break;
				}
			}

		} else if (mStepFrom == 3 && mSetpTo == 4) {
			switch (mViewNo) {
			case 1:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 2:
				matrix.postSkew(-0.1f, 0.0f, 0, 0);
				break;
			case 4:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			}

		} else if (mStepFrom == 4 && mSetpTo == 1) {
			switch (mViewNo) {
			case 1:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 2:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 3:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				matrix.postScale(1f, 0.75f);
				matrix.postSkew(0.15f, 0.0f, 0, 0);
				break;
			case 4:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				matrix.postSkew(0.15f, 0.0f, 0, 0);
				break;
			}

		} else if (mStepFrom == 4 && mSetpTo == 2) {
			switch (mViewNo) {
			case 1:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 2:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 3:
				matrix.preTranslate(0, centerY);
				matrix.postSkew(-0.15f, 0.0f, centerX, centerY);
				break;
			case 4:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			}

		} else if (mStepFrom == 4 && mSetpTo == 3) {
			switch (mViewNo) {
			case 1:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 2:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 3:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			case 4:
				matrix.preTranslate(-centerX, -centerY);
				matrix.postTranslate(centerX, centerY);
				break;
			}

		} else {
			if (mModel == 1) {
				matrix.setScale(1f, 0.75f);
				matrix.postSkew(0.15f, 0.0f, 0, 0);
			} else if (mModel == 2) {
				matrix.setScale(0.75f, 1f);
				matrix.postSkew(0.0f, -0.15f, 0, 0);
			}

		}

	}

}
