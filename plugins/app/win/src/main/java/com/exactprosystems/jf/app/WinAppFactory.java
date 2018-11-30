/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.PluginDescription;
import com.exactprosystems.jf.api.common.PluginFieldDescription;
import com.exactprosystems.jf.api.common.i18n.R;
import org.w3c.dom.Node;

import java.util.*;

@PluginDescription(
		pluginName = "WIN",
		description = R.WIN_PLUGIN_DESCRIPTION,
		difference = R.WIN_PLUGIN_DIFFERENCE
)
public class WinAppFactory extends AbstractApplicationFactory
{
	@PluginFieldDescription(parameter = "LogLevel", description = R.WIN_PLUGIN_LOG_LEVEL, example = "ALL")
	public static final String		logLevel				= "LogLevel";
	@PluginFieldDescription(parameter = "jreExec", description = R.WIN_PLUGIN_JRE_EXEC, example = "C:\\Program Files\\Java\\jdk1.8.0_XX\\bin\\java")
	public static final String		jreExecName				= "jreExec";
	@PluginFieldDescription(parameter = "jreArgs", description = R.WIN_PLUGIN_JRE_ARGS, example = "-Xms128m -Xmx1G")
	public static final String		jreArgsName				= "jreArgs";
	@PluginFieldDescription(parameter = "MaxTimeout", description = R.WIN_PLUGIN_MAX_TIMEOUT, example = "20000")
	public static final String		maxTimeout				= "MaxTimeout";

	@PluginFieldDescription(parameter = "AlwaysToFront", description = R.WIN_PLUGIN_ALWAYS_TO_FRONT, example = "true")
	public static final String		alwaysToFront			= "AlwaysToFront";
	@PluginFieldDescription(parameter = "MainWindow", description = R.WIN_PLUGIN_MAIN_WINDOW, example = "'Example'")
	public static final String		mainWindowName			= "MainWindow";
	@PluginFieldDescription(parameter = "Height", description = R.WIN_PLUGIN_HEIGHT, example = "100")
	public static final String		mainWindowHeight		= "Height";
	@PluginFieldDescription(parameter = "Width", description = R.WIN_PLUGIN_WIDTH, example = "100")
	public static final String		mainWindowWidth			= "Width";
	@PluginFieldDescription(parameter = "PID", description = R.WIN_PLUGIN_PID, example = "101")
	public static final String		pidName					= "PID";
	@PluginFieldDescription(parameter = "ControlKind", description = R.WIN_PLUGIN_CONTROL_KIND, example = "ControlKind.Panel")
	public static final String		controlKindName			= "ControlKind";
	@PluginFieldDescription(parameter = "Timeout", description = R.WIN_PLUGIN_TIMEOUT, example = "20000")
	public static final String		connectionTimeout		= "Timeout";

	@PluginFieldDescription(parameter = "Exec", description = R.WIN_PLUGIN_EXEC, example = "'example.exe'")
	public static final String		execName				= "Exec";
	@PluginFieldDescription(parameter = "WorkDir", description = R.WIN_PLUGIN_WORK_DIR, example = "'C:/example/'")
	public static final String		workDirName				= "WorkDir";
	@PluginFieldDescription(parameter = "Args", description = R.WIN_PLUGIN_ARGS, example = "'Arg1'")
	public static final String		argsName				= "Args";

	@PluginFieldDescription(parameter = "Rectangle", description = R.WIN_PLUGIN_RECTANGLE, example = "Rectangle{x=5, y=10, width=200, height=149}")
	public static final String      propertyWindowRectangle = "Rectangle";
	@PluginFieldDescription(parameter = "Title", description = R.WIN_PLUGIN_TITLE, example = "'Title'")
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
        info.addTypes(ControlKind.Label,        ControlType.Text.name(), ControlType.DataItem.name(), ControlType.HeaderItem.name());
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
