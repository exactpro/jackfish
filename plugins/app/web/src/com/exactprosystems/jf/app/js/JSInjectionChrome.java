////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app.js;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class JSInjectionChrome extends JSInjection
{
	private final String x = "x";
	private final String y = "y";

	private final String to		= "toElement";
	private final String out	= "fromElement";
	@Override
	public void injectJSHighlight(WebDriver driver)
	{
		String locationChrome = getHighlight(to, out);
		((JavascriptExecutor) driver).executeScript(locationChrome);
	}

	@Override
	public void injectJSLocation(WebDriver driver)
	{
		String locationChrome = getLocation(x, y);
		((JavascriptExecutor) driver).executeScript(locationChrome);
	}
}
