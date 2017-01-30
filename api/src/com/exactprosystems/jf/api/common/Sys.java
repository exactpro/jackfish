////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Sys
{
    @DescriptionAttribute(text = "Returns current host name")
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

    @DescriptionAttribute(text = "Returns current user name")
    public static String userName()
    {
        return System.getProperty("user.name");
    }

    
    @DescriptionAttribute(text = "Returns OS name")
    public static String osName()
    {
    	return System.getProperty("os.name");
    }

    @DescriptionAttribute(text = "Save @text to clipboard")
	public static void copyToClipboard(String text)
	{
		StringSelection stringSelection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
	}

	@DescriptionAttribute(text = "Return text from clipboard")
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
}
