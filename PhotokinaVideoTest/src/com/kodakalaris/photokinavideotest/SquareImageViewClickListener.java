package com.kodakalaris.photokinavideotest;

import android.view.ViewGroup;

import com.kodakalaris.photokinavideotest.views.SquareImageView;

public interface SquareImageViewClickListener {
	public void onImageClick(SquareImageView squareImageView, String filePath);

	public ViewGroup getRootView();

	public int getShadowWidth();

	public int getShadowHeight();

	public void onImageDrop(SquareImageView dropSource, SquareImageView dropedOn, boolean isTargetDropable, boolean wasSwap);

	public boolean areViewsDragable();

}
