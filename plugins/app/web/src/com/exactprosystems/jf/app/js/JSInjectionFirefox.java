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

public class JSInjectionFirefox extends JSInjection
{
	private final String x		= "pageX";
	private final String y		= "pageY";

	private final String to		= "explicitOriginalTarget";
	private final String out	= "target";

	@Override
	public void injectJSHighlight(WebDriver driver)
	{
		String highLightFF = getHighlight(to, out);
		((JavascriptExecutor) driver).executeScript(highLightFF);
	}

	@Override
	public void injectJSLocation(WebDriver driver)
	{
		String locationFF = getLocation(x, y);
		((JavascriptExecutor) driver).executeScript(locationFF);
	}
}
