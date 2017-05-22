package com.exactprosystems.jf.tool.custom;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

public class ServiceLambdaBean
{
	private Supplier<BufferedImage> bufferedImageSupplier;
	private Supplier<Rectangle>     rectangleSupplier;

	public ServiceLambdaBean(Supplier<BufferedImage> bufferedImageSupplier, Supplier<Rectangle> rectangleSupplier)
	{
		this.bufferedImageSupplier = bufferedImageSupplier;
		this.rectangleSupplier = rectangleSupplier;
	}


	public Supplier<BufferedImage> bufferedImageSupplier()
	{
		return bufferedImageSupplier;
	}

	public void setBufferedImageSupplier(Supplier<BufferedImage> bufferedImageSupplier)
	{
		this.bufferedImageSupplier = bufferedImageSupplier;
	}

	public Supplier<Rectangle> rectangleSupplier()
	{
		return rectangleSupplier;
	}

	public void setRectangleSupplier(Supplier<Rectangle> rectangleSupplier)
	{
		this.rectangleSupplier = rectangleSupplier;
	}
}
