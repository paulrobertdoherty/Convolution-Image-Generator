package com.nn;

public interface PixelSelector {
	public int getWidth();
	public int getHeight();
	public Color getPixel(int x, int y);
}
