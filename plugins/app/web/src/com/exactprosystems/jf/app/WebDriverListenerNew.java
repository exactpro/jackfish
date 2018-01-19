////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.common.i18n.R;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.internal.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class WebDriverListenerNew implements WebDriver, JavascriptExecutor,
		FindsById, FindsByClassName, FindsByLinkText, FindsByName,
		FindsByCssSelector, FindsByTagName, FindsByXPath,
		HasInputDevices, HasCapabilities, Interactive, TakesScreenshot,
		WrapsDriver, HasTouchScreen
{
	private final WebDriver webDriver;

	public WebDriverListenerNew(WebDriver webDriver)
	{
		this.webDriver = webDriver;
	}

	//region WebDriver
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
		List<WebElement> temp = this.webDriver.findElements(by);
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
		WebElement temp = this.webDriver.findElement(by);
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
	public Options manage()
	{
		return this.webDriver.manage();
	}
	//endregion

	//region JavascriptExecutor
	@Override
	public Object executeScript(String script, Object... args)
	{
		if (this.webDriver instanceof JavascriptExecutor)
		{
			return ((JavascriptExecutor) this.webDriver).executeScript(script, args);
		}
		throw new UnsupportedOperationException(R.WEB_DRIVER_LISTENER_NO_JS.get());
	}

	@Override
	public Object executeAsyncScript(String script, Object... args)
	{
		if (this.webDriver instanceof JavascriptExecutor)
		{
			return ((JavascriptExecutor) this.webDriver).executeAsyncScript(script, args);
		}
		throw new UnsupportedOperationException(R.WEB_DRIVER_LISTENER_NO_JS.get());
	}

	//endregion

	//region HasInputDevices
	@Override
	public Keyboard getKeyboard()
	{
		if (this.webDriver instanceof HasInputDevices)
		{
			return new KeyboardListener(this.webDriver);
		}
		throw this.create("getKeyboard");
	}

	@Override
	public Mouse getMouse()
	{
		if (this.webDriver instanceof HasInputDevices)
		{
			return new MouseListener(this.webDriver);
		}
		throw this.create("getMouse");
	}
	//endregion

	//region WrapsDriver
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
	//endregion

	//region TakesScreenshot
	@Override
	public <X> X getScreenshotAs(OutputType<X> target)
	{
		if (this.webDriver instanceof TakesScreenshot)
		{
			return ((TakesScreenshot) this.webDriver).getScreenshotAs(target);
		}
		throw this.create("getScreenshotAs");
	}
	//endregion

	//region HasTouchDevices
	public TouchScreen getTouch()
	{
		if (this.webDriver instanceof HasTouchScreen)
		{
			return ((HasTouchScreen) this.webDriver).getTouch();
		}
		throw this.create("getTouch");
	}
	//endregion

	//region Interactive
	@Override
	public void perform(Collection<Sequence> actions)
	{
		if (this.webDriver instanceof Interactive)
		{
			((Interactive) this.webDriver).perform(actions);
			return;
		}
		throw this.create("perform");
	}

	@Override
	public void resetInputState()
	{
		if (this.webDriver instanceof Interactive)
		{
			((Interactive) this.webDriver).resetInputState();
			return;
		}
		throw this.create("resetInputState");
	}
	//endregion

	//region HasCapabilities
	@Override
	public Capabilities getCapabilities()
	{
		if (this.webDriver instanceof HasCapabilities)
		{
			return ((HasCapabilities) this.webDriver).getCapabilities();
		}
		throw this.create("getCapabilities");
	}
	//endregion

	//region FindsByClassName
	@Override
	public WebElement findElementByClassName(String using)
	{
		if (this.webDriver instanceof FindsByClassName)
		{
			return ((FindsByClassName) this.webDriver).findElementByClassName(using);
		}
		throw this.create("findElementByClassName");
	}

	@Override
	public List<WebElement> findElementsByClassName(String using)
	{
		if (this.webDriver instanceof FindsByClassName)
		{
			return ((FindsByClassName) this.webDriver).findElementsByClassName(using);
		}
		throw this.create("findElementsByClassName");
	}
	//endregion

	//region FindsByCssSelector
	@Override
	public WebElement findElementByCssSelector(String using)
	{
		if (this.webDriver instanceof FindsByCssSelector)
		{
			return ((FindsByCssSelector) this.webDriver).findElementByCssSelector(using);
		}
		throw this.create("FindElementByCssSelector");
	}

	@Override
	public List<WebElement> findElementsByCssSelector(String using)
	{
		if (this.webDriver instanceof FindsByCssSelector)
		{
			return ((FindsByCssSelector) this.webDriver).findElementsByCssSelector(using);
		}
		throw this.create("FindElementsByCssSelector");
	}
	//endregion

	//region FindsById
	@Override
	public WebElement findElementById(String using)
	{
		if (this.webDriver instanceof FindsById)
		{
			return ((FindsById) this.webDriver).findElementById(using);
		}
		throw this.create("findElementById");
	}

	@Override
	public List<WebElement> findElementsById(String using)
	{
		if (this.webDriver instanceof FindsById)
		{
			return ((FindsById) this.webDriver).findElementsById(using);
		}
		throw this.create("findElementsById");
	}
	//endregion

	//region FindsByLinkText
	@Override
	public WebElement findElementByLinkText(String using)
	{
		if (this.webDriver instanceof FindsByLinkText)
		{
			return ((FindsByLinkText) this.webDriver).findElementByLinkText(using);
		}
		throw this.create("findElementByLinkText");
	}

	@Override
	public List<WebElement> findElementsByLinkText(String using)
	{
		if (this.webDriver instanceof FindsByLinkText)
		{
			return ((FindsByLinkText) this.webDriver).findElementsByLinkText(using);
		}
		throw this.create("findElementsByLinkText");
	}

	@Override
	public WebElement findElementByPartialLinkText(String using)
	{
		if (this.webDriver instanceof FindsByLinkText)
		{
			return ((FindsByLinkText) this.webDriver).findElementByPartialLinkText(using);
		}
		throw this.create("findElementByPartialLinkText");
	}

	@Override
	public List<WebElement> findElementsByPartialLinkText(String using)
	{
		if (this.webDriver instanceof FindsByLinkText)
		{
			return ((FindsByLinkText) this.webDriver).findElementsByPartialLinkText(using);
		}
		throw this.create("findElementsByPartialLinkText");
	}
	//endregion

	//region FindsByName
	@Override
	public WebElement findElementByName(String using)
	{
		if (this.webDriver instanceof FindsByName)
		{
			return ((FindsByName) this.webDriver).findElementByName(using);
		}
		throw this.create("findElementByName");
	}

	@Override
	public List<WebElement> findElementsByName(String using)
	{
		if (this.webDriver instanceof FindsByName)
		{
			return ((FindsByName) this.webDriver).findElementsByName(using);
		}
		throw this.create("findElementsByName");
	}
	//endregion

	//region FindsByTagName
	@Override
	public WebElement findElementByTagName(String using)
	{
		if (this.webDriver instanceof FindsByTagName)
		{
			return ((FindsByTagName) this.webDriver).findElementByTagName(using);
		}
		throw this.create("findElementByTagName");
	}

	@Override
	public List<WebElement> findElementsByTagName(String using)
	{
		if (this.webDriver instanceof FindsByTagName)
		{
			return ((FindsByTagName) this.webDriver).findElementsByTagName(using);
		}
		throw this.create("findElementsByTagName");
	}
	//endregion

	//region FindsByXPath
	@Override
	public WebElement findElementByXPath(String using)
	{
		if (this.webDriver instanceof FindsByXPath)
		{
			return ((FindsByXPath) this.webDriver).findElementByXPath(using);
		}
		throw this.create("findElementByXPath");
	}

	@Override
	public List<WebElement> findElementsByXPath(String using)
	{
		if (this.webDriver instanceof FindsByXPath)
		{
			return ((FindsByXPath) this.webDriver).findElementsByXPath(using);
		}
		throw this.create("findElementsByXPath");
	}
	//endregion

	private UnsupportedOperationException create(String methodName)
	{
		return new UnsupportedOperationException(String.format(R.WEB_DRIVER_LISTENER_DRIVER_UNSUPPORT.get(), methodName));
	}

	private WebElement createWebElement(WebElement element)
	{
		return new WebElementListener(element);
	}

	private class WebElementListener implements WebElement, WrapsDriver, WrapsElement, Locatable
	{
		private WebElement element;

		public WebElementListener(WebElement element)
		{
			this.element = element;
		}

		@Override
		public void click()
		{
			this.element.click();
		}

		@Override
		public void submit()
		{
			this.element.submit();
		}

		@Override
		public void sendKeys(CharSequence... keysToSend)
		{
			this.element.sendKeys(keysToSend);
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
			List<WebElement> temp = this.element.findElements(by);
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
			return this.element.findElement(by);
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
		public <X> X getScreenshotAs(OutputType<X> target)
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
			throw new UnsupportedOperationException(R.WEB_DRIVER_LISTENER_NOT_LOCATABLE.get());
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

		@Override
		public Rectangle getRect()
		{
			return this.element.getRect();
		}
	}

	private class MouseListener implements Mouse
	{
		private final Mouse mouse;

		public MouseListener(WebDriver webDriver)
		{
			this.mouse = ((HasInputDevices) webDriver).getMouse();
		}

		@Override
		public void click(Coordinates where)
		{
			this.mouse.click(where);
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
			this.mouse.mouseMove(where);
		}

		@Override
		public void mouseMove(Coordinates where, long xOffset, long yOffset)
		{
			this.mouse.mouseMove(where, xOffset, yOffset);
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

		public KeyboardListener(WebDriver driver)
		{
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
