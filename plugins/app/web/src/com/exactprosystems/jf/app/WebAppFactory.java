////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IGuiDictionary;

public class WebAppFactory implements IApplicationFactory
{
	private static final int requiredMajorVersion = 2;
	private static final int requiredMinorVersion = 2;

	public static final String chromeDriverPathName	= "ChromeDriverPath";
	public static final String ieDriverPathName		= "IEDriverPath";
	public static final String chromeDriverBinary	= "ChromeDriverBinary";

	public final static String browserName 	= "Browser";
	public final static String urlName 		= "URL";

	private static String[] knownParameters = {chromeDriverPathName, ieDriverPathName, chromeDriverBinary};
	
	private static String[] knownStartArgs = { browserName, urlName };

	private static String[] knownConnectArgs = { };

	private static ControlKind[] supportedControls = 
		{ 
			ControlKind.Any, ControlKind.Wait, ControlKind.Button, ControlKind.CheckBox, ControlKind.ComboBox, ControlKind.Dialog,
			ControlKind.Frame, ControlKind.Image, ControlKind.Label, ControlKind.ListView, ControlKind.Menu, ControlKind.MenuItem, ControlKind.Panel,
			ControlKind.ProgressBar, ControlKind.RadioButton, ControlKind.Row, ControlKind.ScrollBar, ControlKind.Slider, ControlKind.Splitter,
			ControlKind.Spinner, ControlKind.Table, ControlKind.TabPanel, ControlKind.TextBox, ControlKind.ToggleButton, 
			ControlKind.Tooltip, ControlKind.Tree, ControlKind.TreeItem,
		};

	private IGuiDictionary dictionary = null;
	
	@Override
	public void init(IGuiDictionary dictionary)
	{
		this.dictionary = dictionary;
	}

	@Override
	public IApplication createApplication()
	{
		return new ProxyWebApp();
	}

	@Override
	public String getRemoteClassName()
	{
		return SeleniumRemoteApplication.class.getCanonicalName();
	}

	@Override
	public String[] wellKnownParameters()
	{
		return knownParameters;
	}

	@Override
	public String[] wellKnownStartArgs()
	{
		return knownStartArgs;
	}

	@Override
	public String[] wellKnownConnectArgs()
	{
		return knownConnectArgs;
	}

	@Override
	public ControlKind[] supportedControlKinds()
	{
		return supportedControls;
	}

	@Override
	public boolean canFillParameter(String parameterToFill)
	{
		return browserName.equals(parameterToFill);
	}

	@Override
	public String[] listForParameter(String parameterToFill)
	{
		switch (parameterToFill)
		{
			case browserName:
				int i = -1;
				String[] result = new String[6];
				result[++i] = "Firefox";
				result[++i] = "Chrome";
				result[++i] = "InternetExplorer";
				result[++i] = "Opera";
				result[++i] = "PhantomJS";
				result[++i] = "Safari";
				return result;

			default:
				return new String[0];
		}
	}
	
	@Override
	public IGuiDictionary getDictionary()
	{
		return this.dictionary;
	}


	//----------------------------------------------------------------------------------------------
	// VersionSupported
	//----------------------------------------------------------------------------------------------
	@Override
	public int requiredMajorVersion()
	{
		return requiredMajorVersion;
	}

	@Override
	public int requiredMinorVersion()
	{
		return requiredMinorVersion;
	}

	@Override
	public boolean isSupported(int major, int minor)
	{
		return (major * 1000 + minor) >= (requiredMajorVersion * 1000 + requiredMinorVersion);
	}
	//----------------------------------------------------------------------------------------------

}
