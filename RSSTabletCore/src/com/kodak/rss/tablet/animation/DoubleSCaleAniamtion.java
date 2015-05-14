package com.kodak.rss.tablet.animation;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class DoubleSCaleAniamtion extends Animation{	
	private List<AnimationStatus> mAniList;
	float centerX,centerY;

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);					
		setInterpolator(new LinearInterpolator());		
		centerX = width*1f/2;
		centerY = height*1f/2;		
	}
	
	public DoubleSCaleAniamtion(ArrayList<AnimationStatus> statusList) {
		mAniList = statusList;		
	}	

	@Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
		if (mAniList == null ) return;
		if (mAniList.size() < 1) return;
        int index1 = 0;
        int index2 = 1;
              
        for (int i = 0; i < mAniList.size() - 1; i++) {
            if (interpolatedTime >= mAniList.get(i).Time && interpolatedTime <= mAniList.get(i + 1).Time) {
                index1 = i;
                index2 = i + 1;
                break;
            }
        }       
        AnimationStatus keyStatus1 = mAniList.get(index1);
        AnimationStatus keyStatus2 = mAniList.get(index2);
        if (keyStatus1 == null || keyStatus2 == null)return;

        Matrix matrix = t.getMatrix();        
        float sx1 = keyStatus1.Scale_X;
        float sy1 = keyStatus1.Scale_Y;
        float sx2 = keyStatus2.Scale_X;
        float sy2 = keyStatus2.Scale_Y;
        float totelTime = keyStatus2.Time - keyStatus1.Time;        
        float sx = sx1 + (sx2 - sx1) * interpolatedTime / totelTime;
        float sy = sy1 + (sy2 - sy1) * interpolatedTime / totelTime;

        matrix.setScale(sx, sy);
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);              
	}
	
}
