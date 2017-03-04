package com.nn;

import java.awt.image.BufferedImage;
import java.util.*;

public class Convolver {
	/**
	 * The size of the matrix that the filters for the convolutional layers use.
	 */
	private static final int FILTER_SIZE = 3;
	
	private static final int ACTUAL_SIZE = FILTER_SIZE - 1;
	
	/**
	 * Increase for faster image production at the expense of quality
	 */
	private static final int MIN_DIFFERENCE = 1;
	
	//The width and height of the most convolved image
	private static int WIDTH = 0, HEIGHT = 0;

	//Remember: make them all add to 1 or 0
	private static final double[][][] MATRICES = new double[][][]{
			//Gaussian blur
			{
			{1/16.0, 1/8.0, 1/16.0},
			{1/8.0, 1/4.0, 1/8.0},
			{1/16.0, 1/8.0, 1/16.0}}, {
						//Sharpen
						{0, -1, 0},
						{-1, 5, -1},
						{0, -1, 0}},
									//Edge detection
									{
									{-1, -1, -1},
									{-1, 8, -1},
									{-1, -1, -1}}
	};
	
	/**
	 * Create a smaller, more filtered image from a pixel selector
	 * @param ps
	 * @return convolved image
	 */
	private static Color[][] toConvolved(PixelSelector ps, double[][] currentFilter) {
		Color[][] toReturn = new Color[(ps.getWidth() - 1) / ACTUAL_SIZE][(ps.getHeight() - 1) / ACTUAL_SIZE];
		//For each pixel
		for (int x = 0; x < toReturn.length; x++) {
			for (int y = 0; y < toReturn[x].length; y++) {
				//Add up all the pixels in the area, then multiply them by the filter and produce a new pixel to return
				Color total = new Color(0, 0, 0);
				for (int i = x; i < x + FILTER_SIZE; i++) {
					for (int j = y; j < y + FILTER_SIZE; j++) {
						total = add(multiply(ps.getPixel((x * ACTUAL_SIZE) + (i - x), (y * ACTUAL_SIZE) + (j - y)), currentFilter[i - x][j - y]), total);
					}
				}
				toReturn[x][y] = total;
			}
		}
		return toReturn;
	}
	
	private static List<Color> add(List<Color> one, List<Color> two) {
		for (int i = 0; i < two.size(); i++) {
			one.add(two.get(i));
		}
		return one;
	}
	
	private static Color add(Color one, Color two) {
		return new Color(one.getRed() + two.getRed(), one.getGreen() + two.getGreen(), one.getBlue() + two.getBlue());
	}

	private static Color multiply(Color pixel, double multiplier) {
		return new Color((int)(pixel.getRed() * multiplier), (int)(pixel.getGreen() * multiplier), (int)(pixel.getBlue() * multiplier));
	}
	
	private static List<Color> getInputs(Color[][][] convolutions, int width, int height, List<Color> inputs) {
		List<Color> toReturn = new ArrayList<Color>();
		for (int i = 0; i < convolutions.length; i++) {
			toReturn = add(toReturn, getInputs(convolutions[i], width, height, inputs));
		}
		return toReturn;
	}

