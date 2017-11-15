////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.common.Str;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;

public enum Browser
{
	ANDROIDCHROME		("AndroidChrome"),
	ANDROIDBROWSER		("AndroidBrowser"),
	FIREFOX				("Firefox"),
	CHROME				("Chrome"),
	INTERNETEXPLORER	("InternetExplorer"),
	OPERA				("Opera"),
	PHANTOMJS			("PhantomJS"),
	SAFARI				("Safari");

	private String browserName;

	Browser(String name)
	{
		this.browserName = name;
	}

	public String getBrowserName()
	{
		return browserName;
	}

	public WebDriver createDriver(String pathToBinary, String firefoxProfileDir, boolean usePrivateMode) throws Exception
	{
		switch (this)
		{
			case FIREFOX:
				if (!Str.IsNullOrEmpty(firefoxProfileDir))
				{
					FirefoxProfile profile = new FirefoxProfile(new File(firefoxProfileDir));
					return new FirefoxDriver(profile);
				}
				return new FirefoxDriver();

			case ANDROIDCHROME:
					ChromeOptions chromeOptions = new ChromeOptions();
					chromeOptions.setExperimentalOption("androidPackage", "com.android.chrome");
					DesiredCapabilities capabilities_chrome = new DesiredCapabilities();
					capabilities_chrome.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
					return new ChromeDriver(capabilities_chrome);

			case ANDROIDBROWSER:
					ChromeOptions browserOptions = new ChromeOptions();
					browserOptions.setExperimentalOption("androidPackage", "com.android.browser");
					browserOptions.setExperimentalOption("androidActivity","com.android.browser.BrowserActivity");
					DesiredCapabilities capabilities_browser = new DesiredCapabilities();
					capabilities_browser.setCapability(ChromeOptions.CAPABILITY, browserOptions);
					return new ChromeDriver(capabilities_browser);
				
			case CHROME:
				if (Str.IsNullOrEmpty(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY)))
				{
					throw new Exception("You need set the 'ChromeDriverPath' parameter on plugin to valid value");
				}
				ChromeOptions options = new ChromeOptions();
				options.addArguments("--disable-extensions"); // TODO think about it. Mb take out this on parameters for adapter?
				if (pathToBinary != null && !pathToBinary.isEmpty())
				{
					options.setBinary(new File(pathToBinary));
				}
				return new ChromeDriver(options);

			case INTERNETEXPLORER:
				if (Str.IsNullOrEmpty(System.getProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY)))
				{
					throw new Exception("You need set the 'IEDriverPath' parameter on plugin to valid value");
				}
				DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
				capabilities.setCapability("ie.enableFullPageScreenshot", false);
				capabilities.setCapability("enablePersistentHover", false);
				if (usePrivateMode)
				{
					capabilities.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
					capabilities.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
				}
				return new InternetExplorerDriver(capabilities);

			case OPERA:
				return new OperaDriver();

			case PHANTOMJS:
				return new PhantomJSDriver();

			case SAFARI:
				return new SafariDriver();

			default:
				throw new Exception("Unknown browser : " + this.browserName);
		}

	}
}
