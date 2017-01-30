////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import javax.imageio.ImageIO;
import com.exactprosystems.jf.api.common.Storable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class ImageWrapper implements Storable
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
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{" + this.description + ":" + this.width + "x" + this.height + "}";
	}

	@Override
	public String getName() 
	{
		if (this.fileName == null)
		{
			return "";
		}
		return new File(this.fileName).getName();
	}
	
	@Override
	public List<String> getFileList() 
	{
		String name = getFileName();
		if (name == null)
		{
			name = getDescription();
		}
		if (name == null)
		{
			name = "image";
		}
		
		return Arrays.asList(name);
	}

	@Override
	public byte[] getData(String file) throws IOException 
	{
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
				ByteArrayOutputStream temp = new ByteArrayOutputStream())
		{
			ImageIO.write(getImage(), "jpg", temp);
			return temp.toByteArray();
		}
	}

	@Override
	public void addFile(String file, byte[] data) throws IOException 
	{
		setDescription(file);
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data))
		{
			BufferedImage bi = ImageIO.read(bais);
			this.width = bi.getWidth();
			this.height = bi.getHeight();
			this.pixels = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
		}
	}

	public ImageWrapper cutImage(int x1, int y1, int x2, int y2)
	{
		BufferedImage bi = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		bi.setRGB(0, 0, this.width, this.height, this.pixels, 0, this.width);
		return new ImageWrapper(bi.getSubimage(x1, y1, x2-x1, y2-y1));
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

	public File saveToDir(String dirName) throws IOException
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

    public void clearFile()
    {
        this.fileName = null;
    }

    public String getName(String reportDir) throws IOException
    {
        if (getFileName() == null)
        {
            saveToDir(reportDir);
        }
        return new File(getFileName()).getName();
    }

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
	
	
	private String	fileName	= null;
	private String 	description	= null;
}