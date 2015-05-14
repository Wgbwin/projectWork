package com.kodak.rss.tablet.view;


public interface DragTarget{

	
	public Object[] pointToPosition(float xOnScreen, float yOnScreen);
	
	
	public void hideAllFrames();
	
}
