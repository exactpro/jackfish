////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.Select;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SeleniumOperationExecutor implements OperationExecutor<WebElement>
{
	private static final String tag_tbody = "tbody";
	private static final String tag_tr = "tr";
	private static final String tag_td = "td";
	private static final String tag_thead = "thead";
	private static final String tag_th = "th";
	private final int repeatLimit = 4;

	public SeleniumOperationExecutor(EventFiringWebDriver driver, Logger logger)
	{
		this.driver = driver;
		this.logger = logger;
	}

	@Override
	public boolean tableIsContainer()
	{
		return true;
	}

	@Override
	public boolean mouseTable(WebElement component, int column, int row, MouseAction action) throws Exception
	{
		return false; 	// the realization is not needed because table is a container
	}

	@Override
	public String getValueTableCell(WebElement component, int column, int row) throws Exception
	{
		return null;	// the realization is not needed because table is a container
	}

	@Override
	public boolean textTableCell(WebElement component, int column, int row, String text) throws Exception
	{
		return false; 	// the realization is not needed because table is a container 
	}

	@Override
	public String get(WebElement component) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (component.getTagName().equals("input"))
				{
					return component.getAttribute("value");
				}
				return component.getText();
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
				//				throw new StaleElementReferenceException("Element is no longer attached to the DOM");
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public Map<String, String> getRow(WebElement component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		int repeat = 1;
		Exception real = null;
		do
		{
			try
			{
				List<Map<String, String>> list = new ArrayList<>();

				List<String> headers = getHeaders(component, useNumericHeader, header);

				List<WebElement> rows = findRows(additional, component);

				for (WebElement row : rows)
				{
					if (rowMatches(row, valueCondition, colorCondition, headers))
					{
						list.add(getRowValues(row, headers));
					}
				}
				if (list.size() == 1)
				{
					return list.get(0);
				}

				throw new RemoteException("Found " + list.size() + " rows instead 1.");
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
				//				throw new StaleElementReferenceException("Element is no longer attached to the DOM");
			}
			catch (Exception e)
			{
				logger.error(String.format("Error getRow(%s, %s, %s)", component, valueCondition, colorCondition));
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public List<String> getRowIndexes(WebElement component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<String> result = new ArrayList<>();
				List<WebElement> rows = findRows(additional, component);
				for (int i = 0; i < rows.size(); i++)
				{
					WebElement row = rows.get(i);
					if (rowMatches(row, valueCondition, colorCondition, getHeaders(component, useNumericHeader, header)))
					{
						result.add(String.valueOf(i));
					}
				}
				return result;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
				//				throw new StaleElementReferenceException("Element is no longer attached to the DOM");
			}
			catch (Exception e)
			{
				logger.error(String.format("Error getRowIndexes(%s, %s, %s)", component, valueCondition, colorCondition));
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public Map<String, String> getRowByIndex(WebElement component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<String> headers = getHeaders(component, useNumericHeader, header);
				List<WebElement> rows = findRows(additional, component);
				if (i > rows.size() - 1 || i < 0)
				{
					throw new RemoteException("Invalid index : " + i + ", max index : " + (rows.size() - 1));
				}
				return getRowValues(rows.get(i), headers);

			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("Error on get row by index");
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public Map<String, ValueAndColor> getRowWithColor(WebElement component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<String> headers = getHeaders(component, useNumericHeader, header);
				List<WebElement> rows = findRows(additional, component);

				if (rows.isEmpty())
				{
					logger.error("Table is empty");
					throw new RemoteException("Table is empty");
				}

				if (i < 0 || i > rows.size() - 1)
				{
					throw new RemoteException("Invalid index=[" + i + "]. Maximum index=[" + (rows.size() - 1) + "].");
				}
				return valueFromRow(rows.get(i), headers);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("Error on get row with color");
				logger.error(e.getMessage(), e);
				throw new RemoteException(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public String[][] getTable(WebElement component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		String outerHTML = component.getAttribute("outerHTML");
		Document doc = Jsoup.parse(outerHTML);
		List<String> headers;
		if (header != null)
		{
			WebElement element = find(null, header);
			headers = getHeaders(element.getAttribute("outerHTML"), useNumericHeader);
		}
		else
		{
			headers = getHeaders(outerHTML, useNumericHeader);
		}
		logger.debug("Headers : " + headers);
		Elements rows = findRows(doc);
		logger.debug("Rows size : " + rows.size());
		String[][] res = new String[rows.size() + 1][headers.size()];
		for (int i = 0; i < res[0].length; i++)
		{
			res[0][i] = headers.get(i);
		}
		for (int i = 1; i < res.length; i++)
		{
			Element row = rows.get(i - 1);
			Elements cells = row.children();
			for (int j = 0; j < cells.size(); j++)
			{
				res[i][j] = cells.get(j).text();
			}
		}
		return res;
	}

	private List<String> getHeaders(String outerHtml, boolean useNumericHeader) throws RemoteException
	{
		Document doc = Jsoup.parse(outerHtml);
		ArrayList<String> result = new ArrayList<>();
		Elements header = null;
		/*
			try to find element with tag thead.
		 */
		Element firstThead = doc.select(tag_thead).first();
		/*
			if firstThead thead is not present, try to find rows in this table.
		 */
		if (firstThead == null)
		{
			Element firstTr = doc.select(tag_tr).first();
			if (firstTr == null)
			{
				throw new RemoteException("Table is empty");
			}
			header = firstTr.children();
			for (int i = 0; i < header.size(); i++)
			{
				result.add(String.valueOf(i));
			}
			return result;
		}
		Elements trOfFirstThead = firstThead.children();
		header = trOfFirstThead.last().children();

		if (useNumericHeader)
		{
			for (int i = 0; i < header.size(); i++)
			{
				if (header.get(i).tag().getName().equals(tag_th))
				{
					result.add(String.valueOf(i));
				}
			}
		}
		else
		{
			for (Element element : header)
			{
				if (element.tag().getName().equals(tag_th))
				{
					result.add(element.text());
				}
			}
		}
		return result;
	}

	public static Elements findRows(Document doc)
	{
		return doc.select(tag_tbody).first().children();
	}

	@Override
	public Color getColor(String color) throws Exception
	{
		if (color == null)
		{
			return null;
		}

		if (color.equalsIgnoreCase("transparent"))
		{
			return new Color(255, 255, 255, 0);
		}
		StringBuilder colorSB = new StringBuilder(color);
		colorSB.delete(0, 5);
		colorSB.deleteCharAt(colorSB.length() - 1);
		String[] colors = colorSB.toString().split(", ");
		return new Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]), Integer.parseInt(colors[3]));
	}

	public List<WebElement> findAll(ControlKind controlKind, WebElement window, Locator locator) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				By by = new MatcherSelenium(controlKind, locator);

				List<WebElement> ret = (window == null) ? driver.findElements(by) : window.findElements(by);
				return ret;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public WebElement find(Locator owner, Locator locator) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				WebElement window = null;

				if (owner != null)
				{
					List<WebElement> elements = findAll(owner.getControlKind(), null, owner);

					if (elements.isEmpty())
					{
						throw new RemoteException("Owner was not found. Owner: " + owner);
					}

					if (elements.size() > 1)
					{
						throw new RemoteException(elements.size() + " owners were found instead 1. Owner: " + owner);
					}
					window = elements.get(0);
				}

				ControlKind controlKind = locator.getControlKind();

				List<WebElement> elements = findAll(controlKind, window, locator);

				if (elements.isEmpty())
				{
					if (locator.isWeak())
					{
						return new DummyWebElement();
					}
					else
					{
						throw new RemoteException("No one element was found. Element: " + locator);
					}
				}

				if (elements.size() > 1)
				{
					for (WebElement element : elements)
					{
						logger.error("Found : " + getElementString(element));
					}
					throw new RemoteException(elements.size() + " elements were found instead 1. Element: " + locator);
				}
				return elements.get(0);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private String getElementString(WebElement element)
	{
		String s = element.getAttribute("outerHTML");
		return s.substring(0, s.indexOf(">")+1);
	}

	@Override
	public WebElement lookAtTable(WebElement tableComp, Locator additional, Locator header, int x, int y) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			logger.info("findIntoTable(" + tableComp + ", " + x + ", " + y + ")" + (additional == null ? "" : "additional " + additional));
			try
			{
				if (y < 0)
				{
					List<WebElement> headers = getHeaders(tableComp);
					return headers.get(x);
				}
				else if (x < 0)
				{
					List<WebElement> rows = findRows(additional, tableComp);
					return rows.get(y);
				}
				else
				{
					logger.debug("Row = " + y);
					List<WebElement> rows = findRows(additional, tableComp);
					logger.debug("Rows count = " + rows.size());
					WebElement row1 = rows.get(y);
					logger.debug("Column = " + x);
					List<WebElement> cells1 = row1.findElements(By.tagName(tag_td));
					logger.debug("Column count " + cells1.size());
					return cells1.get(x);
				}
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("Error on find into table");
				logger.error(e.getMessage(), e);
				throw new RemoteException("Error on find into table");
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean mouse(WebElement component, int x, int y, MouseAction action) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				Actions clicker = new Actions(driver);
				switch (action)
				{
					case Move:
						if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
						{
							clicker.moveToElement(component).perform();
						}
						else
						{
							clicker.moveToElement(component, x, y).perform();
						}
						break;

					case LeftClick:
						if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
						{
							clicker.moveToElement(component).click().perform();
						}
						else
						{
							clicker.moveToElement(component, x, y).click().perform();
						}
						break;

					case LeftDoubleClick:
						if (driver.getWrappedDriver() instanceof ChromeDriver)
						{
							driver.executeScript("var evt = document.createEvent('MouseEvents');" + "evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);" + "arguments[0].dispatchEvent(evt);", component);
						}
						else
						{
							if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
							{
								clicker.moveToElement(component).doubleClick().perform();
							}
							else
							{
								clicker.moveToElement(component, x, y).doubleClick().perform();
							}
						}
						break;

					case RightClick:
						if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE)
						{
							clicker.contextClick(component).perform();
						}
						else
						{
							clicker.moveToElement(component, x, y).contextClick().perform();
						}
						break;

					case RightDoubleClick:
						return false;

				}


				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean push(WebElement component) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				component.click();
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean text(WebElement component, String text, boolean clear) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (clear)
				{
					component.clear();
				}
				component.sendKeys(text);
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean toggle(WebElement component, boolean value) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				component.click();
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean select(WebElement component, String selectedText) throws Exception
	{
		new Select(component).selectByValue(selectedText);
		return true;
	}

	@Override
	public boolean fold(WebElement component, String path, boolean collaps) throws Exception
	{
		// TODO process the parameter path
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				component.click();
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean wait(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong) throws Exception
	{
		long begin = System.currentTimeMillis();
		try
		{
			Exception real = null;
			int repeat = 1;
			do
			{
				try
				{
					logger.debug("Wait to "+ (toAppear ? "" : "Dis" )+"appear for " + locator +" on time " + ms);
					final By by = new MatcherSelenium(ControlKind.Wait, locator);

					long time = System.currentTimeMillis();
					while (System.currentTimeMillis() < time + ms)
					{
						try
						{
							List<WebElement> elements = driver.findElements(by);
							if (toAppear)
							{
								if (elements.size() > 0)
								{
									return true;
								}
							}
							else
							{
								if (elements.size() == 0)
								{
									return true;
								}
							}
						}
						catch (Exception e)
						{
							logger.error("Error on waiting");
							logger.error(e.getMessage(), e);
						}
					}
					return false;
				}
				catch (StaleElementReferenceException e)
				{
					real = e;
					logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
				}
			}
			while (++repeat < repeatLimit);
			throw real;
		}
		finally
		{
			if (atomicLong != null)
			{
				atomicLong.set(System.currentTimeMillis() - begin);
			}
		}
	}

	@Override
	public boolean press(WebElement component, Keyboard key) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				switch (key)
				{
					case Down:
						component.sendKeys(Keys.DOWN);
						break;

					case Escape:
						component.sendKeys(Keys.ESCAPE);
						break;

					case Enter:
						component.sendKeys(Keys.ENTER);
						break;

					case Tab:
						component.sendKeys(Keys.TAB);
						break;

					case Delete:
						component.sendKeys(Keys.DELETE);
						break;

					case Backspase:
						component.sendKeys(Keys.BACK_SPACE);
						break;

					case Shift:
						component.sendKeys(Keys.SHIFT);

					default:
						return false;
				}
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public boolean upAndDown(WebElement component, Keyboard key, boolean b) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (b)
				{
					upAndDownActions = new Actions(driver);
				}
				switch (key)
				{
					case F2:
						upOrDown(upAndDownActions, Keys.F2, b);
						break;

					case DIG1:
						upOrDown(upAndDownActions, Keys.NUMPAD1, b);
						break;

					case Shift:
						upOrDown(upAndDownActions, Keys.SHIFT, b);
						break;

					case Backspase:
						upOrDown(upAndDownActions, Keys.BACK_SPACE, b);
						break;

					case Delete:
						upOrDown(upAndDownActions, Keys.DELETE, b);
						break;

					case Enter:
						upOrDown(upAndDownActions, Keys.ENTER, b);
						break;

					case Escape:
						upOrDown(upAndDownActions, Keys.ESCAPE, b);
						break;

					case Tab:
						upOrDown(upAndDownActions, Keys.TAB, b);
						break;

					default:
						return false;
				}
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private void upOrDown(Actions actions, Keys keys, boolean down)
	{
		if (down)
		{
			actions.keyDown(keys).perform();
		}
		else
		{
			actions.keyUp(keys).perform();
		}
	}

	@Override
	public boolean setValue(WebElement component, double value) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				Actions actions = new Actions(driver);

				int height = component.getSize().getHeight();
				int width = component.getSize().getWidth();
				if (height > width)
				{
					actions.moveToElement(component, 0, (int) ((double)(value * ((double) width / 100)))).click().build().perform();
				}
				//horizontal slider
				else
				{
					actions.moveToElement(component, (int) ((double)(value * ((double) width / 100))), 0).click().build().perform();
				}

				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	public String getValue(WebElement component) throws Exception
	{
		//TODO make this method
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (component.getTagName().equals("input"))
				{
					return component.getAttribute("value");
				}
				return component.getText();
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
				//throw new StaleElementReferenceException("Element is no longer attached to the DOM");
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private Map<String, String> getRowValues(WebElement row, List<String> headers) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				Map<String, String> result = new LinkedHashMap<>();
				List<WebElement> cells = row.findElements(By.tagName(tag_td));

				for (int i = 0; i < (headers.size() > cells.size() ? cells.size() : headers.size()); i++)
				{
					String key = headers.get(i);
					WebElement webElement = cells.get(i);

					result.put(key, webElement.getText());
				}

				return result;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private List<WebElement> getHeaders(WebElement grid) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<WebElement> headerTable = grid.findElements(By.tagName(tag_thead));
				if (headerTable.isEmpty())
				{
					List<WebElement> firstRow = grid.findElements(By.tagName(tag_tr));
					if (firstRow.isEmpty())
					{
						throw new RemoteException("Table is empty");
					}
					return firstRow.get(0).findElements(By.tagName(tag_td));
				}
				return headerTable.get(0).findElements(By.tagName(tag_th));
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private List<String> getHeaders(WebElement grid, boolean useNumericHeader, Locator header) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				logger.debug("Header : " + header + "  != null : " + (header != null));
				List<String> result = new ArrayList<>();
				if (header != null)
				{
					WebElement headerTable = find(null, header);
					List<WebElement> thRows = headerTable.findElements(By.tagName(tag_th));
					if (!thRows.isEmpty())
					{
						for (WebElement thRow : thRows)
						{
							result.add(thRow.getText());
						}
						return result;
					}
					List<WebElement> tdRows = headerTable.findElements(By.tagName(tag_td));
					if (!tdRows.isEmpty())
					{
						for (WebElement tdRow : tdRows)
						{
							result.add(tdRow.getText());
						}
						return result;
					}
				}
				List<WebElement> firstHeader = null;
				List<WebElement> headerTable = grid.findElements(By.tagName(tag_thead));
				if (headerTable.isEmpty())
				{
					return getHeaders(grid, result);
				}

				List<WebElement> headers1 = headerTable.get(0).findElements(By.tagName(tag_tr));
				if (headers1.isEmpty())
				{
					return getHeaders(grid, result);
				}
				WebElement headers = headers1.get(headers1.size() - 1);
				firstHeader = headers.findElements(By.tagName(tag_th));
				logger.debug("use numeric : " + useNumericHeader);
				if (useNumericHeader)
				{
					for (int i = 0; i < firstHeader.size(); i++)
					{
						result.add(String.valueOf(i));
					}
				}
				else
				{
					for (WebElement element : firstHeader)
					{
						result.add(element.getText().trim());
					}
				}
				return result;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private List<String> getHeaders(WebElement grid, List<String> result) throws RemoteException
	{
		List<WebElement> header;
		List<WebElement> firstRow = grid.findElements(By.tagName(tag_tr));
		if (firstRow.isEmpty())
		{
			throw new RemoteException("Table is empty");
		}
		header = firstRow.get(0).findElements(By.tagName(tag_td));

		for (int i = 0; i < header.size(); i++)
		{
			result.add(String.valueOf(i));
		}
		return result;
	}


	private List<WebElement> findRows(Locator additional, WebElement component) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (additional != null)
				{
					MatcherSelenium by = new MatcherSelenium(ControlKind.Row, additional);
					return component.findElement(By.tagName(tag_tbody)).findElements(by);
				}
				else
				{
					return component.findElement(By.tagName(tag_tbody)).findElements(By.tagName(tag_tr));
				}
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private Map<String, ValueAndColor> valueFromRow(WebElement row, List<String> headers) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				Map<String, ValueAndColor> res = new LinkedHashMap<String, ValueAndColor>();

				List<WebElement> cells = row.findElements(By.tagName(tag_td));
				for (int i = 0; i < cells.size(); i++)
				{
					WebElement cell = cells.get(i);
					String name = i < headers.size() ? headers.get(i) : String.valueOf(i);
					String value = cell.getText();
					String colorFG = cell.getCssValue("color");
					String colorBG = cell.getCssValue("background-color");
					res.put(name, new ValueAndColor(value, getColor(colorFG), getColor(colorBG)));
				}

				return res;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private boolean rowMatches(WebElement row, ICondition valueCondition, ICondition colorCondition, List<String> headers) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				List<WebElement> cells = row.findElements(By.tagName(tag_td));
				for (int i = 0; i < cells.size(); i++)
				{
					WebElement cell = cells.get(i);

					String name = (i < headers.size() ? headers.get(i) : null);
					if (valueCondition != null)
					{
						if (!valueCondition.isMatched(name, cell.getText()))
						{
							return false;
						}
					}

					if (colorCondition != null)
					{
						Color color = getColor(cell.getCssValue("color"));
						Color bgColor = getColor(cell.getCssValue("background-color"));
						if (!colorCondition.isMatched2(name, color, bgColor))
						{
							return false;
						}
					}
				}
				return true;
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumOperationExecutor : " + repeat);
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private Actions upAndDownActions;
	private EventFiringWebDriver driver;
	private Logger logger;
}
