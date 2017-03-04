package com;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.nn.Convolver;

public class Main {

	public static void main(String[] args) {
		try {
			File f = new File("monet.jpg");
			File g = new File("new.png");
			BufferedImage newImage = Convolver.getOutputs(new Random(1234), Convolver.getInputs(ImageIO.read(f)));
			ImageIO.write(newImage, "png", g);
			System.out.println("Done!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}