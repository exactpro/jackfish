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
	private static final String NAVIGATE_TO = "Navigate to";
	private static final String NAVIGATE_BACK = "Navigate back";
	private static final String NAVIGATE_FORWARD = "Navigate forward";
	private static final String FIND_BY = "Find by";
	private static final String CLICK = "Click";
	private static final String ENTER_TEXT = "Enter text";
	private static final String EXECUTE_SCRIPT = "Execute script";

	private Histogram findByHistogram;
	private Histogram clickHistogram;
	private Histogram enterTextHistogram;
	private Histogram executeScriptHistogram;

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
		this.findByHistogram = new Histogram(FIND_BY, this.logger);
		this.clickHistogram = new Histogram(CLICK, this.logger);
		this.enterTextHistogram = new Histogram(ENTER_TEXT, this.logger);
		this.executeScriptHistogram = new Histogram(EXECUTE_SCRIPT, this.logger);
	}


	@Override
	public void beforeNavigateTo(String url, WebDriver driver)
	{
		this.timeNavigateTo = System.currentTimeMillis();
	}

	@Override
	public void beforeNavigateBack(WebDriver driver)
	{
		this.timeNavigateBack = System.currentTimeMillis();
	}

	@Override
	public void beforeNavigateForward(WebDriver driver)
	{
		this.timeNavigateForward = System.currentTimeMillis();
	}

	@Override
	public void beforeFindBy(By by, WebElement element, WebDriver driver)
	{
		this.timeFindBy = System.currentTimeMillis();
	}

	@Override
	public void beforeClickOn(WebElement element, WebDriver driver)
	{
		this.timeClick = System.currentTimeMillis();
	}

	@Override
	public void beforeChangeValueOf(WebElement element, WebDriver driver)
	{
		this.timeText = System.currentTimeMillis();
	}

	@Override
	public void beforeScript(String script, WebDriver driver)
	{
		this.timeScript = System.currentTimeMillis();
	}

	@Override
	public void afterNavigateTo(String url, WebDriver driver)
	{
		this.log(NAVIGATE_TO, this.timeNavigateTo);
	}

	@Override
	public void afterNavigateBack(WebDriver driver)
	{
		this.log(NAVIGATE_BACK, this.timeNavigateBack);
	}

	@Override
	public void afterNavigateForward(WebDriver driver)
	{
		this.log(NAVIGATE_FORWARD, this.timeNavigateForward);
	}

	@Override
	public void afterFindBy(By by, WebElement element, WebDriver driver)
	{
		this.findByHistogram.add(this.timeFindBy);
		this.log(FIND_BY, this.timeFindBy);
	}

	@Override
	public void afterClickOn(WebElement element, WebDriver driver)
	{
		this.clickHistogram.add(this.timeClick);
		this.log(CLICK, this.timeClick);
	}

	@Override
	public void afterChangeValueOf(WebElement element, WebDriver driver)
	{
		this.clickHistogram.add(this.timeText);
		this.log(ENTER_TEXT, this.timeText);
	}

	@Override
	public void afterScript(String script, WebDriver driver)
	{
		this.executeScriptHistogram.add(this.timeScript);
		this.log(EXECUTE_SCRIPT, this.timeScript);
	}

	@Override
	public void onException(Throwable throwable, WebDriver driver)
	{
		
	}

	public void reportAll()
	{
		this.findByHistogram.report();
		this.clickHistogram.report();
		this.enterTextHistogram.report();
		this.executeScriptHistogram.report();
	}

	private void log(String msg, long time)
	{
		this.logger.debug("$$$$ " + msg + ", time : " + (System.currentTimeMillis() - time) + "ms");
	}
}
