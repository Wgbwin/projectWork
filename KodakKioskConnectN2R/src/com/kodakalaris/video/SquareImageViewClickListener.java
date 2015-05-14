package com.kodakalaris.video;

import android.view.ViewGroup;

import com.kodakalaris.video.views.SquareImageView;

/**
 * This interface is used by SquareImageView to notify an activity of
 * events that occured to the view.
 * 
 * If the activity that holds a SquareImageView implements this
 * interface, SquareImageview will call these methods it.
 *
 */
public interface SquareImageViewClickListener {
	public void onImageClick(SquareImageView squareImageView, String filePath);
	
	public void onImageDoubleClick(SquareImageView squareImageView ) ;

	public ViewGroup getRootView();

	public int getShadowWidth();

	public int getShadowHeight();

	public void onImageDrop(SquareImageView dropSource, SquareImageView dropedOn, boolean isTargetDropable, boolean wasSwap);

	public boolean areViewsDragable();

}
