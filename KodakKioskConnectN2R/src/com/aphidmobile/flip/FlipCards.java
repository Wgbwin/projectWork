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

import java.util.Date;

import android.os.Handler;
import android.util.Log;
import android.view.*;
import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.TextureUtils;

import javax.microedition.khronos.opengles.GL10;

public class FlipCards {
	private static final float ACCELERATION = 0.618f;
	private static final float TIP_SPEED = 1f;
	private static final float MOVEMENT_RATE = 0.8f;
	private static final int MAX_TIP_ANGLE = 0;

	private static final int STATE_INIT = 0;
	private static final int STATE_TOUCH = 1;
	private static final int STATE_AUTO_ROTATE = 2;
	private static final int STATE_AUTO_FLIP = 3;

	private ViewDualCards frontCards;

	public ViewDualCards getFrontCards() {
		return frontCards;
	}
	private ViewDualCards backCards;

	private float angle = 0f;
	private boolean forward = true;
	private int animatedFrame = 0;
	private int state = STATE_INIT;

	//private float lastY = -1;
	private float lastX = -1;

	@SuppressWarnings("unused")
	private VelocityTracker velocityTracker;
	private FlipViewController controller;

	private int activeIndex = -1;
	
	private boolean visible = false;

	public FlipCards(FlipViewController controller) {
		this.controller = controller;
		
		frontCards = new ViewDualCards();
		backCards = new ViewDualCards();
		resetAxises();
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		controller.getSurfaceView().requestRender();
	}

	public void reloadTexture(int frontIndex, View frontView, int backIndex, View backView) {
		synchronized (this) {
			if (frontView != null) {
				if (backCards.getView() == frontView) {
					frontCards.setView(-1, null);
					swapCards();
				}
			}

			if (backView != null) {
				if (frontCards.getView() == backView) {
					backCards.setView(-1, null);
					swapCards();
				}
			}

			boolean frontChanged = frontCards.setView(frontIndex, frontView);
			boolean backChanged = backCards.setView(backIndex, backView);
			if(cardsWidth == 0){
				cardsWidth = frontCards.getView().getWidth();
			}

			if (AphidLog.ENABLE_DEBUG)
				AphidLog.d("reloading texture: %s and %s; old views: %s, %s, front changed %s, back changed %s", frontView, backView, frontCards.getView(), backCards.getView(), frontChanged, backChanged);

			if (frontIndex == activeIndex) {
				if (angle >= 180)
					angle -= 180;
				else if (angle < 0)
					angle += 180;
			} else if (backIndex == activeIndex) {
				if (angle < 0)
					angle += 180;
			}

//			AphidLog.i("View changed: front (%d, %s), back (%d, %s), angle %s, activeIndex %d", frontIndex, frontView, backIndex, backView, angle, activeIndex);
		}
	}

	public void rotateBy(float delta) {
		angle += delta;
		if (backCards.getIndex() == -1) {
			if (angle >= MAX_TIP_ANGLE)
				angle = MAX_TIP_ANGLE;
		}

		if (angle > 180)
			angle = 180;
		else if (angle < 0)
			angle = 0;
	}
	public void setState(int state) {
		if (this.state != state) {
			this.state = state;
			animatedFrame = 0;
		}
	}

	private static String tag = FlipCards.class.getSimpleName();
	private boolean isAutoFlip = false;
	public synchronized void draw(FlipRenderer renderer, GL10 gl) {		
		applyTexture(renderer, gl);

		if (!TextureUtils.isValidTexture(frontCards.getTexture()) && !TextureUtils.isValidTexture(backCards.getTexture()))
			return;
		
		if (!visible){
			return;
		}

		switch (state) {
			case STATE_INIT: {
				/*if (false) { //XXX: debug only
					if (angle >= 180)
						forward = false;
					else if (angle <= 0)
						forward = true;

					rotateBy((forward ? TIP_SPEED : -TIP_SPEED));
					if (angle > 90 && angle <= 180 - MAX_TIP_ANGLE) {
						forward = true;
					} else if (angle < 90 && angle >= MAX_TIP_ANGLE) {
						forward = false;
					}
				}*/
			}
			break;
			case STATE_TOUCH:
				break;
			case STATE_AUTO_ROTATE: {
				animatedFrame++;
				rotateBy((forward ? ACCELERATION : -ACCELERATION) * animatedFrame);
				
				if (angle >= 180 || angle <= 0) {
					setState(STATE_INIT);
					if (angle >= 180) { //flip to next page
						if (backCards.getIndex() != -1) {
							activeIndex = backCards.getIndex();
							controller.postFlippedToView(activeIndex);
						} else
							angle = 180;
					}
					controller.postHideFlipAnimation(false);
				} else
					controller.getSurfaceView().requestRender();
			}
			break;
			case STATE_AUTO_FLIP:
				if(!isAutoFlip){
					rotateBy(forward ? 0:180);
				}
				animatedFrame++;
				rotateBy((forward ? ACCELERATION : -ACCELERATION) * animatedFrame);
				
				if(forward && !isAutoFlip){
					isAutoFlip = true;
				} else if (!forward && !isAutoFlip){
					isAutoFlip = true;
				}
				if (angle >= 180 || angle <= 0) {
					setState(STATE_INIT);
					if(forward){
						swapCards();
					}
					if(!forward){
						
					} else {
						if(frontCards.getIndex() < controller.getAdapter().getCount()){
							activeIndex = frontCards.getIndex();
							controller.postFlippedToView(activeIndex);
						}
					}
					isAutoFlip = false;
					angle = 0;
					controller.postHideFlipAnimation(false);
				} else {
					controller.getSurfaceView().requestRender();
				}
				break;
			default:
				AphidLog.e("Invalid state: " + state);
				break;
		}

		if (angle < 90) { //render front view over back view
			frontCards.getTopCard().setAngle(0);
			frontCards.getTopCard().draw(gl);

			backCards.getBottomCard().setAngle(0);
			backCards.getBottomCard().draw(gl);

			frontCards.getBottomCard().setAngle(angle);
			frontCards.getBottomCard().draw(gl);
		} else { //render back view first
			frontCards.getTopCard().setAngle(0);
			frontCards.getTopCard().draw(gl);

			backCards.getTopCard().setAngle(180 - angle);
			backCards.getTopCard().draw(gl);

			backCards.getBottomCard().setAngle(0);
			backCards.getBottomCard().draw(gl);
		}
	}