	private static List<Color> getInputs(Color[][] convolutions, int width, int height, List<Color> inputs) {
		if (width >= FILTER_SIZE && height >= FILTER_SIZE) {
			Color[][][] newConvs = new Color[MATRICES.length][][];
			int newWidth = 0;
			int newHeight = 0;
			for (int i = 0; i < MATRICES.length; i++) {
				newConvs[i] = toConvolved(new ColorArraySelector(convolutions), MATRICES[i]);
				newWidth = newConvs[i].length;
				newHeight = newConvs[i][0].length;
			}
			return getInputs(newConvs, newWidth, newHeight, inputs);
		} else {
			WIDTH = width;
			HEIGHT = height;
			List<Color> toReturn = new ArrayList<Color>();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					toReturn.add(convolutions[x][y]);
				}
			}
			return toReturn;
		}
	}
	
	public static Color[] getInputs(final BufferedImage image) {
		PixelSelector bufferedImageSelector = new PixelSelector() {
			@Override
			public int getWidth() {return image.getWidth();}
			@Override
			public int getHeight() {return image.getHeight();}
			@Override
			public Color getPixel(int x, int y) {return new Color(new java.awt.Color(image.getRGB(x, y)));}
		};
		
		//Get the first convolutions from the image
		Color[][][] firstConvolve = new Color[MATRICES.length][][];
		int width = 0;
		int height = 0;
		for (int i = 0; i < firstConvolve.length; i++) {
			firstConvolve[i] = toConvolved(bufferedImageSelector, MATRICES[i]);
			width = firstConvolve[i].length;
			height = firstConvolve[i][0].length;
		}
		
		//Then return the rest
		List<Color> inputs = getInputs(firstConvolve, width, height, new ArrayList<Color>());
		return inputs.toArray(new Color[inputs.size()]);
	}
	
	private static Color[][][] toImage(Color[] outputs) {
		List<Color[][]> list = new ArrayList<Color[][]>();
		int count = 0;
		while (count < outputs.length) {
			Color[][] toAdd = new Color[WIDTH][HEIGHT];
			for (int x = 0; x < WIDTH; x++) {
				for (int y = 0;  y < HEIGHT; y++) {
					toAdd[x][y] = outputs[count];
					count++;
				}
			}
			list.add(toAdd);
		}
		return list.toArray(new Color[list.size()][][]);
	}
	
	/**
	 * A binary search function
	 * @param f
	 * @return
	 */
	private static double search(double min, double max, Function f) {
		double a = min;
		double c = max;
		double b = 0;
		while (Math.abs(c - a) > MIN_DIFFERENCE) {
			b = (a + c) / 2;
			double fa = f.f(a);
			double fc = f.f(c);
			
			if (fa < fc) {
				c = b;
			} else {
				a = b;
			}
		}
		
		return b;
	}
	
	private static double getMin(Color[] answers) {
		double min = answers[0].getRed();
		for (Color c : answers) {
			double minOfColor = Math.min(c.getRed(), Math.min(c.getGreen(), c.getBlue()));
			min = Math.min(minOfColor, min);
		}
		return min;
	}
	
	private static double getMax(Color[] answers) {
		double max = answers[0].getRed();
		for (Color c : answers) {
			double maxOfColor = Math.max(c.getRed(), Math.max(c.getGreen(), c.getBlue()));
			max = Math.max(maxOfColor, max);
		}
		return max;
	}
	
	/**
	 * Finds the total difference between two colors
	 * @param one
	 * @param two
	 * @return
	 */
	private static double dif(Color one, Color two) {
		return Math.abs(one.getRed() - two.getRed()) + Math.abs(one.getGreen() - two.getGreen()) + Math.abs(one.getBlue() - two.getBlue());
	}
	
	private static Color[][] getColors(Random r, final Color[] answers, final Color[][] producedColors) {
		Color[][] toReturn = new Color[FILTER_SIZE][FILTER_SIZE];
		
		//Fill in the pixels with a shade of grey
		double min = getMin(answers);
		double max = getMax(answers);
		double greyVal = search(min, max, new Function() {
			@Override
			public double f(double test) {
				double ans = 0;
				Color grey = new Color(test, test, test);
				Color[][] testColors = new Color[FILTER_SIZE][FILTER_SIZE];
				for (int i = 0; i < answers.length; i++) {
					Color answer = new Color(0, 0, 0);
					for (int x = 0; x < producedColors.length; x++) {
						for (int y = 0; y < producedColors[x].length; y++) {
							if (producedColors[x][y] == null) {
								testColors[x][y] = grey;
							} else {
								testColors[x][y] = producedColors[x][y];
							}
							answer = add(answer, multiply(testColors[x][y], MATRICES[i][x][y]));
						}
					}
					ans += dif(answers[i], answer);
				}
				return ans;
			}
		});
		
		//Set a good greyscale for each color
		/*
		for (int x = 0; x < FILTER_SIZE; x++) {
			for (int y = 0; y < FILTER_SIZE; y++) {
				if (producedColors[x][y] != null) {
					toReturn[x][y] = producedColors[x][y];
				} else {
					final double fx = x;
					final double fy = y;
					final double fgrey = greyVal;
					double color = search(min, max, new Function() {
						@Override
						public double f(double test) {
							double ans = 0;
							Color grey = new Color(test, test, test);
							Color otherGrey = new Color(fgrey, fgrey, fgrey);
							Color[][] testColors = new Color[FILTER_SIZE][FILTER_SIZE];
							for (int i = 0; i < answers.length; i++) {
								Color answer = new Color(0, 0, 0);
								for (int x = 0; x < producedColors.length; x++) {
									for (int y = 0; y < producedColors[x].length; y++) {
										if (producedColors[x][y] == null) {
											if (x == fx && y == fy) {
												testColors[x][y] = grey;
											} else {
												testColors[x][y] = otherGrey;
											}
										} else {
											testColors[x][y] = producedColors[x][y];
										}
										answer = add(answer, multiply(testColors[x][y], MATRICES[i][x][y]));
									}
								}
								ans = dif(answers[i], answer);
							}
							return ans;
						}
					});
					toReturn[x][y] = new Color(color, color, color);
				}
			}
		}
		*/
		
		//Add color to each pixel
		for (int x = 0; x < FILTER_SIZE; x++) {
			for (int y = 0; y < FILTER_SIZE; y++) {
				if (producedColors[x][y] != null) {
					toReturn[x][y] = producedColors[x][y];
				} else {
					final double fx = x;
					final double fy = y;
					//Decides to either change red, green, or blue
					final int index = r.nextInt(3);
					final double fgrey = greyVal;//toReturn[x][y].getRed();
					double color = search(min, max, new Function() {
						@Override
						public double f(double test) {
							double ans = 0;
							Color newColor = getColor(index, fgrey, test);
							Color otherGrey = new Color(fgrey, fgrey, fgrey);
							Color[][] testColors = new Color[FILTER_SIZE][FILTER_SIZE];
							for (int i = 0; i < answers.length; i++) {
								Color answer = new Color(0, 0, 0);
								for (int x = 0; x < producedColors.length; x++) {
									for (int y = 0; y < producedColors[x].length; y++) {
										if (producedColors[x][y] == null) {
											if (x == fx && y == fy) {
												testColors[x][y] = newColor;
											} else {
												testColors[x][y] = otherGrey;
											}
										} else {
											testColors[x][y] = producedColors[x][y];
										}
										answer = add(answer, multiply(testColors[x][y], MATRICES[i][x][y]));
									}
								}
								ans = dif(answers[i], answer);
							}
							return ans;
						}

						private Color getColor(int index, double fgrey, double test) {
							switch (index) {
								case 0:
									return new Color(test, fgrey, fgrey);
								case 1:
									return new Color(fgrey, test, fgrey);
								case 2:
									return new Color(fgrey, fgrey, test);
								default:
									return null;
							}
						}
					});
					toReturn[x][y] = new Color(color, color, color);
				}
			}
		}
		
		return toReturn;
	}
	
	

	private static Color[][] fromConvolved(Random r, Color[][][] convs) {
		List<Color[][]> returnList = new ArrayList<Color[][]>();
		for (int i = 0; i < convs.length; i += MATRICES.length) {
			Color[][] toAdd = new Color[(convs[0].length * ACTUAL_SIZE) + 1][(convs[0][0].length * ACTUAL_SIZE) + 1];
			for (int x = 0; x < convs[0].length; x++) {
				for (int y = 0 ; y < convs[0][0].length; y++) {
					Color[] answers = new Color[MATRICES.length];
					for (int j = 0; j < MATRICES.length; j++) {
						answers[j] = convs[i + j][x][y];
					}
					
					//Get the pixels already produced
					Color[][] producedColors = new Color[FILTER_SIZE][FILTER_SIZE];
					int startX = x * ACTUAL_SIZE;
					int startY = y * ACTUAL_SIZE;
					for (int a = startX; a < startX + FILTER_SIZE; a++) {
						for (int b = startY; b < startY + FILTER_SIZE; b++) {
							producedColors[a - startX][b - startY] = toAdd[a][b];
						}
					}
					
					//Add the new colors to the new image
					Color[][] newColors = getColors(r, answers, producedColors);
					for (int a = 0; a < FILTER_SIZE; a++) {
						for (int b = 0; b < FILTER_SIZE; b++) {
							toAdd[a + startX][b + startY] = newColors[a][b];
						}
					}
					
				}
			}
			returnList.add(toAdd);
		}
		
		//Either return an image or merge them together
		Color[][][] toReturn = returnList.toArray(new Color[returnList.size()][][]);
		if (toReturn.length >= MATRICES.length) {
			return fromConvolved(r, toReturn);
		} else {
			return toReturn[0];
		}
	}
	
	public static BufferedImage getOutputs(Random r, Color[] outputs) {
		Color[][][] convs = toImage(outputs);
		Color[][] finalImage = fromConvolved(r, convs);
		BufferedImage buffered = new BufferedImage(finalImage.length, finalImage[0].length, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < buffered.getWidth(); x++) {
			for (int y = 0; y < buffered.getHeight(); y++) {
				buffered.setRGB(x, y, finalImage[x][y].getInt());
			}
		}
		System.out.println("Image produced!");
		return buffered;
	}
}