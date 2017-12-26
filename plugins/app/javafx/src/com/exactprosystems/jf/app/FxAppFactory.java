////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.PluginDescription;
import com.exactprosystems.jf.api.common.PluginFieldDescription;
import com.exactprosystems.jf.api.common.i18n.R;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.w3c.dom.Node;

import java.util.*;

@PluginDescription(
		pluginName = "JAVAFX",
		description = R.JAVAFX_PLUGIN_DESCRIPTION,
		difference = R.JAVAFX_PLUGIN_DIFFERENCE
)
public class FxAppFactory extends AbstractApplicationFactory
{
	@PluginFieldDescription(parameter = "LogLevel", description = R.JAVAFX_PLUGIN_LOG_LEVEL, example = "ALL")
	public static final String logLevel      = "LogLevel";
	@PluginFieldDescription(parameter = "jreExec", description = R.JAVAFX_PLUGIN_JRE_EXEC, example = "C:\\Program Files\\Java\\jdk1.8.0_XX\\bin\\java")
	public static final String jreExecName   = "jreExec";
	@PluginFieldDescription(parameter = "jreArgs", description = R.JAVAFX_PLUGIN_JRE_ARGS, example = "-Xms128m -Xmx1G")
	public static final String jreArgsName   = "jreArgs";

	@PluginFieldDescription(parameter = "MainClass", description = R.JAVAFX_PLUGIN_MAIN_CLASS, example = "com.example.MainClass")
	public static final String mainClassName = "MainClass";
	@PluginFieldDescription(parameter = "Jar", description = R.JAVAFX_PLUGIN_JAR, example = "'C:/example.jar'")
	public static final String jarName       = "Jar";
	@PluginFieldDescription(parameter = "Args", description = R.JAVAFX_PLUGIN_ARGS, example = "'Arg1'")
	public static final String argsName      = "Args";
/*
	@PluginFieldDescription(parameter = "URL", description = R.JAVAFX_PLUGIN_URL, example = "'http://site.com/start.jnlp'")
	public static final String urlName = "URL";
*/
	@PluginFieldDescription(parameter = "Title", description = R.JAVAFX_PLUGIN_TITLE, example = "'Title'")
	public static final String propertyTitle = "Title";

	private static String[] empty = {};

	private static PluginInfo info;

	static
	{
		Map<LocatorFieldKind, String> fieldMap = new EnumMap<>(LocatorFieldKind.class);

		fieldMap.put(LocatorFieldKind.UID, "id");
		fieldMap.put(LocatorFieldKind.ACTION, null);
		fieldMap.put(LocatorFieldKind.CLAZZ, "class");
		fieldMap.put(LocatorFieldKind.NAME, "name");
		fieldMap.put(LocatorFieldKind.TITLE, "title");
		fieldMap.put(LocatorFieldKind.TEXT, "text");
		fieldMap.put(LocatorFieldKind.TOOLTIP, "tooltip");

		info = new FxPluginInfo(fieldMap, new ArrayList<>());

		info.addTypes(ControlKind.Any, PluginInfo.ANY_TYPE);
		info.addTypes(ControlKind.Button, ButtonBase.class.getName());
		info.addTypes(ControlKind.CheckBox, CheckBox.class.getName());
		info.addTypes(ControlKind.ComboBox, ComboBox.class.getName(), ChoiceBox.class.getName());
		info.addTypes(ControlKind.Dialog, Stage.class.getName());
		info.addTypes(ControlKind.Frame, Window.class.getName());
		info.addTypes(ControlKind.Label, Label.class.getName());
		info.addTypes(ControlKind.ListView, ListView.class.getName());
		info.addTypes(ControlKind.Menu, Menu.class.getName());
		info.addTypes(ControlKind.MenuItem, MenuItem.class.getName());
		info.addTypes(ControlKind.Panel, Pane.class.getName());
		info.addTypes(ControlKind.ProgressBar, ProgressBar.class.getName());
		info.addTypes(ControlKind.RadioButton, RadioButton.class.getName());
		info.addTypes(ControlKind.ScrollBar, ScrollBar.class.getName());
		info.addTypes(ControlKind.Slider, Slider.class.getName());
		info.addTypes(ControlKind.Splitter, SplitPane.class.getName());
		info.addTypes(ControlKind.Spinner, Spinner.class.getName());
		info.addTypes(ControlKind.Table, TableView.class.getName());
		info.addTypes(ControlKind.Row, TreeTableRow.class.getName(), TableRow.class.getName());
		info.addTypes(ControlKind.TabPanel, TabPane.class.getName());
		info.addTypes(ControlKind.TextBox, TextInputControl.class.getName());
		info.addTypes(ControlKind.ToggleButton, ToggleButton.class.getName());
		info.addTypes(ControlKind.Tooltip, Tooltip.class.getName());
		info.addTypes(ControlKind.Tree, TreeView.class.getName());
		info.addTypes(ControlKind.Wait, PluginInfo.ANY_TYPE);
	}

	@Override
	public IApplication createApplication() throws Exception
	{
		return new ProxyFxApp();
	}

	@Override
	public String getRemoteClassName()
	{
		return FxRemoteApplication.class.getCanonicalName();
	}

	@Override
	public PluginInfo getInfo()
	{
		return info;
	}

	@Override
	public String[] wellKnownParameters(ParametersKind kind)
	{
		switch (kind)
		{
			case LOAD: return new String[]{jreExecName, jreArgsName, logLevel, trimTextName};
			case START: return new String[] {jarName, argsName, mainClassName};
			case GET_PROPERTY: return new String[] {propertyTitle};
			default: return empty;
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

	private static class FxPluginInfo extends PluginInfo
	{
		FxPluginInfo(Map<LocatorFieldKind, String> fieldMap, List<String> notStableList)
		{
			super(fieldMap, notStableList);
		}

		@Override
		public ControlKind derivedControlKindByNode(Node node)
		{
			Object userData = node.getUserData(IRemoteApplication.baseParnetName);
			if (userData instanceof String)
			{
				return super.controlKindByType((String) userData);
			}
			return ControlKind.Any;
		}
	}
}
