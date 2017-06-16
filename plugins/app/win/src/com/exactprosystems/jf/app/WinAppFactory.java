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

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class WinAppFactory implements IApplicationFactory
{
    public static final String      helpFileName            = "help.txt";
    
	public static final String		logLevel				= "LogLevel";
	public static final String		jreExecName				= "jreExec";
	public static final String		jreArgsName				= "jreArgs";
	public static final String		maxTimeout				= "MaxTimeout";

	public static final String		alwaysToFront			= "AlwaysToFront";
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
    public static final String      propertyTitle           = "Title";
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
			case LOAD:		     return new String[] { jreExecName, jreArgsName, maxTimeout, logLevel};
			case START:		     return new String[] { execName, workDirName, argsName, alwaysToFront };
			case CONNECT:        return new String[] { mainWindowName, mainWindowHeight, mainWindowWidth, pidName, controlKindName, connectionTimeout, alwaysToFront };
			case GET_PROPERTY:   return new String[] { propertyWindowRectangle, propertyTitle }; 
            case SET_PROPERTY:   return new String[] { propertyTitle }; 
			default:		     return empty;	
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
    public void init(IGuiDictionary dictionary)
    {
        this.dictionary = dictionary;
    }

	@Override
	public InputStream getHelp()
	{
		return WinAppFactory.class.getResourceAsStream(helpFileName);
	}

    @Override
    public Set<ControlKind> supportedControlKinds()
    {
        return Arrays.stream(supportedControls)
                .sorted((c1,c2) -> c1.name().compareTo(c2.name()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
	public IGuiDictionary getDictionary()
	{
		return this.dictionary;
	}

    @Override
    public PluginInfo getInfo()
    {
		Map<ControlKind, ControlInfo> controlMap = new LinkedHashMap<>();
		controlMap.put(ControlKind.Any, 		new ControlInfo().addTypes(ControlType.Any.name())
														  .addExcludes(OperationKind.KEY_DOWN, OperationKind.KEY_UP, OperationKind.PRESS));
		controlMap.put(ControlKind.Button,		new ControlInfo().addTypes(ControlType.Button.name(), ControlType.SplitButton.name(), ControlType.Hyperlink.name()));
		controlMap.put(ControlKind.CheckBox, 	new ControlInfo().addTypes(ControlType.CheckBox.name()));
		controlMap.put(ControlKind.ComboBox,	new ControlInfo().addTypes(ControlType.ComboBox.name()));
		controlMap.put(ControlKind.Dialog,      new ControlInfo().addTypes(ControlType.Window.name()));
		controlMap.put(ControlKind.Frame,       new ControlInfo().addTypes(ControlType.Window.name()));
		controlMap.put(ControlKind.Label,       new ControlInfo().addTypes(ControlType.Text.name()));
		controlMap.put(ControlKind.MenuItem,    new ControlInfo().addTypes(ControlType.MenuItem.name()));
		controlMap.put(ControlKind.Panel,       new ControlInfo().addTypes(ControlType.Pane.name()));
		controlMap.put(ControlKind.RadioButton, new ControlInfo().addTypes(ControlType.RadioButton.name()));
		controlMap.put(ControlKind.Row,         new ControlInfo().addTypes(ControlType.Custom.name()));
		controlMap.put(ControlKind.Table,       new ControlInfo().addTypes(ControlType.Table.name(), ControlType.DataGrid.name()));
		controlMap.put(ControlKind.TabPanel,    new ControlInfo().addTypes(ControlType.Tab.name()));
		controlMap.put(ControlKind.TextBox,     new ControlInfo().addTypes(ControlType.Edit.name(), ControlType.Document.name()));
		controlMap.put(ControlKind.Menu,		new ControlInfo().addTypes(ControlType.Menu.name()));
		controlMap.put(ControlKind.Wait,		new ControlInfo().addTypes(ControlType.Wait.name()));
		controlMap.put(ControlKind.ToggleButton,new ControlInfo().addTypes(ControlType.Button.name()));
		controlMap.put(ControlKind.ListView,  	new ControlInfo().addTypes(ControlType.List.name(), ControlType.DataGrid.name()));
		controlMap.put(ControlKind.Tree,  		new ControlInfo().addTypes(ControlType.Tree.name()));
		controlMap.put(ControlKind.Tooltip, 	new ControlInfo().addTypes(ControlType.ToolTip.name()));
		controlMap.put(ControlKind.Image, 		new ControlInfo().addTypes(ControlType.Image.name()));
		controlMap.put(ControlKind.Spinner, 	new ControlInfo().addTypes(ControlType.Spinner.name()));
		controlMap.put(ControlKind.ProgressBar,	new ControlInfo().addTypes(ControlType.ProgressBar.name()));
		controlMap.put(ControlKind.ScrollBar,	new ControlInfo().addTypes(ControlType.ScrollBar.name()));
		controlMap.put(ControlKind.Slider,		new ControlInfo().addTypes(ControlType.Slider.name()));
		controlMap.put(ControlKind.TreeItem,	new ControlInfo().addTypes(ControlType.TreeItem.name()));

		Map<LocatorFieldKind, String> fieldMap = new LinkedHashMap<>();
		fieldMap.put(LocatorFieldKind.UID,		AttributeKind.UID.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.CLAZZ,	AttributeKind.CLASS.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.NAME,		AttributeKind.NAME.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.TEXT,		AttributeKind.TEXT.name().toLowerCase());

		return new PluginInfo(controlMap, fieldMap);
    }
}
