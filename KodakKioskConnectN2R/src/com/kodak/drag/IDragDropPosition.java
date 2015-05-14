package com.kodak.drag;

public interface IDragDropPosition {
	
	public void dropOnGrid(int x, int y, int offsetY);
	
	public void resetPostionIndicator(int x, int y);
	
	public void scroll(int x, int y, int upScrollBounce, int downScrollBounce);
}
