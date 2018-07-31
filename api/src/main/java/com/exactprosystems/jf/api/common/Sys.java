/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.common;

import com.exactprosystems.jf.api.common.i18n.R;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Sys
{
    @HideAttribute
    public static String jfDir = ".";
    
	@DescriptionAttribute(text = R.SYS_CURRENT_PROCESS_ID_DESCRIPTION)
	public static int currentProcessId()
	{
		return ProcessTools.currentProcessId();
	}

	@DescriptionAttribute(text = R.SYS_HOST_NAME)
	public static String hostName()
	{
		String hostname = "Unknown";

		try
		{
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		}
		catch (UnknownHostException ex)
		{
			// nothing to do
		}
		return hostname;
	}

	@DescriptionAttribute(text = R.SYS_USER_NAME)
	public static String userName()
	{
		return System.getProperty("user.name");
	}


	@DescriptionAttribute(text = R.SYS_OS_NAME)
	public static String osName()
	{
		return System.getProperty("os.name");
	}

	@DescriptionAttribute(text = R.SYS_COPY_TO_CLIPBOARD)
	public static void copyToClipboard(@FieldParameter(name = "text") String text)
	{
		StringSelection stringSelection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
	}

	@DescriptionAttribute(text = R.SYS_GET_FROM_CLIPBOARD)
	public static String getFromClipboard()
	{
		try
		{
			Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
			Clipboard systemClipboard = defaultToolkit.getSystemClipboard();
			Object data = systemClipboard.getData(DataFlavor.stringFlavor);
			return String.valueOf(data);
		}
		catch (Exception e)
		{
			return "";
		}
	}

	@DescriptionAttribute(text = R.SYS_EXISTS)
	public static boolean exists(@FieldParameter(name = "path") String path)
	{
		return Files.exists(Paths.get(path), LinkOption.NOFOLLOW_LINKS);
	}

	@DescriptionAttribute(text = R.SYS_COPY_FILE)
	public static void copyFile(@FieldParameter(name = "pathFrom") String pathFrom, @FieldParameter(name = "pathTo") String pathTo) throws IOException
	{
		Files.copy(Paths.get(pathFrom), Paths.get(pathTo), StandardCopyOption.COPY_ATTRIBUTES);
	}

	@DescriptionAttribute(text = R.SYS_MOVE_FILE)
	public static void moveFile(@FieldParameter(name = "pathFrom") String pathFrom, @FieldParameter(name = "pathTo") String pathTo) throws IOException
	{
		Files.move(Paths.get(pathFrom), Paths.get(pathTo), StandardCopyOption.ATOMIC_MOVE);
	}
	
    @DescriptionAttribute(text = R.SYS_WRITE_FILE)
    public static void writeFile(@FieldParameter(name = "buf") byte[] buf, @FieldParameter(name = "pathTo") String pathTo) throws IOException
    {
        try (InputStream in = new ByteArrayInputStream(buf))
        {
            Files.copy(in, Paths.get(pathTo), StandardCopyOption.REPLACE_EXISTING);
        }
    }
	
    @DescriptionAttribute(text = R.SYS_READ_FILE)
    public static byte[] readFile(@FieldParameter(name = "pathFrom") String pathFrom) throws IOException
    {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            Files.copy(Paths.get(pathFrom), out);
            return out.toByteArray();
        }
    }
    
    @DescriptionAttribute(text = R.SYS_JF_DIR)
    public static String jfDir()
    {
        return jfDir;
    }
}
