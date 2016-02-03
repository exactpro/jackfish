////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.HistogramMetric;
import com.exactprosystems.jf.api.app.MetricsCounter;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.internal.WrapsElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WebDriverListenerNew implements WebDriver, JavascriptExecutor, HasInputDevices, TakesScreenshot, WrapsDriver, HasTouchScreen
{
	private final WebDriver webDriver;
	private MetricsCounter metricsCounter;

	public WebDriverListenerNew(WebDriver webDriver, MetricsCounter metricsCounter)
	{
		this.webDriver = webDriver;
		this.metricsCounter = metricsCounter;
	}

	@Override
	public void get(String url)
	{
		this.webDriver.get(url);
	}

	@Override
	public String getCurrentUrl()
	{
		return this.webDriver.getCurrentUrl();
	}

	@Override
	public String getTitle()
	{
		return this.webDriver.getTitle();
	}

	@Override
	public List<WebElement> findElements(By by)
	{
		this.metricsCounter.before(HistogramMetric.Find);
		List<WebElement> temp = this.webDriver.findElements(by);
		this.metricsCounter.after(HistogramMetric.Find);
		List<WebElement> result = new ArrayList<>(temp.size());
		for (WebElement element : temp)
		{
			result.add(createWebElement(element));
		}
		return result;
	}

	@Override
	public WebElement findElement(By by)
	{
		this.metricsCounter.before(HistogramMetric.Find);
		WebElement temp = this.webDriver.findElement(by);
		this.metricsCounter.after(HistogramMetric.Find);
		return createWebElement(temp);
	}

	@Override
	public String getPageSource()
	{
		return this.webDriver.getPageSource();
	}

	@Override
	public void close()
	{
		this.webDriver.close();
	}

	@Override
	public void quit()
	{
		this.webDriver.quit();
	}

	@Override
	public Set<String> getWindowHandles()
	{
		return this.webDriver.getWindowHandles();
	}

	@Override
	public String getWindowHandle()
	{
		return this.webDriver.getWindowHandle();
	}

	@Override
	public TargetLocator switchTo()
	{
		return this.webDriver.switchTo();
	}

	@Override
	public Navigation navigate()
	{
		return this.webDriver.navigate();
	}

	@Override
	public Object executeScript(String script, Object... args)
	{
		if (this.webDriver instanceof JavascriptExecutor)
		{
			return ((JavascriptExecutor) this.webDriver).executeScript(script, args);
		}
		throw new UnsupportedOperationException("Current driver instance does not support executing javascript");
	}

	@Override
	public Object executeAsyncScript(String script, Object... args)
	{
		if (this.webDriver instanceof JavascriptExecutor)
		{
			return ((JavascriptExecutor) this.webDriver).executeAsyncScript(script, args);
		}
		throw new UnsupportedOperationException("Current driver instance does not support executing javascript");
	}

	@Override
	public Options manage()
	{
		return this.webDriver.manage();
	}

	@Override
	public Keyboard getKeyboard()
	{
		if (this.webDriver instanceof HasInputDevices)
		{
			return new KeyboardListener(this.webDriver, this.metricsCounter);
		}
		throw new UnsupportedOperationException("Current driver does not implement advanced user interactions yet.");
	}

	@Override
	public Mouse getMouse()
	{
		if (this.webDriver instanceof HasInputDevices)
		{
			return new MouseListener(this.webDriver, this.metricsCounter);
		}
		throw new UnsupportedOperationException("Current driver does not implement advanced user interactions yet.");
	}

	@Override
	public WebDriver getWrappedDriver()
	{
		if (this.webDriver instanceof WrapsDriver)
		{
			return ((WrapsDriver) this.webDriver).getWrappedDriver();
		}
		else
		{
			return this.webDriver;
		}
	}

	@Override
	public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException
	{
		if (this.webDriver instanceof TakesScreenshot)
		{
			return ((TakesScreenshot) this.webDriver).getScreenshotAs(target);
		}
		throw new UnsupportedOperationException("Current driver instance does not support taking screenshots");
	}

	public TouchScreen getTouch()
	{
		if (this.webDriver instanceof HasTouchScreen)
		{
			return ((HasTouchScreen) this.webDriver).getTouch();
		}
		throw new UnsupportedOperationException("Underlying driver does not implement advanced user interactions yet.");
	}

	private WebElement createWebElement(WebElement element)
	{
		return new WebElementListener(element, this.metricsCounter);
	}

	private class WebElementListener implements WebElement, WrapsDriver, WrapsElement, Locatable
	{
		private WebElement element;
		private MetricsCounter metricsCounter;

		public WebElementListener(WebElement element, MetricsCounter metricsCounter)
		{
			this.element = element;
			this.metricsCounter = metricsCounter;
		}

		@Override
		public void click()
		{
			metricsCounter.before(HistogramMetric.Click);
			this.element.click();
			metricsCounter.after(HistogramMetric.Click);
		}

		@Override
		public void submit()
		{
			this.element.submit();
		}

		@Override
		public void sendKeys(CharSequence... keysToSend)
		{
			metricsCounter.before(HistogramMetric.Text);
			this.element.sendKeys(keysToSend);
			metricsCounter.after(HistogramMetric.Text);
		}

		@Override
		public void clear()
		{
			this.element.clear();
		}

		@Override
		public String getTagName()
		{
			return this.element.getTagName();
		}

		@Override
		public String getAttribute(String name)
		{
			return this.element.getAttribute(name);
		}

		@Override
		public boolean isSelected()
		{
			return this.element.isSelected();
		}

		@Override
		public boolean isEnabled()
		{
			return this.element.isEnabled();
		}

		@Override
		public String getText()
		{
			return this.element.getText();
		}

		@Override
		public List<WebElement> findElements(By by)
		{
			this.metricsCounter.before(HistogramMetric.Find);
			List<WebElement> temp = this.element.findElements(by);
			this.metricsCounter.after(HistogramMetric.Find);
			List<WebElement> result = new ArrayList<>(temp.size());
			for (WebElement element : temp)
			{
				result.add(createWebElement(element));
			}
			return result;
		}

		@Override
		public WebElement findElement(By by)
		{
			this.metricsCounter.before(HistogramMetric.Find);
			WebElement temp = this.element.findElement(by);
			this.metricsCounter.after(HistogramMetric.Find);
			return temp;
		}

		@Override
		public boolean isDisplayed()
		{
			return this.element.isDisplayed();
		}

		@Override
		public Point getLocation()
		{
			return this.element.getLocation();
		}

		@Override
		public Dimension getSize()
		{
			return this.element.getSize();
		}

		@Override
		public String getCssValue(String propertyName)
		{
			return this.element.getCssValue(propertyName);
		}

		@Override
		public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException
		{
			return this.element.getScreenshotAs(target);
		}

		@Override
		public Coordinates getCoordinates()
		{
			if (this.element instanceof Locatable)
			{
				return ((Locatable) this.element).getCoordinates();
			}
			throw new UnsupportedOperationException("Current element instance does not locatable");
		}

		@Override
		public WebDriver getWrappedDriver()
		{
			return webDriver;
		}

		@Override
		public WebElement getWrappedElement()
		{
			return this.element;
		}
	}

	private class MouseListener implements Mouse
	{
		private final MetricsCounter metricsCounter;
		private final Mouse mouse;

		public MouseListener(WebDriver webDriver, MetricsCounter metricsCounter)
		{
			this.metricsCounter = metricsCounter;
			this.mouse = ((HasInputDevices) webDriver).getMouse();
		}

		@Override
		public void click(Coordinates where)
		{
			this.metricsCounter.before(HistogramMetric.Click);
			this.mouse.click(where);
			this.metricsCounter.after(HistogramMetric.Click);
		}

		@Override
		public void doubleClick(Coordinates where)
		{
			this.mouse.doubleClick(where);
		}

		@Override
		public void mouseDown(Coordinates where)
		{
			this.mouse.mouseDown(where);
		}

		@Override
		public void mouseUp(Coordinates where)
		{
			this.mouse.mouseUp(where);
		}

		@Override
		public void mouseMove(Coordinates where)
		{
			this.metricsCounter.before(HistogramMetric.Move);
			this.mouse.mouseMove(where);
			this.metricsCounter.after(HistogramMetric.Move);
		}

		@Override
		public void mouseMove(Coordinates where, long xOffset, long yOffset)
		{
			this.metricsCounter.before(HistogramMetric.Move);
			this.mouse.mouseMove(where, xOffset, yOffset);
			this.metricsCounter.after(HistogramMetric.Move);
		}

		@Override
		public void contextClick(Coordinates where)
		{
			this.mouse.contextClick(where);
		}
	}

	private class KeyboardListener implements Keyboard
	{
		private final Keyboard keyboard;
		private final MetricsCounter metricsCounter;

		public KeyboardListener(WebDriver driver, MetricsCounter metricsCounter)
		{
			this.metricsCounter = metricsCounter;
			this.keyboard = ((HasInputDevices) driver).getKeyboard();
		}

		@Override
		public void sendKeys(CharSequence... keysToSend)
		{
			this.keyboard.sendKeys(keysToSend);
		}

		@Override
		public void pressKey(CharSequence keyToPress)
		{
			this.keyboard.pressKey(keyToPress);
		}

		@Override
		public void releaseKey(CharSequence keyToRelease)
		{
			this.keyboard.releaseKey(keyToRelease);
		}
	}
}
