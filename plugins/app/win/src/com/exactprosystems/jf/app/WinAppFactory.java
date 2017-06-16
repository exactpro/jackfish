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
	public boolean isAllowed(ControlKind kind, OperationKind operation) {
		return getInfo().isAllowed(kind, operation);
	}

	@Override
	public boolean isSupported(ControlKind kind) {
		return getInfo().isSupported(kind);
	}

	@Override
    public PluginInfo getInfo()
    {
		Map<LocatorFieldKind, String> fieldMap = new LinkedHashMap<>();
		fieldMap.put(LocatorFieldKind.UID,		AttributeKind.UID.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.CLAZZ,	AttributeKind.CLASS.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.NAME,		AttributeKind.NAME.name().toLowerCase());
		fieldMap.put(LocatorFieldKind.TEXT,		AttributeKind.TEXT.name().toLowerCase());

		PluginInfo info = new PluginInfo(fieldMap);
		info.add(ControlKind.Any).setTypes(ControlType.Any.name());
		info.add(ControlKind.Button).setTypes(ControlType.Button.name(), ControlType.SplitButton.name(), ControlType.Hyperlink.name());
		info.add(ControlKind.CheckBox).setTypes(ControlType.CheckBox.name());
		info.add(ControlKind.ComboBox).setTypes(ControlType.ComboBox.name());
		info.add(ControlKind.Dialog).setTypes(ControlType.Window.name());
		info.add(ControlKind.Frame).setTypes(ControlType.Window.name());
		info.add(ControlKind.Label).setTypes(ControlType.Text.name())
				.addExcludes(OperationKind.KEY_DOWN, OperationKind.KEY_UP, OperationKind.PRESS);
		info.add(ControlKind.MenuItem).setTypes(ControlType.MenuItem.name());
		info.add(ControlKind.Panel).setTypes(ControlType.Pane.name());
		info.add(ControlKind.RadioButton).setTypes(ControlType.RadioButton.name());
		info.add(ControlKind.Row).setTypes(ControlType.Custom.name());
		info.add(ControlKind.Table).setTypes(ControlType.Table.name(), ControlType.DataGrid.name());
		info.add(ControlKind.TabPanel).setTypes(ControlType.Tab.name());
		info.add(ControlKind.TextBox).setTypes(ControlType.Edit.name(), ControlType.Document.name());
		info.add(ControlKind.Menu).setTypes(ControlType.Menu.name());
		info.add(ControlKind.Wait).setTypes(ControlType.Wait.name());
		info.add(ControlKind.ToggleButton).setTypes(ControlType.Button.name());
		info.add(ControlKind.ListView).setTypes(ControlType.List.name(), ControlType.DataGrid.name());
		info.add(ControlKind.Tree).setTypes(ControlType.Tree.name());
		info.add(ControlKind.Tooltip).setTypes(ControlType.ToolTip.name());
		info.add(ControlKind.Image).setTypes(ControlType.Image.name());
		info.add(ControlKind.Spinner).setTypes(ControlType.Spinner.name());
		info.add(ControlKind.ProgressBar).setTypes(ControlType.ProgressBar.name());
		info.add(ControlKind.ScrollBar).setTypes(ControlType.ScrollBar.name());
		info.add(ControlKind.Slider).setTypes(ControlType.Slider.name());
		info.add(ControlKind.TreeItem).setTypes(ControlType.TreeItem.name());

		return info;
    }
}
