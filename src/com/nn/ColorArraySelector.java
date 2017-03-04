package com.nn;

public class ColorArraySelector implements PixelSelector {
	private Color[][] array;

	public ColorArraySelector(Color[][] array) {
		this.array = array;
	}

	@Override
	public int getWidth() {
		return array.length;
	}

	@Override
	public int getHeight() {
		return array[0].length;
	}

	@Override
	public Color getPixel(int x, int y) {
		if (array[x][y] == null) {
			System.out.println(x + ", " + y + ", " + array.length + ", " + array[0].length);
		}
		return array[x][y];
	}
}
