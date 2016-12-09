////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

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

}
