////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class ImageWrapper implements Serializable
{

	private static final long serialVersionUID = 4378018644528294988L;
	private int					width;
	private int					height;
	private int[]				pixels;

	public ImageWrapper()
	{
		this.width = 1;
		this.height = 1;
		this.pixels = new int[] {0};
	}

	public ImageWrapper(int width, int height, int[] buffer)
	{
		this.width = width;
		this.height = height;
		this.pixels = buffer;
	}

	public ImageWrapper(BufferedImage bi)
	{
		this.width = bi.getWidth();
		this.height = bi.getHeight();
		this.pixels = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
	}

	public BufferedImage getImage()
	{
		BufferedImage bi = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		bi.setRGB(0, 0, this.width, this.height, this.pixels, 0, this.width);
		return bi;
	}

	public void saveToFile(String fileName) throws Exception
	{
		this.fileName = fileName;
		try (OutputStream os = new FileOutputStream(new File(fileName)))
		{
			ImageIO.write(this.getImage(), "jpg", os);
		}
	}

	public File saveToDir(String dirName) throws Exception
	{
		File file = new File(dirName);
		File temp = null;
		if (!file.exists())
		{
			file.mkdir();
		}

		if (file.isDirectory())
		{
			temp = File.createTempFile("img", ".jpg", file);
			try (OutputStream os = new FileOutputStream(temp))
			{
				ImageIO.write(this.getImage(), "jpg", os);
			}
			this.fileName = temp.getPath();
		}
		return temp;
	}

	public String getFileName()
	{
		return this.fileName;
	}


	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
	
	@Override
	public String toString()
	{
		return (this.description == null ? getClass().getSimpleName() : this.description )+ "[" + this.width + "x" + this.height + "]";
	}

	
	
	private String	fileName	= null;
	private String 	description	= null;
}