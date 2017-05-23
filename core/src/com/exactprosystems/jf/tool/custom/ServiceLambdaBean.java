package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.tool.Common;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class ServiceLambdaBean
{
	private final Common.SupplierWithException<BufferedImage> bufferedImageSupplier;
	private final Common.SupplierWithException<Rectangle>     rectangleSupplier;

	public ServiceLambdaBean(Common.SupplierWithException<BufferedImage> bufferedImageSupplier, Common.SupplierWithException<Rectangle> rectangleSupplier)
	{
		Objects.requireNonNull(bufferedImageSupplier);
		Objects.requireNonNull(rectangleSupplier);
		this.bufferedImageSupplier = bufferedImageSupplier;
		this.rectangleSupplier = rectangleSupplier;
	}

	public Common.SupplierWithException<BufferedImage> bufferedImageSupplier()
	{
		return bufferedImageSupplier;
	}

	public Common.SupplierWithException<Rectangle> rectangleSupplier()
	{
		return rectangleSupplier;
	}
}
