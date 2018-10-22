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

import java.util.regex.Pattern;

public class WebPluginArguments {
	
	private String browserName;
	private String startUrl;
	
	private String chromeDriverPath;
	private String chromeDriverBinary;
	private String firefoxProfileDirectory;
	private String geckoDriverPath;
	private String ieDriverPath;
	private boolean usePrivateMode = false;
	private boolean isDriverLogging = false;
	
	public String getBrowserName()
	{
		return this.browserName;
	}
	
	public void setBrowserName(String browserName)
	{
		this.browserName = getNullIfEmpty(browserName);
	}
	
	public String getStartUrl()
	{
		return this.startUrl;
	}
	
	public void setStartUrl(String url)
	{
		this.startUrl = getNullIfEmpty(url);
	}
	
	public String getChromeDriverPath()
	{
		return this.chromeDriverPath;
	}
	
	public void setChromeDriverPath(String chromeDriverPath)
	{
		this.chromeDriverPath = getNullIfEmpty(chromeDriverPath);
	}
	
	public String getChromeDriverBinary()
	{
		return this.chromeDriverBinary;
	}
	
	public void setChromeDriverBinary(String chromeDriverBinary)
	{
		this.chromeDriverBinary = chromeDriverBinary;
	}
	
	public String getFirefoxProfileDirectory()
	{
		return this.firefoxProfileDirectory;
	}
	
	public void setFirefoxProfileDirectory(String firefoxProfileDirectory)
	{
		this.firefoxProfileDirectory = getNullIfEmpty(firefoxProfileDirectory);
	}
	
	public String getGeckoDriverPath()
	{
		return this.geckoDriverPath;
	}
	
	public void setGeckoDriverPath(String geckoDriverPath)
	{
		this.geckoDriverPath = getNullIfEmpty(geckoDriverPath);
	}
	
	public String getIEDriverPath()
	{
		return this.ieDriverPath;
	}
	
	public void setIEDriverPath(String ieDriverPath)
	{
		this.ieDriverPath = getNullIfEmpty(ieDriverPath);
	}
	
	public boolean isUsePrivateMode()
	{
		return this.usePrivateMode;
	}
	
	public void setUsePrivateMode(boolean usePrivateMode)
	{
		this.usePrivateMode = usePrivateMode;
	}
	
	public boolean isDriverLogging()
	{
		return this.isDriverLogging;
	}
	
	public void setDriverLogging(boolean isDriverLogging)
	{
		this.isDriverLogging = isDriverLogging;
	}
	
	private static Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s*");
	
	private static boolean isEmptyOrWhiteSpaceOnly(String str)
	{
		return (str == null) || WHITE_SPACE_PATTERN.matcher(str).matches();
	}
	
	private static String getNullIfEmpty(String str)
	{
		if (isEmptyOrWhiteSpaceOnly(str)) {
			return null;
		}
		return str;
	}
}
