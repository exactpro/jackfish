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
import com.exactprosystems.jf.api.common.PluginDescription;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.*;

@PluginDescription(
		description = "1",
		additionalDescription = "2",
		any = "3"
)
public class WinAppFactory extends AbstractApplicationFactory
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
	private static      String[]    empty = {  };

	private static PluginInfo       info;
	
	static
	{
        Map<LocatorFieldKind, String> fieldMap = new LinkedHashMap<>();
        fieldMap.put(LocatorFieldKind.UID,      AttributeKind.UID.name().toLowerCase());
        fieldMap.put(LocatorFieldKind.CLAZZ,    AttributeKind.CLASS.name().toLowerCase());
        fieldMap.put(LocatorFieldKind.NAME,     AttributeKind.NAME.name().toLowerCase());
        fieldMap.put(LocatorFieldKind.TEXT,     AttributeKind.TEXT.name().toLowerCase());
        
        info = new WinPluginInfo(fieldMap, new ArrayList<String>());
        info.addTypes(ControlKind.Any,          ControlType.Any.name());
        info.addTypes(ControlKind.Button,       ControlType.Button.name(), ControlType.SplitButton.name(), ControlType.Hyperlink.name());
        info.addTypes(ControlKind.CheckBox,     ControlType.CheckBox.name());
        info.addTypes(ControlKind.ComboBox,     ControlType.ComboBox.name());
        info.addTypes(ControlKind.Dialog,       ControlType.Window.name());
        info.addTypes(ControlKind.Label,        ControlType.Text.name());
        info.addTypes(ControlKind.MenuItem,     ControlType.MenuItem.name());
        info.addTypes(ControlKind.Panel,        ControlType.Pane.name());
        info.addTypes(ControlKind.RadioButton,  ControlType.RadioButton.name());
        info.addTypes(ControlKind.Row,          ControlType.Custom.name());
        info.addTypes(ControlKind.Table,        ControlType.Table.name(), ControlType.DataGrid.name());
        info.addTypes(ControlKind.TabPanel,     ControlType.Tab.name());
        info.addTypes(ControlKind.TextBox,      ControlType.Edit.name(), ControlType.Document.name());
        info.addTypes(ControlKind.Menu,         ControlType.Menu.name(), ControlType.MenuBar.name());
        info.addTypes(ControlKind.Wait,         ControlType.Wait.name());
        info.addTypes(ControlKind.ToggleButton, ControlType.Button.name());
        info.addTypes(ControlKind.ListView,     ControlType.List.name(), ControlType.DataGrid.name());
        info.addTypes(ControlKind.ProgressBar,  ControlType.ProgressBar.name(), ControlType.Pane.name());
        info.addTypes(ControlKind.ScrollBar,    ControlType.ScrollBar.name());
        info.addTypes(ControlKind.Slider,       ControlType.Slider.name());
        info.addTypes(ControlKind.Tree,     	ControlType.Tree.name());
		info.addTypes(ControlKind.TreeItem,     ControlType.TreeItem.name());
        info.addTypes(ControlKind.Spinner,     	ControlType.Spinner.name());

        info.addExcludes(ControlKind.Menu,      OperationKind.KEY_DOWN, OperationKind.KEY_UP, OperationKind.PRESS, OperationKind.EXPAND, OperationKind.COLLAPSE);
        info.addExcludes(ControlKind.MenuItem,  OperationKind.KEY_DOWN, OperationKind.KEY_UP, OperationKind.PRESS);
	}
	
	//region IFactory
	@Override
	public String[] wellKnownParameters(ParametersKind kind)
	{
		switch (kind)
		{
			case LOAD:		     return new String[] { jreExecName, jreArgsName, maxTimeout, logLevel, trimTextName};
			case START:		     return new String[] { execName, workDirName, argsName, alwaysToFront };
			case CONNECT:        return new String[] { mainWindowName, mainWindowHeight, mainWindowWidth, pidName, controlKindName, connectionTimeout, alwaysToFront };
			case GET_PROPERTY:   return new String[] { propertyWindowRectangle, propertyTitle }; 
            case SET_PROPERTY:   return empty;
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

	//endregion

	//region IApplicationFactory

	@Override
	public InputStream getHelp()
	{
		return WinAppFactory.class.getResourceAsStream(helpFileName);
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
	public PluginInfo getInfo()
	{
		return info;
	}

	//endregion

	private static class WinPluginInfo extends PluginInfo
	{
		WinPluginInfo(Map<LocatorFieldKind, String> fieldMap, List<String> notStableList)
		{
			super(fieldMap, notStableList);
		}

		@Override
		public ControlKind derivedControlKindByNode(Node node)
		{
			return ControlKind.Any;
		}
	}
}