	public void invalidateTexture() {
		frontCards.abandonTexture();
		backCards.abandonTexture();
	}

	private long downTime = 0;
	private float downPostionX = 0f;
	private int cardsWidth = 0;
	public synchronized boolean handleTouchEvent(MotionEvent event, boolean isOnTouchEvent) {
		float delta;

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				lastX = event.getX();
				downTime = new Date().getTime();
				downPostionX = event.getX();
				return isOnTouchEvent;
			case MotionEvent.ACTION_MOVE:
				if(controller.hasPageSelected){
					return false;
				}
				delta = lastX - event.getX();
				if (Math.abs(delta) > controller.getTouchSlop())
					setState(STATE_TOUCH); //XXX: initialize views?
				if (state == STATE_TOUCH) {
					controller.showFlipAnimation();
					
					final float angleDelta = 180 * delta / controller.getContentHeight() * MOVEMENT_RATE;
					angle += angleDelta;
					if (backCards.getIndex() == -1) {
						if (angle >= MAX_TIP_ANGLE)
							angle = MAX_TIP_ANGLE;
					} else if (backCards.getIndex() == 0) {
						if (angle <= 180 - MAX_TIP_ANGLE)
							angle = 180 - MAX_TIP_ANGLE;
					}
					if (angle < 0) {
						if (frontCards.getIndex() > 0) {
							activeIndex = frontCards.getIndex() - 1; //xxx
							controller.flippedToView(activeIndex);
						} else {
							swapCards();
							frontCards.setView(-1, null);
							if (-angle >= MAX_TIP_ANGLE)
								angle = -MAX_TIP_ANGLE;
							angle += 180;
						}
					}
					lastX = event.getX();
					controller.getSurfaceView().requestRender();
					return true;
				}

				return isOnTouchEvent;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				long time = new Date().getTime()-downTime;
				float distance = event.getX() - downPostionX;
				if(time < 400 && distance>-50 && distance<50){					
					if(controller.getCurrentView() != null){
						return controller.getCurrentView().performClick();
					}
					
					if(controller.hasPageSelected && controller.getCurrentView()!=null){
						controller.getCurrentView().performClick();
						return isOnTouchEvent;
					}
					if(frontCards == null||frontCards.getView()==null){
						return isOnTouchEvent;
					}
//					if(event.getX()<frontCards.getView().getWidth()/2){
//						Log.i(tag, "onSingleTapUp left");
//						forward = false;
//						if(frontCards.getIndex()>0){
//							Log.i(tag, "ACTION_UP frontCards.getIndex()>0");
//							activeIndex = frontCards.getIndex();
//							Log.i(tag, forward + " " + activeIndex);
//							controller.postFlippedToView(activeIndex-1);
//							swapCards();
//						} else {
//							return isOnTouchEvent;
//						}						
//					} else {
//						Log.i(tag, "onSingleTapUp right");
//						if(backCards.getIndex() == -1){
//							return isOnTouchEvent;
//						}
//						forward = true;
//					}
//					controller.showFlipAnimation();
//					setState(STATE_AUTO_FLIP);
//					controller.getSurfaceView().requestRender();
//					return isOnTouchEvent;
				}
				if (state == STATE_TOUCH) {
					delta = lastX - event.getX();
					rotateBy(180 * delta / controller.getContentHeight() * MOVEMENT_RATE);
					forward = angle >= 90;
					setState(STATE_AUTO_ROTATE);
					controller.getSurfaceView().requestRender();
				}				
				return isOnTouchEvent;
		}

		return false;
	}

	private void resetAxises() {
		frontCards.getTopCard().setAxis(Card.AXIS_TOP);
		frontCards.getBottomCard().setAxis(Card.AXIS_TOP);
		backCards.getBottomCard().setAxis(Card.AXIS_TOP);
		backCards.getTopCard().setAxis(Card.AXIS_BOTTOM);
	}
	
	private void swapCards() {
		ViewDualCards tmp = frontCards;
		frontCards = backCards;
		backCards = tmp;
		resetAxises();
	}
	
	private void applyTexture(FlipRenderer renderer, GL10 gl) {
		frontCards.buildTexture(renderer, gl);
		backCards.buildTexture(renderer, gl);
	}
	
	public void showAutoFlip(int index){
		forward = true;
		controller.showFlipAnimation();
		setState(STATE_AUTO_FLIP);
		controller.getSurfaceView().requestRender();
	}
}