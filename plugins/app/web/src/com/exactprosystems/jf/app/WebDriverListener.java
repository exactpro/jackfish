////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

public class WebDriverListener implements WebDriverEventListener
{
	private long timeNavigateTo;
	private long timeNavigateBack;
	private long timeNavigateForward;
	private long timeFindBy;
	private long timeClick;
	private long timeText;
	private long timeScript;
	
	private Logger logger;
	
	public WebDriverListener(Logger logger)
	{
		this.logger = logger;
	}


	@Override
	public void beforeNavigateTo(String url, WebDriver driver)
	{
		this.timeNavigateTo = System.currentTimeMillis();
	}

	@Override
	public void afterNavigateTo(String url, WebDriver driver)
	{
		this.log(String.format("Navigate to url [%s]", url), this.timeNavigateTo);
	}

	@Override
	public void beforeNavigateBack(WebDriver driver)
	{
		this.timeNavigateBack = System.currentTimeMillis();
	}

	@Override
	public void afterNavigateBack(WebDriver driver)
	{
		this.log("Navigate back", this.timeNavigateTo);
	}

	@Override
	public void beforeNavigateForward(WebDriver driver)
	{
		this.timeNavigateForward = System.currentTimeMillis();
	}

	@Override
	public void afterNavigateForward(WebDriver driver)
	{
		this.log("Navigate forward", this.timeNavigateForward);
	}

	@Override
	public void beforeFindBy(By by, WebElement element, WebDriver driver)
	{
		this.timeFindBy = System.currentTimeMillis();
	}

	@Override
	public void afterFindBy(By by, WebElement element, WebDriver driver)
	{
		this.log("Find by", this.timeFindBy);
	}

	@Override
	public void beforeClickOn(WebElement element, WebDriver driver)
	{
		this.timeClick = System.currentTimeMillis();
	}

	@Override
	public void afterClickOn(WebElement element, WebDriver driver)
	{
		this.log("Click", this.timeClick);
	}

	@Override
	public void beforeChangeValueOf(WebElement element, WebDriver driver)
	{
		this.timeText = System.currentTimeMillis();
	}

	@Override
	public void afterChangeValueOf(WebElement element, WebDriver driver)
	{
		this.log("Set text", this.timeText);
	}

	@Override
	public void beforeScript(String script, WebDriver driver)
	{
		this.timeScript = System.currentTimeMillis();
	}

	@Override
	public void afterScript(String script, WebDriver driver)
	{
		this.log("Execute script", this.timeScript);
	}

	@Override
	public void onException(Throwable throwable, WebDriver driver)
	{
		
	}

	private void log(String msg, long time)
	{
		this.logger.debug("$$$$ " + msg + ", time : " + (System.currentTimeMillis() - time) + "ms");
	}
}
