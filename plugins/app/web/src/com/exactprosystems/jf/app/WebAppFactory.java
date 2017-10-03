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
import com.exactprosystems.jf.api.common.PluginFieldDescription;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.*;

@PluginDescription(
		description = "A Web plugin used for testings web browser application. The plugin based on {{*Selenium*}} framework. \n" +
				"Can starts on {{/Windows/}}, {{/Linux/}} and {{/MacOS/}}. (InternetExplorer only on {{/Windows/}}).\n" +
				"The plugin supported cross browse testing. \n" +
				"Supported browsers : " +  //todo
				"{{*How use*}}: \n" +
				"1. Open project tree in JF (click on 'Project' in left top corner) \n" +
				"2. Go to 'App entries' and choose WEB or WEB_WIN (for Windows) plugin\n" +
				"3. Check params for plugin: \n" +
				"appDicPath - path to dictionary for current plugin, \n" +
				"driver path for used browser (see additional info onwards). \n" +
				"Example and use other params You can find in table onwards \n" +
				"4. In actions part of dictionary choose plugin via combo box and click on start button. \n" +
				"You will see new window with 2 fields: URL and Browser. \n" +
				"Set URL (remember about http/https) and choose Browser via combo box in right part of field, click on 'Start WEB' button. \n" +
				"JF will run selected browser and try open selected URL. \n", //todo
		additionalDescription = "How set path to browser: \n" +
				"Open JackFish folder -> apps. You will see few folders each one for different OS. In each folder You can find driver files for browsers. \n" +
				"Open folder which corresponds to your OS and choose file which corresponds to your browser and OS architecture. \n" +
				"Copy file name (with extension if You are using Windows). In JF tool open Project -> App entries, choose WEB (or WEB_WIN for Windows) plugin. \n" +
				"Below You will see table with parameters. Choose parameter which one correspond to selected browser and set 'Value' like 'folderName/driverFileName' where folderName - folder in JF/apps for current OS \n" +
				"and driverFileName - name of file driver (remember about file extension for Windows). For example 'win/chromedriver-linux-2.32-x64.exe'. \n" +
				"Remember! After each changes in config click 'Save configuration' and 'Reload configuration'. Without that your changes not applicable. \n" +
				" \n" +
				" \n" +
				" \n"
)
public class WebAppFactory extends AbstractApplicationFactory
{
    public static final String  helpFileName          = "help.txt";

    @PluginFieldDescription(parameter = "LogLevel", description = "Set logging level for control logging output. You can use different levels (ALL, ERROR, INFO, WARNING, etc), but we are recommend use 'ALL'.", example = "ALL")
    public static final String   logLevel             = "LogLevel";
	@PluginFieldDescription(parameter = "jreExec", description = "Set path to jar", example = "")
    public static final String   jreExecName          = "jreExec";
	@PluginFieldDescription(parameter = "jreArgs", description = "Run JVM with determined params. For example, initial size and max size of the memory application pool", example = "-Xms128m -Xmx1G")
    public static final String   jreArgsName          = "jreArgs";

	@PluginFieldDescription(parameter = "ChromeDriverPath", description = "Path to Chrome driver. Depends on used OS", example = "unix/chromedriver-linux-2.32-x64.")
    public static final String   chromeDriverPathName     = "ChromeDriverPath";
	@PluginFieldDescription(parameter = "GeckoDriverPath", description = "Path to Firefox driver. Depends on used OS", example = "unix/geckodriver-linux-0.11.1-x64.")
    public static final String   geckoDriverPathName      = "GeckoDriverPath";
	@PluginFieldDescription(parameter = "IEDriverPath", description = "Path to Internet Explorer driver. Depends on used OS. IE can used only in Windows.", example = "win/iedriverserver-2.53.1-x64.exe")
    public static final String   ieDriverPathName         = "IEDriverPath";
	@PluginFieldDescription(parameter = "ChromeDriverBinary", description = "Path to Chrome binary driver", example = "unix/chromedriver")
    public static final String   chromeDriverBinary       = "ChromeDriverBinary";
	@PluginFieldDescription(parameter = "FirefoxProfileDirectory", description = "Path to Firefox profile folder which can contains personal information such as bookmarks, passwords, etc.", example = "~/.mozilla/firefox/xxxxxxxx.default/")
    public static final String   firefoxProfileDir        = "FirefoxProfileDirectory";
	@PluginFieldDescription(parameter = "UsePrivateMode", description = "Allows you to browse the Internet without saving any information about which sites and pages you’ve visited.", example = "true")
    public static final String   usePrivateMode           = "UsePrivateMode";

	@PluginFieldDescription(parameter = "Browser", description = "Choose one of determined browsers via combo box", example = "'Chrome'")
    public static final String   browserName              = "Browser";
	@PluginFieldDescription(parameter = "URL", description = "Set URL which should opened via Browser", example = "https://example.com")
    public static final String   urlName                  = "URL";
	@PluginFieldDescription(parameter = "WhereOpen", description = "Available only when application is running." +
			" In actions part of dictionary switch to 'New' tab, set URL and choose where open this URl - new tab of browser, new browser window or change URL in first opened tab - via combo box.", example = "'OpenInTab'")
    public static final String   whereOpenName            = "WhereOpen";

    public static final String   tabName                  = "Tab";

