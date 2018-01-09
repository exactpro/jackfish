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
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

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

	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_");

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
				FirefoxOptions firefoxOptions = new FirefoxOptions();
				if (!Str.IsNullOrEmpty(firefoxProfileDir))
				{
					FirefoxProfile profile = new FirefoxProfile(new File(firefoxProfileDir));
					firefoxOptions.setProfile(profile);
				}
				firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
				firefoxOptions.setCapability(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, logFile("firefoxDriver.log"));
				return new FirefoxDriver(firefoxOptions);

			case ANDROIDCHROME:
				System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "androidChrome.log");
				System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");

				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.setExperimentalOption("androidPackage", "com.android.chrome");

				return new ChromeDriver(chromeOptions);

			case ANDROIDBROWSER:
				System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, logFile("androidBrowser.log"));
				System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");

				ChromeOptions browserOptions = new ChromeOptions();
				browserOptions.setExperimentalOption("androidPackage", "com.android.browser");
				browserOptions.setExperimentalOption("androidActivity","com.android.browser.BrowserActivity");

				return new ChromeDriver(browserOptions);
				
			case CHROME:
				if (Str.IsNullOrEmpty(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY)))
				{
					throw new Exception("You need set the 'ChromeDriverPath' parameter on plugin to valid value");
				}
				System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, logFile("chromeDriver.log"));
				System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");

				ChromeOptions options = new ChromeOptions();
				options.addArguments("--disable-extensions");
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
				InternetExplorerOptions ieOptions = new InternetExplorerOptions();
				ieOptions.setCapability("ie.enableFullPageScreenshot", false);
				ieOptions.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
				ieOptions.setCapability(InternetExplorerDriver.LOG_FILE, logFile("ieDriver.log"));
				ieOptions.setCapability(InternetExplorerDriver.LOG_LEVEL, "DEBUG");
				if (usePrivateMode)
				{
					ieOptions.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
					ieOptions.setCapability(InternetExplorerDriver.IE_SWITCHES, Collections.singletonList("-private"));
				}
				return new InternetExplorerDriver(ieOptions);

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

	private static String logFile(String init)
	{
		return formatter.format(new Date()) + init;
	}
}
