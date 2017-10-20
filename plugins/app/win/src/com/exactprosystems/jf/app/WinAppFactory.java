////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.FieldType;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.PluginDescription;
import com.exactprosystems.jf.api.common.PluginFieldDescription;
import com.exactprosystems.jf.api.common.i18n.R;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.*;

@PluginDescription(
		pluginName = "WIN",
		description = R.WIN_PLUGIN_DESCRIPTION,
		difference = R.WIN_PLUGIN_DIFFERENCE
)
public class WinAppFactory extends AbstractApplicationFactory
{
    public static final String      helpFileName            = "help.txt";

    @PluginFieldDescription(parameter = "LogLevel", description = R.WIN_PLUGIN_LOG_LEVEL, example = "ALL", fieldType = FieldType.PLUGIN)
	public static final String		logLevel				= "LogLevel";
	@PluginFieldDescription(parameter = "jreExec", description = R.WIN_PLUGIN_JRE_EXEC, example = "C:\\Program Files\\Java\\jdk1.8.0_XX\\bin\\java", fieldType = FieldType.PLUGIN)
	public static final String		jreExecName				= "jreExec";
	@PluginFieldDescription(parameter = "jreArgs", description = R.WIN_PLUGIN_JRE_ARGS, example = "-Xms128m -Xmx1G", fieldType = FieldType.PLUGIN)
	public static final String		jreArgsName				= "jreArgs";
	@PluginFieldDescription(parameter = "MaxTimeout", description = R.WIN_PLUGIN_MAX_TIMEOUT, example = "1000", fieldType = FieldType.PLUGIN)
	public static final String		maxTimeout				= "MaxTimeout";

	@PluginFieldDescription(parameter = "AlwaysToFront", description = R.WIN_PLUGIN_ALWAYS_TO_FRONT, example = "true", fieldType = FieldType.APP_START_CONNECT)
	public static final String		alwaysToFront			= "AlwaysToFront";
	@PluginFieldDescription(parameter = "MainWindow", description = R.WIN_PLUGIN_MAIN_WINDOW, example = "'Example'", fieldType = FieldType.APP_CONNECT)
	public static final String		mainWindowName			= "MainWindow";
	@PluginFieldDescription(parameter = "Height", description = R.WIN_PLUGIN_HEIGHT, example = "100", fieldType = FieldType.APP_CONNECT)
	public static final String		mainWindowHeight		= "Height";
	@PluginFieldDescription(parameter = "Width", description = R.WIN_PLUGIN_WIDTH, example = "100", fieldType = FieldType.APP_CONNECT)
	public static final String		mainWindowWidth			= "Width";
	@PluginFieldDescription(parameter = "PID", description = R.WIN_PLUGIN_PID, example = "101", fieldType = FieldType.APP_CONNECT)
	public static final String		pidName					= "PID";
	@PluginFieldDescription(parameter = "ControlKind", description = R.WIN_PLUGIN_CONTROL_KIND, example = "ControlKind.Panel", fieldType = FieldType.APP_CONNECT)
	public static final String		controlKindName			= "ControlKind";
	@PluginFieldDescription(parameter = "Timeout", description = R.WIN_PLUGIN_TIMEOUT, example = "1000", fieldType = FieldType.APP_CONNECT)
	public static final String		connectionTimeout		= "Timeout";

	@PluginFieldDescription(parameter = "Exec", description = R.WIN_PLUGIN_EXEC, example = "'example.exe'", fieldType = FieldType.APP_START)
	public static final String		execName				= "Exec";
	@PluginFieldDescription(parameter = "WorkDir", description = R.WIN_PLUGIN_WORK_DIR, example = "'C:/example/'", fieldType = FieldType.APP_START)
	public static final String		workDirName				= "WorkDir";
	@PluginFieldDescription(parameter = "Args", description = R.WIN_PLUGIN_ARGS, example = "'Arg1'", fieldType = FieldType.APP_START)
	public static final String		argsName				= "Args";

	@PluginFieldDescription(parameter = "Rectangle", description = R.WIN_PLUGIN_RECTANGLE, example = "", fieldType = FieldType.APP_WORK)
	public static final String      propertyWindowRectangle = "Rectangle";
	@PluginFieldDescription(parameter = "Title", description = R.WIN_PLUGIN_TITLE, example = "", fieldType = FieldType.APP_WORK)
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
