package com.exactprosystems.jf.app;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;

import java.lang.reflect.Field;
import java.util.Map;

import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;

/**
 * Use this class instead of {@link InternetExplorerOptions}, if need use private mode for the IE.
 * Remove this class, when {@link InternetExplorerOptions} will fixed.
 */
public class CustomInternetExplorerOptions extends InternetExplorerOptions
{
	private Map<String, Object> superMap;

	public CustomInternetExplorerOptions()
	{
		super();
		this.addSuperMap();
	}

	public CustomInternetExplorerOptions(Capabilities source)
	{
		super();
		this.addSuperMap();
		setCapability("se:ieOptions", this.superMap);
		setCapability(BROWSER_NAME, BrowserType.IE);
		setCapability(PLATFORM, Platform.WINDOWS);
		setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);

		merge(source);

	}

	@Override
	public void setCapability(String key, Object value)
	{
		if (InternetExplorerDriver.IE_SWITCHES.equals(key))
		{
			try
			{
				super.setCapability(key, value);
			}
			catch (IllegalArgumentException e)
			{
				this.superMap.put(key, value);
			}
		}
		else
		{
			super.setCapability(key, value);
		}
	}

	private void addSuperMap()
	{
		try // TODO workaround
		{
			Field mapField = InternetExplorerOptions.class.getDeclaredField("ieOptions");
			mapField.setAccessible(true);
			this.superMap = (Map<String, Object>) mapField.get(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
