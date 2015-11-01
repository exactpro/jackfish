////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

public class DummyWebElement implements WebElement
{
	public DummyWebElement()
	{
	}
	
	@Override
	public void clear()
	{
	}

	@Override
	public void click()
	{
	}

	@Override
	public WebElement findElement(By arg0)
	{
		return null;
	}

	@Override
	public List<WebElement> findElements(By arg0)
	{
		return null;
	}

	@Override
	public String getAttribute(String arg0)
	{
		return null;
	}

	@Override
	public String getCssValue(String arg0)
	{
		return null;
	}

	@Override
	public Point getLocation()
	{
		return null;
	}

	@Override
	public Dimension getSize()
	{
		return null;
	}

	@Override
	public String getTagName()
	{
		return null;
	}

	@Override
	public String getText()
	{
		return null;
	}

	@Override
	public boolean isDisplayed()
	{
		return false;
	}

	@Override
	public boolean isEnabled()
	{
		return false;
	}

	@Override
	public boolean isSelected()
	{
		return false;
	}

	@Override
	public void sendKeys(CharSequence... arg0)
	{
	}

	@Override
	public void submit()
	{
	}
}
