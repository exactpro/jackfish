////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionInfo 
{
	private static String version = null;
	
	public static String getVersion()
	{
		
		if ( version == null )
		{
			InputStream inStream = VersionInfo.class.getResourceAsStream("version.properties");
			
			if ( inStream != null )
			{
				Properties properties = new Properties();
				
				try
				{
					properties.load(inStream);
					
					version = properties.getProperty("VERSION");
				}
				catch ( IOException e )
				{
					version = "1.0.0.1";
				}
			}
			else
				version = "1.0.0.1";
		}
		
		return version;
	}

}
