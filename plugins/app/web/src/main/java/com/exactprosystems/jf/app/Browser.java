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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.safari.SafariDriver;

public enum Browser
{
	
	ANDROIDCHROME {
		@Override
		public WebDriver createDriver(WebPluginArguments arguments) throws Exception
		{
			setChromeDriverPath(arguments);
			
			if (arguments.isDriverLogging())
			{
				logger.info("Selenium driver logging is enabled");
				System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "androidChrome.log");
				System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");
			}
			if (arguments.isUsePrivateMode()) {
				logger.warn(String.format("Private mode for %s browser is not supported", arguments.getBrowserName()));
			}
			
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.setExperimentalOption("androidPackage", "com.android.chrome");

			return new ChromeDriver(chromeOptions);
		}
	},
	ANDROIDBROWSER {
		@Override
		public WebDriver createDriver(WebPluginArguments arguments) throws Exception
		{
			setChromeDriverPath(arguments);
			
			if (arguments.isDriverLogging())
			{
				logger.info("Selenium driver logging is enabled");
				System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, logFile("androidBrowser.log"));
				System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");
			}
			if (arguments.isUsePrivateMode()) {
				logger.warn(String.format("Private mode for %s browser is not supported", arguments.getBrowserName()));
			}
			
			ChromeOptions browserOptions = new ChromeOptions();
			browserOptions.setExperimentalOption("androidPackage", "com.android.browser");
			browserOptions.setExperimentalOption("androidActivity","com.android.browser.BrowserActivity");
	
			return new ChromeDriver(browserOptions);
		}
	},
	FIREFOX {
		@Override
		public WebDriver createDriver(WebPluginArguments arguments) throws Exception
		{
			setGeckoDriverPath(arguments);
			
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			if (arguments.getFirefoxProfileDirectory() != null)
			{
				firefoxOptions.setProfile(new FirefoxProfile(new File(arguments.getFirefoxProfileDirectory())));
				logger.info(String.format("Firefox profile path is set to %s", arguments.getFirefoxProfileDirectory()));
			}
			if (arguments.isDriverLogging())
			{
				logger.info("Selenium driver logging is enabled");
				firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
				firefoxOptions.setCapability(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, logFile("firefoxDriver.log"));
			}
			if (arguments.isUsePrivateMode()) {
				logger.warn(String.format("Private mode for %s browser is not supported", arguments.getBrowserName()));
			}
			return new FirefoxDriver(firefoxOptions);
		}
	},
	CHROME {
		@Override
		public WebDriver createDriver(WebPluginArguments arguments) throws Exception
		{
			setChromeDriverPath(arguments);
			
			if (arguments.isDriverLogging())
			{
				logger.info("Selenium driver logging is enabled");
				System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, logFile("chromeDriver.log"));
				System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");
			}
			if (arguments.isUsePrivateMode()) {
				logger.warn(String.format("Private mode for %s browser is not supported", arguments.getBrowserName()));
			}
			
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--disable-extensions");
			if (arguments.getChromeDriverBinary() != null)
			{ 
				options.setBinary(new File(arguments.getChromeDriverBinary()));
				logger.info(String.format("Chrome binary location is set to %s", arguments.getChromeDriverBinary()));
			}
			
			return new ChromeDriver(options);
		}
	},
	INTERNETEXPLORER {
		@Override
		public WebDriver createDriver(WebPluginArguments arguments) throws Exception
		{
			setIEDriverPath(arguments);

			InternetExplorerOptions ieOptions = new InternetExplorerOptions();
			ieOptions.setCapability("ie.enableFullPageScreenshot", false);
			ieOptions.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
			if (arguments.isDriverLogging())
			{
				logger.info("Selenium driver logging is enabled");
				ieOptions.setCapability(InternetExplorerDriver.LOG_FILE, logFile("ieDriver.log"));
				ieOptions.setCapability(InternetExplorerDriver.LOG_LEVEL, "DEBUG");
			}
			if (arguments.isUsePrivateMode())
			{
				logger.info("Starting browser in private mode");
				ieOptions.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
				ieOptions.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
			}
			return new InternetExplorerDriver(ieOptions);
		}
	},
	OPERA {
		@Override
		public WebDriver createDriver(WebPluginArguments arguments) throws Exception
		{
			logger.warn(String.format("%s browser is untested and may not work as expected", arguments.getBrowserName()));
			return new OperaDriver();
		}
	},
	PHANTOMJS {
		@Override
		public WebDriver createDriver(WebPluginArguments arguments) throws Exception
		{
			logger.warn(String.format("%s browser is untested and may not work as expected", arguments.getBrowserName()));
			return new PhantomJSDriver();
		}
	},
	SAFARI {
		@Override
		public WebDriver createDriver(WebPluginArguments arguments) throws Exception
		{
			logger.warn(String.format("%s browser is untested and may not work as expected", arguments.getBrowserName()));
			return new SafariDriver();
		}
	};

	public abstract WebDriver createDriver(WebPluginArguments arguments) throws Exception;
	
	private static final Logger logger = Logger.getLogger(Browser.class);

	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_");

	private static String logFile(String baseName)
	{
		return formatter.format(new Date()) + baseName;
	}
	
	private static String formatAbsentArgumentText(String argumentName, String browserName)
	{
		// TODO use string from I8N
		return String.format(
			"%s argument should be specified for %s browser", argumentName, browserName);
	}
	
	private static void setChromeDriverPath(WebPluginArguments arguments) throws Exception
	{
		String driverPath = arguments.getChromeDriverPath(); 
		if (driverPath == null) {
			throw new Exception(
					formatAbsentArgumentText(WebAppFactory.chromeDriverPathName, arguments.getBrowserName()));
		}
		System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, driverPath);
		logger.info(String.format("Using chrome driver binary at %s", driverPath));
	}

	private static void setGeckoDriverPath(WebPluginArguments arguments) throws Exception
	{
		String driverPath = arguments.getGeckoDriverPath();
		if (driverPath == null) {
			throw new Exception(
					formatAbsentArgumentText(WebAppFactory.geckoDriverPathName, arguments.getBrowserName()));
		}
		System.setProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, driverPath);
		logger.info(String.format("Using gecko driver binary at %s", driverPath));
	}

	private static void setIEDriverPath(WebPluginArguments arguments) throws Exception
	{
		String driverPath = arguments.getIEDriverPath();
		if (driverPath == null) {
			throw new Exception(
					formatAbsentArgumentText(WebAppFactory.ieDriverPathName, arguments.getBrowserName()));
		}
		System.setProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY, driverPath);
		logger.info(String.format("Using IE driver binary at %s", driverPath));
	}

}
