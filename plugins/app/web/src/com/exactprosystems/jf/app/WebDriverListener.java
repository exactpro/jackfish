////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.HistogramTransfer;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebDriverListener implements WebDriverEventListener
{
	public static final String NAVIGATE_TO = "Navigate to";
	public static final String NAVIGATE_BACK = "Navigate back";
	public static final String NAVIGATE_FORWARD = "Navigate forward";
	public static final String FIND_BY = "Find by";
	public static final String CLICK = "Click";
	public static final String ENTER_TEXT = "Enter text";
	public static final String EXECUTE_SCRIPT = "Execute script";

	private long timeNavigateTo;
	private long timeNavigateBack;
	private long timeNavigateForward;
	private long timeFindBy;
	private long timeClick;
	private long timeText;
	private long timeScript;
	
	private Logger logger;
	private Map<String, List<HistogramTransfer>> subscribers;

	public WebDriverListener(Logger logger)
	{
		this.logger = logger;
		this.subscribers = new HashMap<>();
		this.subscribers.put(NAVIGATE_TO, new ArrayList<HistogramTransfer>());
		this.subscribers.put(NAVIGATE_BACK, new ArrayList<HistogramTransfer>());
		this.subscribers.put(NAVIGATE_FORWARD, new ArrayList<HistogramTransfer>());
		this.subscribers.put(FIND_BY, new ArrayList<HistogramTransfer>());
		this.subscribers.put(CLICK, new ArrayList<HistogramTransfer>());
		this.subscribers.put(ENTER_TEXT, new ArrayList<HistogramTransfer>());
		this.subscribers.put(EXECUTE_SCRIPT, new ArrayList<HistogramTransfer>());

		//		this.findByHistogram = new Histogram(FIND_BY, this.logger);
		//		this.clickHistogram = new Histogram(CLICK, this.logger);
		//		this.enterTextHistogram = new Histogram(ENTER_TEXT, this.logger);
		//		this.executeScriptHistogram = new Histogram(EXECUTE_SCRIPT, this.logger);
	}

	public void addSubscriber(HistogramTransfer transfer)
	{
		List<HistogramTransfer> histogramTransfers = this.subscribers.get(transfer.getName());
		if (histogramTransfers != null)
		{
			histogramTransfers.add(transfer);
		}
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
		this.log(NAVIGATE_TO, System.currentTimeMillis() - this.timeNavigateTo);
	}

	@Override
	public void afterNavigateBack(WebDriver driver)
	{
		this.log(NAVIGATE_BACK, System.currentTimeMillis() - this.timeNavigateBack);
	}

	@Override
	public void afterNavigateForward(WebDriver driver)
	{
		this.log(NAVIGATE_FORWARD, System.currentTimeMillis() - this.timeNavigateForward);
	}

	@Override
	public void afterFindBy(By by, WebElement element, WebDriver driver)
	{
		this.log(FIND_BY, System.currentTimeMillis() - this.timeFindBy);
	}

	@Override
	public void afterClickOn(WebElement element, WebDriver driver)
	{
		this.log(CLICK, System.currentTimeMillis() - this.timeClick);
	}

	@Override
	public void afterChangeValueOf(WebElement element, WebDriver driver)
	{
		this.log(ENTER_TEXT, System.currentTimeMillis() - this.timeText);
	}

	@Override
	public void afterScript(String script, WebDriver driver)
	{
		this.log(EXECUTE_SCRIPT, System.currentTimeMillis() - this.timeScript);
	}

	@Override
	public void onException(Throwable throwable, WebDriver driver)
	{
		
	}

	private void log(String msg, long time)
	{
		for (HistogramTransfer transfer : this.subscribers.get(msg))
		{
			transfer.add(time);
		}
		this.logger.debug("$$$$ " + msg + ", time : " + time + "ms");
	}
}
