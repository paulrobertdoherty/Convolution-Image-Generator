package com.nn;

public class Color {
	@Override
	public String toString() {
		return "Color [red=" + red + ", green=" + green + ", blue=" + blue + "]";
	}

	public double getRed() {
		return red;
	}

	public void setRed(double red) {
		this.red = red;
	}

	public double getGreen() {
		return green;
	}

	public void setGreen(double green) {
		this.green = green;
	}

	public double getBlue() {
		return blue;
	}

	public void setBlue(double blue) {
		this.blue = blue;
	}

	public Color(double red, double green, double blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public Color(java.awt.Color color) {
		this.red = color.getRed();
		this.green = color.getGreen();
		this.blue = color.getBlue();
	}
	
	/**
	 * Makes all the colors within the range
	 * @param a
	 * @return
	 */
	private double cap(double a) {
		return Math.min(255, Math.max(0, a));
	}

	private double red, green, blue;

	public int getInt() {
		return new java.awt.Color((int)cap(red), (int)cap(green), (int)cap(blue)).getRGB();
	}
}
