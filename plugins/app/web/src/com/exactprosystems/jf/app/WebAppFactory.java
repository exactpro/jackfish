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
import com.exactprosystems.jf.api.app.LocatorFieldKind;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.exactprosystems.jf.api.common.ParametersKind;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class WebAppFactory implements IApplicationFactory
{
	public static final String logLevel				= "LogLevel";
	public final static String jreExecName 			= "jreExec";
	public final static String jreArgsName 			= "jreArgs";

	public static final String safariDriverPathName	= "SafariDriverPath";
	public static final String chromeDriverPathName	= "ChromeDriverPath";
	public static final String geckoDriverPathName	= "GeckoDriverPath";
	public static final String ieDriverPathName		= "IEDriverPath";
	public static final String chromeDriverBinary	= "ChromeDriverBinary";
	public static final String firefoxProfileDir	= "FirefoxProfileDirectory";
	public static final String usePrivateMode       = "UsePrivateMode";

	public final static String browserName 			= "Browser";
	public final static String urlName 				= "URL";

	public static final String propertyUrlName		= "URL";
	public static final String propertyTitle		= "Title";
	
	private static String[] empty = {  };

	private static ControlKind[] supportedControls = 
		{ 
			ControlKind.Any, ControlKind.Wait, ControlKind.Button, ControlKind.CheckBox, ControlKind.ComboBox, ControlKind.Dialog,
			ControlKind.Frame, ControlKind.Image, ControlKind.Label, ControlKind.ListView, ControlKind.Menu, ControlKind.MenuItem, ControlKind.Panel,
			ControlKind.ProgressBar, ControlKind.RadioButton, ControlKind.Row, ControlKind.ScrollBar, ControlKind.Slider, ControlKind.Splitter,
			ControlKind.Spinner, ControlKind.Table, ControlKind.TabPanel, ControlKind.TextBox, ControlKind.ToggleButton, 
			ControlKind.Tooltip, ControlKind.Tree, ControlKind.TreeItem,
		};

	private IGuiDictionary dictionary = null;



	//----------------------------------------------------------------------------------------------
	// IFactory
	//----------------------------------------------------------------------------------------------
	@Override
	public String[] wellKnownParameters(ParametersKind kind)
	{
		switch (kind)
		{
			case LOAD:		return new String[] { jreExecName, jreArgsName, safariDriverPathName, chromeDriverPathName, geckoDriverPathName, ieDriverPathName, chromeDriverBinary, firefoxProfileDir,
					usePrivateMode, logLevel };
			case START:		return new String[] { browserName, urlName };
			case PROPERTY:	return new String[] { propertyUrlName, propertyTitle };
			default:		return empty;	
		}
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
				String[] result = new String[]
						{
							"AndroidChrome",
							"AndroidBrowser",
							"Firefox",
							"Chrome",
							"InternetExplorer",
							"Opera",
							"PhantomJS",
							"Safari",
						};
				return result;

			default:
				return new String[0];
		}
	}

	//----------------------------------------------------------------------------------------------
	// IApplicationFactory
	//----------------------------------------------------------------------------------------------
	@Override
	public String getHelp()
	{
		StringBuilder builder = new StringBuilder();
		try
		{
			try (Scanner in = new Scanner(WebAppFactory.class.getResourceAsStream(helpFileName)))
			{
				while (in.hasNext())
				{
					builder.append(in.nextLine());
				}
			}
		}
		catch (Exception e)
		{
			builder = new StringBuilder("Help not found");
		}
		return builder.toString();
	}

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
	public ControlKind[] supportedControlKinds()
	{
		return supportedControls;
	}

	@Override
	public IGuiDictionary getDictionary()
	{
		return this.dictionary;
	}

    @Override
    public PluginInfo getInfo()
    {
        Map<ControlKind, String[]>      controlMap = new LinkedHashMap<>();

        add(controlMap, ControlKind.Any,           "*");
        add(controlMap, ControlKind.Button,        "button", "input", "a", "img");
        add(controlMap, ControlKind.CheckBox,      "button", "input");
        add(controlMap, ControlKind.ComboBox,      "select", "input");
        add(controlMap, ControlKind.Dialog,        "form");
        add(controlMap, ControlKind.Frame,         "form", "body", "frame", "iframe");
        add(controlMap, ControlKind.Image,         "img");
        add(controlMap, ControlKind.Label,         "label", "span");
        add(controlMap, ControlKind.MenuItem,      "li");
        add(controlMap, ControlKind.Panel,         "div");
        add(controlMap, ControlKind.ProgressBar,   "progress");
        add(controlMap, ControlKind.RadioButton,   "input");
        add(controlMap, ControlKind.Row,           "tr");
        add(controlMap, ControlKind.ScrollBar,     "*");
        add(controlMap, ControlKind.Slider,        "div"); 
        add(controlMap, ControlKind.Splitter,      "*");
        add(controlMap, ControlKind.Spinner,       "*");
        add(controlMap, ControlKind.Table,         "table");
        add(controlMap, ControlKind.TabPanel,      "button");
        add(controlMap, ControlKind.TextBox,       "input", "textarea");
        add(controlMap, ControlKind.ToggleButton,  "input");
        add(controlMap, ControlKind.ListView,      "ul");
        add(controlMap, ControlKind.Tree,          "");
        add(controlMap, ControlKind.Wait,          "*");
        add(controlMap, ControlKind.Tooltip,       "*");
        add(controlMap, ControlKind.Menu,          "li");
        add(controlMap, ControlKind.TreeItem,      "");
        
        Map<LocatorFieldKind, String>   fieldMap = new HashMap<>();
        
        fieldMap.put(LocatorFieldKind.ACTION,       "action");
        fieldMap.put(LocatorFieldKind.UID,          "id"); 
        fieldMap.put(LocatorFieldKind.CLAZZ,        "class");
        fieldMap.put(LocatorFieldKind.NAME,         "name");
        fieldMap.put(LocatorFieldKind.TITLE,        "title");
        fieldMap.put(LocatorFieldKind.TEXT,         "placeholder");
        fieldMap.put(LocatorFieldKind.TOOLTIP,      "title");
        
        return new PluginInfo(controlMap, fieldMap);
    }

	//----------------------------------------------------------------------------------------------
	
    private static void add(Map<ControlKind, String[]> controlMap, ControlKind kind, String ... nodes)
    {
        controlMap.put(kind, nodes);
    }
}