    public static final String   propertyUrlName          = "URL";
    public static final String   propertyTitle            = "Title";
    public static final String   propertyAllTitles        = "AllTitles";
    public static final String   propertyCookie           = "Cookie";
    public static final String   propertyAllCookies       = "AllCookies";
    public static final String   propertyAddCookie        = "AddCookie";
    public static final String   propertyRemoveCookie     = "RemoveCookie";
    public static final String   propertyRemoveAllCookies = "RemoveAllCookies";
	
	private static String[]      empty = {  };

    private static PluginInfo    info;

    public static final String[] supportedBrowsers = new String[]
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

    static
    {
        Map<LocatorFieldKind, String>   fieldMap = new HashMap<>();
        
        fieldMap.put(LocatorFieldKind.ACTION,       "action");
        fieldMap.put(LocatorFieldKind.UID,          "id"); 
        fieldMap.put(LocatorFieldKind.CLAZZ,        "class");
        fieldMap.put(LocatorFieldKind.NAME,         "name");
        fieldMap.put(LocatorFieldKind.TITLE,        "title");
        fieldMap.put(LocatorFieldKind.TEXT,         "placeholder");
        fieldMap.put(LocatorFieldKind.TOOLTIP,      "title");

		List<String> notStableList = Arrays.asList("value", "maxlength", "style", "size");
		info = new WebPluginInfo(fieldMap, notStableList);

        info.addTypes(ControlKind.Any, "*");
        info.addTypes(ControlKind.Button, "button", "input", "a", "img");
        info.addTypes(ControlKind.CheckBox, "button", "input");
        info.addTypes(ControlKind.ComboBox,"select", "input");
        info.addTypes(ControlKind.Dialog,"form");
        info.addTypes(ControlKind.Frame,"form", "body", "frame", "iframe");
        info.addTypes(ControlKind.Image,"img");
        info.addTypes(ControlKind.Label,"label", "span");
        info.addTypes(ControlKind.Panel,"div");
        info.addTypes(ControlKind.ProgressBar,"progress");
        info.addTypes(ControlKind.RadioButton,"input");
        info.addTypes(ControlKind.Row,"tr");
        info.addTypes(ControlKind.Slider,"div");
        info.addTypes(ControlKind.Table,"table");
        info.addTypes(ControlKind.TextBox,"input", "textarea");
        info.addTypes(ControlKind.ToggleButton,"input");
        info.addTypes(ControlKind.ListView,"ul");
        info.addTypes(ControlKind.Wait,"*");

    }

	public enum WhereToOpen 
	{
	    OpenInTab,
	    OpenInWindow,
	    OpenNewUrl;
	    
	    public static String[] names()
	    {
	        return new String[] { OpenInTab.name(), OpenInWindow.name(), OpenNewUrl.name() };
	    }
	}

	//region IFactory
	@Override
	public String[] wellKnownParameters(ParametersKind kind)
	{
		switch (kind)
		{
			case LOAD:		    return new String[] { jreExecName, jreArgsName, chromeDriverPathName, geckoDriverPathName, ieDriverPathName, 
			        chromeDriverBinary, firefoxProfileDir, usePrivateMode, logLevel, trimTextName };
			case START:         return new String[] { browserName, urlName };
			case GET_PROPERTY:  return new String[] { propertyUrlName, propertyTitle, propertyAllTitles, propertyCookie, propertyAllCookies };
            case SET_PROPERTY:  return new String[] { propertyUrlName, propertyTitle, propertyAddCookie, propertyRemoveCookie, propertyRemoveAllCookies };
			case NEW_INSTANCE:  return new String[] { urlName, whereOpenName };
			default:		    return empty;	
		}
	}

	@Override
	public boolean canFillParameter(String parameterToFill)
	{
		return browserName.equals(parameterToFill)
			|| whereOpenName.equals(parameterToFill);
	}

	@Override
	public String[] listForParameter(String parameterToFill)
	{
		switch (parameterToFill)
		{
			case browserName:
				return supportedBrowsers;

			case whereOpenName:
				return WhereToOpen.names();

			default:
				return new String[0];
		}
	}

	//endregion

	//region IApplicationFactory

	@Deprecated
    @Override
    public InputStream getHelp()
    {
        return WebAppFactory.class.getResourceAsStream(helpFileName);
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
    public PluginInfo getInfo()
    {
		return info;
    }

    //endregion

	private static class WebPluginInfo extends PluginInfo
	{
		public WebPluginInfo(Map<LocatorFieldKind, String> fieldMap, List<String> notStableList)
		{
			super(fieldMap, notStableList);
		}

		@Override
		public ControlKind derivedControlKindByNode(Node node)
		{
			String nodeName = node.getNodeName();
			switch (nodeName)
			{
				case "img" : return ControlKind.Button;
				case "button" : return ControlKind.Button;
				case "input" :
					Node type = node.getAttributes().getNamedItem("type");
					if (type != null)
					{
						String nodeValue = type.getNodeValue();
						switch (nodeValue)
						{
							case "button" :
							case "reset":
							case "submit":
								return ControlKind.Button;
							case "checkbox" : return ControlKind.CheckBox;
							case "image" : return ControlKind.Button;
							case "radio" : return ControlKind.RadioButton;
							default:
								return ControlKind.TextBox;
						}
					}
					return ControlKind.TextBox;

				case "form" : return ControlKind.Dialog;
				case "div" : return ControlKind.Panel;
			}
			return ControlKind.Any;
		}
	}
}
