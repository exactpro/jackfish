package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.PluginDescription;
import com.exactprosystems.jf.api.common.PluginFieldDescription;
import com.exactprosystems.jf.api.common.i18n.R;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PluginDescription(
		pluginName = "JAVAFX",
		description = R.JAVAFX_PLUGIN_DESCRIPTION,
		difference = R.JAVAFX_PLUGIN_DIFFERENCE
)
public class FxAppFactory extends AbstractApplicationFactory
{
	public static final String helpFileName = "help.txt";

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

	@PluginFieldDescription(parameter = "URL", description = R.JAVAFX_PLUGIN_URL, example = "'http://site.com/start.jnlp'")
	public static final String urlName = "URL";

	@PluginFieldDescription(parameter = "Title", description = R.JAVAFX_PLUGIN_TITLE, example = "'Title'")
	public static final String propertyTitle = "Title";

	private static String[] empty = {};

	private static PluginInfo info;

	static
	{
		Map<LocatorFieldKind, String> fieldMap = new HashMap<>();

		fieldMap.put(LocatorFieldKind.UID, null);
		fieldMap.put(LocatorFieldKind.ACTION, "action");
		fieldMap.put(LocatorFieldKind.CLAZZ, "class");
		fieldMap.put(LocatorFieldKind.NAME, "name");
		fieldMap.put(LocatorFieldKind.TITLE, "title");
		fieldMap.put(LocatorFieldKind.TEXT, null);
		fieldMap.put(LocatorFieldKind.TOOLTIP, "tooltip");

		info = new JavaFxPluginInfo(fieldMap, new ArrayList<>());

		info.addTypes(ControlKind.Any, "*");
		info.addTypes(ControlKind.Button, Button.class.getSimpleName());
		info.addTypes(ControlKind.CheckBox, CheckBox.class.getSimpleName());
		info.addTypes(ControlKind.ComboBox, ComboBox.class.getSimpleName());
		info.addTypes(ControlKind.Dialog, Dialog.class.getSimpleName());
		info.addTypes(ControlKind.Frame, Scene.class.getSimpleName());
		info.addTypes(ControlKind.Label, Label.class.getSimpleName());
		info.addTypes(ControlKind.ListView, ListView.class.getSimpleName());
		info.addTypes(ControlKind.Menu, Menu.class.getSimpleName());
		info.addTypes(ControlKind.MenuItem, MenuItem.class.getSimpleName());
		info.addTypes(ControlKind.Panel, Pane.class.getSimpleName());
		info.addTypes(ControlKind.ProgressBar, ProgressBar.class.getSimpleName());
		info.addTypes(ControlKind.RadioButton, RadioButton.class.getSimpleName());
		info.addTypes(ControlKind.ScrollBar, ScrollBar.class.getSimpleName());
		info.addTypes(ControlKind.Slider, Slider.class.getSimpleName());
		info.addTypes(ControlKind.Splitter, SplitPane.class.getSimpleName());
		info.addTypes(ControlKind.Spinner, Spinner.class.getSimpleName());
		info.addTypes(ControlKind.Table, TableView.class.getSimpleName());
		info.addTypes(ControlKind.TabPanel, TabPane.class.getSimpleName());
		info.addTypes(ControlKind.TextBox, TextField.class.getSimpleName(), TextArea.class.getSimpleName());
		info.addTypes(ControlKind.ToggleButton, ToggleButton.class.getSimpleName());
		info.addTypes(ControlKind.Tooltip, Tooltip.class.getSimpleName());
		info.addTypes(ControlKind.Tree, TreeView.class.getSimpleName());
		info.addTypes(ControlKind.Wait, "*");
	}

	@Override
	public IApplication createApplication() throws Exception
	{
		return null;
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
		return new String[0];
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

	private static class JavaFxPluginInfo extends PluginInfo
	{
		JavaFxPluginInfo(Map<LocatorFieldKind, String> fieldMap, List<String> notStableList)
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
