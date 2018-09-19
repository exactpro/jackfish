/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
