package com.exactprosystems.jf.tool.wizard.related;

import java.awt.image.BufferedImage;

public class ImageAndOffset
{
	public ImageAndOffset(BufferedImage image, int offsetX, int offsetY)
	{
		this.image = image;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public BufferedImage image;
	public int offsetX;
	public int offsetY;
}
