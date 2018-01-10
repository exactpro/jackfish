package com.exactprosystems.jf.app;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverCommandExecutor;

import java.io.File;

import static org.openqa.selenium.ie.InternetExplorerDriver.*;

/**
 * Use this class instead of {@link org.openqa.selenium.ie.InternetExplorerDriver}, if need use private mode for the IE.
 * Remove this options, when {@link org.openqa.selenium.ie.InternetExplorerDriver} will fixed.
 */
public class CustomInternetExplorerDriver extends RemoteWebDriver
{
	public CustomInternetExplorerDriver(Capabilities capabilities)
	{
		if (capabilities == null)
		{
			capabilities = DesiredCapabilities.internetExplorer();
		}

		capabilities = new CustomInternetExplorerOptions(capabilities);
		InternetExplorerDriverService service = setupService(capabilities, 0);

		run(service, capabilities);
	}

	private InternetExplorerDriverService setupService(Capabilities caps, int port)
	{
		InternetExplorerDriverService.Builder builder = new InternetExplorerDriverService.Builder();
		builder.usingPort(port);

		if (caps != null)
		{
			if (caps.getCapability(LOG_FILE) != null)
			{
				String value = (String) caps.getCapability(LOG_FILE);
				if (value != null)
				{
					builder.withLogFile(new File(value));
				}
			}

			if (caps.getCapability(LOG_LEVEL) != null)
			{
				String value = (String) caps.getCapability(LOG_LEVEL);
				if (value != null)
				{
					builder.withLogLevel(InternetExplorerDriverLogLevel.valueOf(value));
				}
			}

			if (caps.getCapability(HOST) != null)
			{
				String value = (String) caps.getCapability(HOST);
				if (value != null)
				{
					builder.withHost(value);
				}
			}

			if (caps.getCapability(EXTRACT_PATH) != null)
			{
				String value = (String) caps.getCapability(EXTRACT_PATH);
				if (value != null)
				{
					builder.withExtractPath(new File(value));
				}
			}

			if (caps.getCapability(SILENT) != null)
			{
				Boolean value = (Boolean) caps.getCapability(SILENT);
				if (value != null)
				{
					builder.withSilent(value);
				}
			}
		}

		return builder.build();
	}

	private void run(InternetExplorerDriverService service, Capabilities capabilities)
	{
		this.assertOnWindows();
		super.setCommandExecutor(new DriverCommandExecutor(service));
		super.startSession(capabilities);
	}

	protected void assertOnWindows()
	{
		Platform current = Platform.getCurrent();
		if (!current.is(Platform.WINDOWS))
		{
			throw new WebDriverException(String.format("You appear to be running %s. The IE driver only runs on Windows.", current));
		}
	}
}
