////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.ParametersKind;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class WinAppFactory implements IApplicationFactory
{
	private static final int		requiredMajorVersion	= 2;
	private static final int		requiredMinorVersion	= 29;

	public static final String		logLevel				= "LogLevel";
	public static final String		jreExecName				= "jreExec";
	public static final String		jreArgsName				= "jreArgs";
	public static final String		maxTimeout				= "MaxTimeout";

	public static final String		mainWindowName			= "MainWindow";
	public static final String		mainWindowHeight		= "Height";
	public static final String		mainWindowWidth			= "Width";
	public static final String		pidName					= "PID";
	public static final String		controlKindName			= "ControlKind";
	public static final String		connectionTimeout		= "Timeout";

	public static final String		execName				= "Exec";
	public static final String		workDirName				= "WorkDir";
	public static final String		argsName				= "Args";

	public static final String      propertyWindowRectangle = "Rectangle";
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
			case LOAD:		return new String[] { jreExecName, jreArgsName, maxTimeout, logLevel};
			case START:		return new String[] { execName, workDirName, argsName };
			case CONNECT:   return new String[] { mainWindowName, mainWindowHeight, mainWindowWidth, pidName, controlKindName, connectionTimeout };
			case PROPERTY:  return new String[] { propertyWindowRectangle }; 
			default:		return empty;	
		}
	}
	
	@Override
	public boolean canFillParameter(String parameterToFill)
	{
		return false;
	}

	@Override
	public String[] listForParameter(String parameterToFill)
	{
		return empty;
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
			try (Scanner in = new Scanner(WinAppFactory.class.getResourceAsStream(helpFileName)))
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
	public IApplication createApplication() throws Exception
	{
		return new ProxyWinGuiApp();
	}

	@Override
	public String getRemoteClassName()
	{
		return WinRemoteApplicationJNA.class.getCanonicalName();
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
		Map<ControlKind, String[]> controlMap = new LinkedHashMap<>();

		add(controlMap, ControlKind.Any,           	ControlType.Any);
		add(controlMap, ControlKind.Button,        	ControlType.Button, ControlType.SplitButton, ControlType.Hyperlink);
		add(controlMap, ControlKind.CheckBox,	   	ControlType.CheckBox);
		add(controlMap, ControlKind.ComboBox,	   	ControlType.ComboBox);
		add(controlMap, ControlKind.Dialog,        	ControlType.Window);
		add(controlMap, ControlKind.Frame,         	ControlType.Window);
		add(controlMap, ControlKind.Label,         	ControlType.Text);
		add(controlMap, ControlKind.MenuItem,      	ControlType.MenuItem);
		add(controlMap, ControlKind.Panel,         	ControlType.Pane);
		add(controlMap, ControlKind.RadioButton,   	ControlType.RadioButton);
		add(controlMap, ControlKind.Row,           	ControlType.Custom);
		add(controlMap, ControlKind.Table,         	ControlType.Table, ControlType.DataGrid);
		add(controlMap, ControlKind.TabPanel,      	ControlType.Tab);
		add(controlMap, ControlKind.TextBox,       	ControlType.Edit, ControlType.Document);

		add(controlMap, ControlKind.ToggleButton,  	ControlType.Button);
		add(controlMap, ControlKind.ListView,  		ControlType.List, ControlType.DataGrid);
		add(controlMap, ControlKind.Tree,  			ControlType.Tree);
		add(controlMap, ControlKind.Tooltip, 		ControlType.ToolTip);
		add(controlMap, ControlKind.Image, 			ControlType.Image);
		add(controlMap, ControlKind.Spinner, 		ControlType.Spinner);
		add(controlMap, ControlKind.ProgressBar,	ControlType.ProgressBar);
		add(controlMap, ControlKind.ScrollBar,		ControlType.ScrollBar);
		add(controlMap, ControlKind.Slider,			ControlType.Slider);
		add(controlMap, ControlKind.TreeItem,		ControlType.TreeItem);

		Map<LocatorFieldKind, String> fieldMap = new LinkedHashMap<>();
		fieldMap.put(LocatorFieldKind.UID,		AttributeKind.UID.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.CLAZZ,	AttributeKind.CLASS.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.NAME,		AttributeKind.NAME.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.TEXT,		AttributeKind.TEXT.name().toLowerCase());

		return new PluginInfo(controlMap, fieldMap);
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

	private static void add(Map<ControlKind, String[]> controlMap, ControlKind kind, ControlType ... types)
	{
		String[] a = new String[types.length];
		for (int i = 0; i < a.length; i++) {
			a[i] = types[i].getName();
		}
		controlMap.put(kind, a);
	}
	//----------------------------------------------------------------------------------------------
}
