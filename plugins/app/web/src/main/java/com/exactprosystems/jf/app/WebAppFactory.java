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

import com.exactprosystems.jf.api.app.AbstractApplicationFactory;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.LocatorFieldKind;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.PluginDescription;
import com.exactprosystems.jf.api.common.PluginFieldDescription;
import com.exactprosystems.jf.api.common.i18n.R;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PluginDescription(
		pluginName = "WEB",
		description = R.WEB_PLUGIN_DESCRIPTION,
		difference = R.WEB_PLUGIN_DIFFERENCE
)
public class WebAppFactory extends AbstractApplicationFactory
{
	@PluginFieldDescription(parameter = "LogLevel", description = R.WEB_PLUGIN_LOG_LEVEL, example = "ALL")
    public static final String   logLevel             = "LogLevel";
	@PluginFieldDescription(parameter = "jreExec", description = R.WEB_PLUGIN_JRE_EXEC, example = "C:\\Program Files\\Java\\jdk1.8.0_XX\\bin\\java")
    public static final String   jreExecName          = "jreExec";
	@PluginFieldDescription(parameter = "jreArgs", description = R.WEB_PLUGIN_JRE_ARGS, example = "-Xms128m -Xmx1G")
    public static final String   jreArgsName          = "jreArgs";

	@PluginFieldDescription(parameter = "ChromeDriverPath", description = R.WEB_PLUGIN_CHROME_DRIVER, example = "unix/chromedriver-linux-2.32-x64.")
    public static final String   chromeDriverPathName     = "ChromeDriverPath";
	@PluginFieldDescription(parameter = "GeckoDriverPath", description = R.WEB_PLUGIN_GECKO_DRIVER, example = "unix/geckodriver-linux-0.11.1-x64.")
    public static final String   geckoDriverPathName      = "GeckoDriverPath";
	@PluginFieldDescription(parameter = "IEDriverPath", description = R.WEB_PLUGIN_IE_DRIVER, example = "win/iedriverserver-2.53.1-x64.exe")
    public static final String   ieDriverPathName         = "IEDriverPath";
	@PluginFieldDescription(parameter = "ChromeDriverBinary", description = R.WEB_PLUGIN_CHROME_DRIVER_BINARY, example = "unix/chromedriver")
    public static final String   chromeDriverBinary       = "ChromeDriverBinary";
	@PluginFieldDescription(parameter = "FirefoxProfileDirectory", description = R.WEB_PLUGIN_FIREFOX_PROFILE, example = "~/.mozilla/firefox/xxxxxxxx.default/")
    public static final String   firefoxProfileDir        = "FirefoxProfileDirectory";
	@PluginFieldDescription(parameter = "UsePrivateMode", description = R.WEB_PLUGIN_PRIVATE_MODE, example = "true")
    public static final String   usePrivateMode           = "UsePrivateMode";
	@PluginFieldDescription(parameter = "IsDriverLogging", description = R.WEB_PLUGIN_IS_DRIVER_LOGGING, example = "true")
	public static final String   isDriverLogging           = "IsDriverLogging";

	@PluginFieldDescription(parameter = "Browser", description = R.WEB_PLUGIN_BROWSER, example = "'Chrome'")
    public static final String   browserName              = "Browser";
	@PluginFieldDescription(parameter = "URL", description = R.WEB_PLUGIN_URL, example = "'https://example.com'", parametersKind = ParametersKind.START)
    public static final String   urlName                  = "URL";
	@PluginFieldDescription(parameter = "WhereOpen", description = R.WEB_PLUGIN_WHERE_OPEN, example = "'OpenInTab' 'OpenInWindow' 'OpenNewUrl'")
    public static final String   whereOpenName            = "WhereOpen";

    public static final String   tabName                  = "Tab";

	//todo change parameter
	@PluginFieldDescription(parameter = "URL", description = R.WEB_PLUGIN_PROPERTY_URL, example = "'https://example.com'", parametersKind = {ParametersKind.GET_PROPERTY, ParametersKind.SET_PROPERTY})
    public static final String   propertyUrlName          = "URL";
	@PluginFieldDescription(parameter = "Title", description = R.WEB_PLUGIN_PROPERTY_TITLE, example = "'Title'")
    public static final String   propertyTitle            = "Title";
	@PluginFieldDescription(parameter = "AllTitles", description = R.WEB_PLUGIN_PROPERTY_ALL_TITLES, example = "['Title1', 'Title2']")
    public static final String   propertyAllTitles        = "AllTitles";
	@PluginFieldDescription(parameter = "Cookie", description = R.WEB_PLUGIN_PROPERTY_COOKIE, example = "'example'")
    public static final String   propertyCookie           = "Cookie";
	@PluginFieldDescription(parameter = "AllCookies", description = R.WEB_PLUGIN_PROPERTY_ALL_COOKIES, example = "[Cookie{example:100}, Cookie{abc:18}]")
    public static final String   propertyAllCookies       = "AllCookies";
	@PluginFieldDescription(parameter = "AddCookie", description = R.WEB_PLUGIN_PROPERTY_ADD_COOKIE, example = "new CookieBean('newBean', 'newValue').setPath('http://example.com').setDomain('example.com').setExpary(new Date()).setSecure(true).setHttpOnly(false)")
    public static final String   propertyAddCookie        = "AddCookie";
	@PluginFieldDescription(parameter = "RemoveCookie", description = R.WEB_PLUGIN_PROPERTY_REMOVE_COOKIE, example = "'example'")
    public static final String   propertyRemoveCookie     = "RemoveCookie";
	@PluginFieldDescription(parameter = "RemoveAllCookies", description = R.WEB_PLUGIN_PROPERTY_REMOVE_ALL_COOKIES, example = "")
    public static final String   propertyRemoveAllCookies = "RemoveAllCookies";


	@PluginFieldDescription(parameter = "AdditionalParameters", description = R.WEB_PLUGIN_PROPERTY_ADDITIONAL_PARAMETERS, example = "--disable-init-reload --enable-chromium-window-alert")
	public static final String additionalParameters = "AdditionalParameters";
	
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

        info.addTypes(ControlKind.Any, PluginInfo.ANY_TYPE);
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
        info.addTypes(ControlKind.Wait,PluginInfo.ANY_TYPE);

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
			        chromeDriverBinary, additionalParameters, firefoxProfileDir, usePrivateMode, isDriverLogging, logLevel, trimTextName };
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
