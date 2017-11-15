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
import org.w3c.dom.Node;

import javax.swing.*;
import java.util.*;

@PluginDescription(
		pluginName = "SWING",
		description = R.SWING_PLUGIN_DESCRIPTION,
		difference = R.SWING_PLUGIN_DIFFERENCE
)
public class SwingAppFactory extends AbstractApplicationFactory
{
	@PluginFieldDescription(parameter = "LogLevel", description = R.SWING_PLUGIN_LOG_LEVEL, example = "ALL")
    public static final String logLevel         = "LogLevel";
	@PluginFieldDescription(parameter = "jreExec", description = R.SWING_PLUGIN_JRE_EXEC, example = "C:\\Program Files\\Java\\jdk1.8.0_XX\\bin\\java")
	public static final String jreExecName 		= "jreExec";
	@PluginFieldDescription(parameter = "jreArgs", description = R.SWING_PLUGIN_JRE_ARGS, example = "-Xms128m -Xmx1G")
	public static final String jreArgsName 		= "jreArgs";
	@PluginFieldDescription(parameter = "MainClass", description = R.SWING_PLUGIN_MAIN_CLASS, example = "com.example.MainClass")
	public static final String mainClassName 	= "MainClass";
	@PluginFieldDescription(parameter = "Jar", description = R.SWING_PLUGIN_JAR, example = "'C:/example.jar'")
	public static final String jarName 			= "Jar";
	@PluginFieldDescription(parameter = "Args", description = R.SWING_PLUGIN_ARGS, example = "'Arg1'")
	public static final String argsName 		= "Args";

	@PluginFieldDescription(parameter = "URL", description = R.SWING_PLUGIN_URL, example = "'http://site.com/start.jnlp'")
	public static final String urlName 			= "URL";

	@PluginFieldDescription(parameter = "Title", description = R.SWING_PLUGIN_TITLE, example = "'Title'")
    public static final String propertyTitle    = "Title";

	private static String[]    empty = {  };

	private static PluginInfo  info;
    
    static
    {
        Map<LocatorFieldKind, String> fieldMap = new HashMap<>();

        fieldMap.put(LocatorFieldKind.UID,      null);
        fieldMap.put(LocatorFieldKind.ACTION,   "action");
        fieldMap.put(LocatorFieldKind.CLAZZ,    "class");
        fieldMap.put(LocatorFieldKind.NAME,     "name");
        fieldMap.put(LocatorFieldKind.TITLE,    "title");
        fieldMap.put(LocatorFieldKind.TEXT,     null);
        fieldMap.put(LocatorFieldKind.TOOLTIP,  "tooltip");

        info = new SwingPluginInfo(fieldMap, new ArrayList<>());

		info.addTypes(ControlKind.Any, PluginInfo.ANY_TYPE);
		info.addTypes(ControlKind.Button, JButton.class.getSimpleName());
		info.addTypes(ControlKind.CheckBox, JCheckBox.class.getSimpleName());
		info.addTypes(ControlKind.ComboBox, JComboBox.class.getSimpleName());
		info.addTypes(ControlKind.Dialog, JDialog.class.getSimpleName());
		info.addTypes(ControlKind.Frame, JFrame.class.getSimpleName());
		info.addTypes(ControlKind.Label, JLabel.class.getSimpleName());
		info.addTypes(ControlKind.ListView, JList.class.getSimpleName());
		info.addTypes(ControlKind.Menu, JMenu.class.getSimpleName());
		info.addTypes(ControlKind.MenuItem, JMenuItem.class.getSimpleName());
		info.addTypes(ControlKind.Panel, JPanel.class.getSimpleName());
		info.addTypes(ControlKind.ProgressBar, JProgressBar.class.getSimpleName());
		info.addTypes(ControlKind.RadioButton, JRadioButton.class.getSimpleName());
		info.addTypes(ControlKind.ScrollBar, JScrollBar.class.getSimpleName());
		info.addTypes(ControlKind.Slider, JSlider.class.getSimpleName());
		info.addTypes(ControlKind.Splitter, JSplitPane.class.getSimpleName());
		info.addTypes(ControlKind.Spinner, JSpinner.class.getSimpleName());
		info.addTypes(ControlKind.Table, JTable.class.getSimpleName());
		info.addTypes(ControlKind.TabPanel, JTabbedPane.class.getSimpleName());
		info.addTypes(ControlKind.TextBox, JTextField.class.getSimpleName(), JTextArea.class.getSimpleName());
		info.addTypes(ControlKind.ToggleButton, JToggleButton.class.getSimpleName());
		info.addTypes(ControlKind.Tooltip, JToolTip.class.getSimpleName());
		info.addTypes(ControlKind.Tree, JTree.class.getSimpleName());
		info.addTypes(ControlKind.Wait,PluginInfo.ANY_TYPE);

        info.addExcludes(ControlKind.MenuItem, OperationKind.EXPAND, OperationKind.COLLAPSE);
        info.addExcludes(ControlKind.ComboBox, OperationKind.SCROLL_TO);
    }

    static final Set<Class<?>> ALL_PARENETS = new HashSet<>(Arrays.asList(
    		JButton.class, JCheckBox.class, JComboBox.class, JDialog.class,
			JFrame.class, JLabel.class, JList.class, JMenu.class,
			JMenuItem.class, JPanel.class, JProgressBar.class,
			JRadioButton.class, JScrollBar.class, JSlider.class,
			JSplitPane.class, JSpinner.class, JTable.class, JTabbedPane.class,
			JTextField.class, JTextArea.class, JToggleButton.class,
			JToolTip.class, JTree.class
	));

	//region IFactory
	@Override
	public String[] wellKnownParameters(ParametersKind kind)
	{
		switch (kind)
		{
			case LOAD:		    return new String[] { jreExecName, jreArgsName, logLevel, trimTextName };
			case START:		    return new String[] { jarName, argsName, mainClassName };
			case CONNECT:	    return new String[] { urlName };
            case GET_PROPERTY:  return new String[] { propertyTitle };
            case SET_PROPERTY:  return new String[] { propertyTitle };
			default:		    return empty;	
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

	@Override
	public IApplication createApplication()
	{
		return new ProxySwingApp();
	}

	@Override
	public String getRemoteClassName()
	{
		return SwingRemoteApplication.class.getCanonicalName();
	}

    @Override
    public PluginInfo getInfo()
    {
		return info;
    }

    //endregion

    private static class SwingPluginInfo extends PluginInfo
	{
		SwingPluginInfo(Map<LocatorFieldKind, String> fieldMap, List<String> notStableList)
		{
			super(fieldMap, notStableList);
		}

		@Override
		public ControlKind derivedControlKindByNode(Node node)
		{
			Object userData = node.getUserData(IRemoteApplication.baseParnetName);
			if (userData instanceof String)
			{
				try
				{
					Class<?> clazz = Class.forName(((String) userData));
					return super.controlKindByType(clazz.getSimpleName());
				}
				catch (ClassNotFoundException e)
				{}
			}
			return ControlKind.Any;
		}
	}
}
