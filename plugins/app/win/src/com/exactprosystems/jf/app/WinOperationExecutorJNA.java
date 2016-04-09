////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.Str;
import org.apache.log4j.Logger;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WinOperationExecutorJNA implements OperationExecutor<UIProxyJNA>
{
	static final String RECTANGLE_PATTERN = "(\\d+),(\\d+),(\\d+),(\\d+)";
	private Logger logger;
	private JnaDriverImpl driver;

	public WinOperationExecutorJNA(Logger logger, JnaDriverImpl driver)
	{
		this.logger = logger;
		this.driver = driver;
	}

	@Override
	public Rectangle getRectangle(UIProxyJNA component) throws Exception
	{
		try
		{
			String property = this.driver.getProperty(component.getIdString(), WindowProperty.BoundingRectangleProperty.getId());
			Rectangle rectangle = new Rectangle();
			Pattern pattern = Pattern.compile(RECTANGLE_PATTERN);
			Matcher matcher = pattern.matcher(property);
			if (matcher.matches())
			{
				rectangle.setBounds(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher
						.group(3)), Integer.parseInt(matcher.group(4)));
			}
			else
			{
				throw new RemoteException("returned rectangle not matches pattern \\d+,\\d+,\\d+,\\d+");
			}
			return rectangle;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRectangle(%s)", component));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Color getColor(String color) throws Exception
	{
		//TODO think about it
		return null;
	}

	@Override
	public List<UIProxyJNA> findAll(ControlKind controlKind, UIProxyJNA window, Locator locator) throws Exception
	{
		try
		{
			int length = 100;
			int[] result = new int[length];
			String ownerId = window == null ? null : window.getIdString();
			int count = this.driver.findAllForLocator(result, length, ownerId, locator.getControlKind()
					.ordinal(), locator.getUid(), locator.getXpath(), locator.getClazz(), locator.getName(), locator.getTitle(), locator
					.getText());
			if (count > length)
			{
				length = count;
				result = new int[length];
				this.driver.findAllForLocator(result, length, ownerId, locator.getControlKind()
						.ordinal(), locator.getUid(), locator.getXpath(), locator.getClazz(), locator.getName(), locator
						.getTitle(), locator.getText());
			}
			int foundElementCount = result[0];
			List<UIProxyJNA> returnedList = new ArrayList<>();
			int currentPosition = 1;
			for (int i = 0; i < foundElementCount; i++)
			{
				int currentArrayLength = result[currentPosition++];
				int[] elem = new int[currentArrayLength];
				for (int j = 0; j < currentArrayLength; j++)
				{
					elem[j] = result[currentPosition++];
				}
				returnedList.add(new UIProxyJNA(elem));
			}
			return returnedList;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("findALl(%s,%s,%s)", controlKind, window, locator));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<UIProxyJNA> findAll(Locator owner, Locator element) throws Exception
	{
		try
		{
			UIProxyJNA ownerElement = new UIProxyJNA(null);
			if (owner != null)
			{
				List<UIProxyJNA> allOwners = findAll(null, owner);
				if (allOwners.isEmpty())
				{
					throw new ElementNotFoundException("Owner was not found. Owner: ", owner);
				}
				if (allOwners.size() > 1)
				{
					throw new ElementNotFoundException(allOwners.size() + " owners were found instead 1. Owner: ", owner);
				}
				ownerElement = allOwners.get(0);
			}
			int length = 100;
			int[] result = new int[length];
			int count = this.driver.findAllForLocator(result, length, ownerElement.getIdString(), element.getControlKind()
					.ordinal(), element.getUid(), element.getXpath(), element.getClazz(), element.getName(), element.getTitle(), element
					.getText());
			if (count > length)
			{
				length = count;
				result = new int[length];
				this.driver.findAllForLocator(result, length, ownerElement.getIdString(), element.getControlKind()
						.ordinal(), element.getUid(), element.getXpath(), element.getClazz(), element.getName(), element
						.getTitle(), element.getText());
			}
			int foundElementCount = result[0];
			List<UIProxyJNA> returnedList = new ArrayList<>();
			int currentPosition = 1;
			for (int i = 0; i < foundElementCount; i++)
			{
				int currentArrayLength = result[currentPosition++];
				int[] elem = new int[currentArrayLength];
				for (int j = 0; j < currentArrayLength; j++)
				{
					elem[j] = result[currentPosition++];
				}
				returnedList.add(new UIProxyJNA(elem));
			}
			return returnedList;
		}
		catch (Exception e)
		{
			logger.error(String.format("findAll(%s,%s)", owner, element));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public UIProxyJNA find(Locator owner, Locator element) throws Exception
	{
		try
		{
			List<UIProxyJNA> list = findAll(owner, element);
			if (list.isEmpty())
			{
				//TODO add condition if element is weak return dummy element.
				throw new ElementNotFoundException(element);
			}
			if (list.size() > 1)
			{
				throw new ElementNotFoundException("Found " + list.size() + " elements instead 1. Element : ", element);
			}
			return list.get(0);
		}
		catch (Exception e)
		{
			logger.error(String.format("find(%s,%s)", owner, element));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public UIProxyJNA lookAtTable(UIProxyJNA table, Locator additional, Locator header, int x, int y) throws Exception
	{
		throw new Exception("This method not needed on windows plugin");
	}

	@Override
	public boolean tableIsContainer()
	{
		return false;
	}

	@Override
	public boolean mouse(UIProxyJNA component, int x, int y, MouseAction action) throws Exception
	{
		try
		{
			this.driver.mouse(component, action, x, y);
			return true;
		}
		catch (Exception e)
		{
			logger.error(String.format("mouse(%s,%d,%d,%s)", component, x, y, action));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean press(UIProxyJNA component, Keyboard key) throws Exception
	{
		try
		{
			this.driver.sendKeys(key.name());
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("press(%s,%s)", component, key));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean upAndDown(UIProxyJNA component, Keyboard key, boolean b) throws Exception
	{
		//TODO need release
		return false;
	}

	@Override
	public boolean push(UIProxyJNA component) throws Exception
	{
		try
		{
			this.driver.doPatternCall(component.getIdString(), WindowPattern.InvokePattern.getId(), "Invoke", null, -1);
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("push(%s)", component));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean toggle(UIProxyJNA component, boolean value) throws Exception
	{
		try
		{
			String className = this.driver.getProperty(component.getIdString(), WindowProperty.ClassNameProperty.getId());
			if (className.equalsIgnoreCase(ControlKind.ToggleButton.getClazz()) || className.equals(ControlKind.CheckBox
					.getClazz()))
			{
				String property = this.driver.getProperty(component.getIdString(), WindowProperty.ToggleStateProperty.getId());
				boolean isSelected = property.equals("On");
				if (value ^ isSelected)
				{
					this.driver.doPatternCall(component.getIdString(), WindowPattern.TogglePattern.getId(), "Toggle", null, -1);
				}
			}
			else if (className.equalsIgnoreCase(ControlKind.RadioButton.getClazz()))
			{
				String property = this.driver.getProperty(component.getIdString(), WindowProperty.IsSelectedProperty.getId());
				boolean isSelected = Boolean.parseBoolean(property);
				if (value ^ isSelected)
				{
					this.driver.doPatternCall(component.getIdString(), WindowPattern.SelectionItemPattern.getId(), "Select", null, -1);
				}
			}
			else
			{
				return false;
			}
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("toggle(%s,%b)", component, value));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean select(UIProxyJNA component, String selectedText) throws Exception
	{
		try
		{
			int length = 100;
			int[] arr = new int[length];
			int count = this.driver.findAll(arr, length, component.getIdString(), WindowTreeScope.Descendants.getValue(), WindowProperty.NameProperty
					.getId(), selectedText);
			if (count > length)
			{
				length = count;
				arr = new int[length];
				this.driver.findAll(arr, length, component.getIdString(), WindowTreeScope.Descendants.getValue(), WindowProperty.NameProperty
						.getId(), selectedText);
			}
			int foundElementCount = arr[0];
			if (foundElementCount > 1)
			{
				throw new Exception("Inside current component found " + foundElementCount + " elements with name " + selectedText);
			}
			this.logger.info("Getting array : " + Arrays.toString(arr));
			int itemLength = arr[1];
			int[] itemId = new int[itemLength];
			System.arraycopy(arr, 2, itemId, 0, itemLength);
			this.logger.info("Element id array : " + Arrays.toString(itemId));
			this.driver.doPatternCall(new UIProxyJNA(itemId).getIdString(), WindowPattern.SelectionItemPattern.getId(), "Select", null, -1);
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("select(%s,%s)", component, selectedText));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean fold(UIProxyJNA component, String path, boolean collaps) throws Exception
	{
		try
		{
			//TODO call to menu and tree - doPatternCall
			if (collaps)
			{

			}
			else
			{

			}
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("fold(%s,%s,%b)", component, path, collaps));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean text(UIProxyJNA component, String text, boolean clear) throws Exception
	{
		try
		{
			String oldText = "";
			if (!clear)
			{
				oldText = this.driver.getProperty(component.getIdString(), WindowProperty.ValueProperty.getId());
			}
			this.driver.doPatternCall(component.getIdString(), WindowPattern.ValuePattern.getId(), "SetValue", oldText + text, 0);
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("text(%s, %s, %s)", component, text, clear));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean wait(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong) throws Exception
	{
		long begin = System.currentTimeMillis();
		try
		{
			long time = System.currentTimeMillis();
			while (System.currentTimeMillis() < time + ms)
			{
				try
				{
					List<UIProxyJNA> elements = this.findAll(null, locator);
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
					this.logger.error("Error on waiting");
					this.logger.error(e.getMessage(), e);
				}
			}
			return false;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("wait(%s,%d,%b)", locator, ms, toAppear));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			atomicLong.set(System.currentTimeMillis() - begin);
		}
	}

	@Override
	public boolean setValue(UIProxyJNA component, double value) throws Exception
	{
		try
		{
			this.driver.doPatternCall(component.getIdString(), WindowPattern.RangeValuePattern.getId(), "SetValue", "" + value, 2);
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("setValue(%s,%e)", component, value));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String getValue(UIProxyJNA component) throws Exception
	{
		try
		{
			String result = this.driver.getProperty(component.getIdString(), WindowProperty.ValueProperty.getId());
			if (Str.IsNullOrEmpty(result))
			{
				result = this.driver.getProperty(component.getIdString(), WindowProperty.NameProperty.getId());
			}
			return result;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getValue(%s)", component));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String get(UIProxyJNA component) throws Exception
	{
		try
		{
			return this.driver.getProperty(component.getIdString(), WindowProperty.NameProperty.getId());
		}
		catch (Exception e)
		{
			this.logger.error(String.format("get(%s)", component));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String getAttr(UIProxyJNA component, String name) throws Exception
	{
		try
		{
			if (!AttributeKind.isSupported(name))
			{
				throw new RemoteException("Unsupported attribute value. Can use only : " + Arrays.toString(AttributeKind.values()));
			}
			return this.driver.elementAttribute(component, AttributeKind.valueOf(name.toUpperCase()));
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getAttr(%s,%s)", component, name));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean mouseTable(UIProxyJNA component, int column, int row, MouseAction action) throws Exception
	{
		try
		{
			List<UIProxyJNA> rows = getRows(component);
			UIProxyJNA currentRow = rows.get(row);
			List<UIProxyJNA> cells = this.getCells(currentRow);
			UIProxyJNA cell = cells.get(column);
			this.driver.mouse(cell, action, Integer.MIN_VALUE, Integer.MAX_VALUE);
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("mouseTable(%s,%d,%d,%s)", component, column, row, action));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean textTableCell(UIProxyJNA component, int column, int row, String text) throws Exception
	{
		try
		{
			List<UIProxyJNA> rows = getRows(component);
			UIProxyJNA currentRow = rows.get(row);
			List<UIProxyJNA> cells = this.getCells(currentRow);
			UIProxyJNA cell = cells.get(column);
			this.driver.doPatternCall(cell.getIdString(), WindowPattern.ValuePattern.getId(), "SetValue", text, 0);
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("textTableCell(%s,%d,%d,%s)", component, column, row, text));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String getValueTableCell(UIProxyJNA component, int column, int row) throws Exception
	{
		try
		{
			List<UIProxyJNA> rows = getRows(component);
			UIProxyJNA needRow = rows.get(row);
			List<String> needRows = getRow(needRow, false);
			return needRows.get(column);
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getValueTableCell(%s, %d, %d)", component, column, row));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, String> getRow(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		try
		{
			List<String> rowIndexes = getRowIndexes(component, additional, header, useNumericHeader, valueCondition, colorCondition);
			if (rowIndexes.size() != 1)
			{
				throw new RemoteException("Found " + rowIndexes.size() + " instead 1");
			}
			return getRowByIndex(component, additional, header, useNumericHeader, Integer.parseInt(rowIndexes.get(0)));
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRow(%s,%s,%s,%b,%s,%s)", component, additional, header, useNumericHeader, valueCondition, colorCondition));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<String> getRowIndexes(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		try
		{
			List<String> returnedList = new ArrayList<>();
			List<UIProxyJNA> rows = getRows(component);
			for (int i = 1; i < rows.size(); i++)
			{
				if (rowMatches(rows.get(i), valueCondition, colorCondition, rows.get(0), useNumericHeader))
				{
					returnedList.add(String.valueOf(i));
				}
			}
			return returnedList;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRowIndexes(%s,%s,%s,%b,%s,%s)", component, additional, header, useNumericHeader, valueCondition, colorCondition));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, String> getRowByIndex(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		try
		{
			List<UIProxyJNA> rows = getRows(component);
			Map<String, String> resultMap = new HashMap<>();
			UIProxyJNA headerRow = rows.get(0);
			List<String> headers = getRow(headerRow, useNumericHeader);

			// +1 because the first row - is header;
			UIProxyJNA needRow = rows.get(i + 1);
			List<String> row = getRow(needRow, false);
			for (int j = 0; j < headers.size(); j++)
			{
				resultMap.put(headers.get(j), row.get(j));
			}
			return resultMap;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRowByIndex(%s,%s,%s,%b,%d)", component, additional, header, useNumericHeader, i));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, ValueAndColor> getRowWithColor(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		return null;
	}

	@Override
	public String[][] getTable(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		try
		{
			List<UIProxyJNA> rows = getRows(component);
			List<String> headerRow = getRow(rows.get(0), useNumericHeader);
			String[][] table = new String[rows.size()][headerRow.size()];
			for (int i = 0; i < headerRow.size(); i++)
			{
				table[0][i] = headerRow.get(i);
			}
			for (int i = 1; i < rows.size(); i++)
			{
				List<String> row = getRow(rows.get(i), false);
				for (int j = 0; j < row.size(); j++)
				{
					table[i][j] = row.get(j);
				}
			}
			return table;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getTable(%s,%s,%s,%b)", component, additional, header, useNumericHeader));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public Locator locatorFromUIProxy(UIProxyJNA element) throws Exception
	{
		try
		{
			String id = this.driver.elementAttribute(element, AttributeKind.ID);
			String uid = this.driver.elementAttribute(element, AttributeKind.UID);
			String clazz = this.driver.elementAttribute(element, AttributeKind.CLASS);
			String name = this.driver.elementAttribute(element, AttributeKind.NAME);
			String tooltip = this.driver.elementAttribute(element, AttributeKind.TEXT);
			Locator locator = new Locator(null, id, ControlKind.findByClazz(clazz));
			locator.uid(uid).clazz(clazz).name(name).tooltip(tooltip);
			return locator;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("locatorFromUIProxy(%s)", element));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public Locator locatorFromUIProxy(int[] id) throws Exception
	{
		return locatorFromUIProxy(new UIProxyJNA(id));
	}

	private List<UIProxyJNA> getRows(UIProxyJNA table) throws Exception
	{
		try
		{
			int length = 100;
			int[] arr = new int[length];
			int res = this.driver.findAll(arr, length, table.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.ClassNameProperty.getId(), "");
			if (res > length)
			{
				length = res;
				arr = new int[length];
				this.driver.findAll(arr, length, table.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.ClassNameProperty.getId(), "");
			}
			int foundElementCount = arr[0];
			List<UIProxyJNA> rowsList = new ArrayList<>();
			int currentPosition = 1;
			for (int i = 0; i < foundElementCount; i++)
			{
				int currentArrayLength = arr[currentPosition++];
				int[] elem = new int[currentArrayLength];
				for (int j = 0; j < currentArrayLength; j++)
				{
					elem[j] = arr[currentPosition++];
				}
				rowsList.add(new UIProxyJNA(elem));
			}
			return rowsList;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRows(%s)", table));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private List<String> getRow(UIProxyJNA row, boolean useNumericHeader) throws Exception
	{
		try
		{
			int length = 100;
			int[] arr = new int[length];
			int res = this.driver.findAll(arr, length, row.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.ClassNameProperty.getId(), "");
			if (res > length)
			{
				length = res;
				arr = new int[length];
				this.driver.findAll(arr, length, row.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.ClassNameProperty.getId(), "");
			}

			int foundElementCount = arr[0];
			List<UIProxyJNA> cellsList = new ArrayList<>();
			int currentPosition = 1;
			for (int i = 0; i < foundElementCount; i++)
			{
				int currentArrayLength = arr[currentPosition++];
				int[] elem = new int[currentArrayLength];
				for (int j = 0; j < currentArrayLength; j++)
				{
					elem[j] = arr[currentPosition++];
				}
				cellsList.add(new UIProxyJNA(elem));
			}
			ArrayList<String> returnedList = new ArrayList<>();
			for (int i = 0; i < cellsList.size(); i++)
			{
				try
				{
					returnedList.add(useNumericHeader ? String.valueOf(i) : this.driver.getProperty(cellsList.get(i).getIdString(), WindowProperty.ValueProperty.getId()));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			return returnedList;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRow(%s,%b)", row, useNumericHeader));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private List<UIProxyJNA> getCells(UIProxyJNA row) throws Exception
	{
		try
		{
			int length = 100;
			int[] arr = new int[length];
			int res = this.driver.findAll(arr, length, row.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.ClassNameProperty.getId(), "");
			if (res > length)
			{
				length = res;
				arr = new int[length];
				this.driver.findAll(arr, length, row.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.ClassNameProperty.getId(), "");
			}

			int foundElementCount = arr[0];
			List<UIProxyJNA> cellsList = new ArrayList<>();
			int currentPosition = 1;
			for (int i = 0; i < foundElementCount; i++)
			{
				int currentArrayLength = arr[currentPosition++];
				int[] elem = new int[currentArrayLength];
				for (int j = 0; j < currentArrayLength; j++)
				{
					elem[j] = arr[currentPosition++];
				}
				cellsList.add(new UIProxyJNA(elem));
			}
			return cellsList;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getCells(%s)", row));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private boolean rowMatches(UIProxyJNA row, ICondition valueCondition, ICondition colorCondition, UIProxyJNA header, boolean useNumericHeader) throws Exception
	{
		try
		{
			List<String> headerCells = getRow(header, useNumericHeader);
			List<String> cells = getRow(row, false);
			for (int i = 0; i < cells.size(); i++)
			{
				if (valueCondition != null)
				{
					String name = headerCells.get(i);
					if (!valueCondition.isMatched(name, cells.get(i)))
					{
						return false;
					}
				}
				if (colorCondition != null)
				{
					//TODO do it pls
				}
			}
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("rowMatches(%s,%s,%s,%s,%b)", row, valueCondition, colorCondition, header, useNumericHeader));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private List<String> getHeaders(UIProxyJNA table, boolean useNumericHeader) throws Exception
	{
		try
		{
			//TODO this code work only for framework WindowsForms. Need implement for Wpf applications
			int[] topRowRuntimeId = findTopRow(table);
			UIProxyJNA topRow = new UIProxyJNA(topRowRuntimeId);
			int length = 100;
			int[] arr = new int[length];
			String headerName = "header";
			int res = this.driver.findAll(arr, length, topRow.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.LocalizedControlTypeProperty
					.getId(), headerName);
			if (res > length)
			{
				length = res;
				arr = new int[length];
				this.driver.findAll(arr, length, topRow.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.LocalizedControlTypeProperty
						.getId(), headerName);
			}
			int foundElementCount = arr[0];
			List<UIProxyJNA> headerList = new ArrayList<>();
			int currentPosition = 1;
			for (int i = 0; i < foundElementCount; i++)
			{
				int currentArrayLength = arr[currentPosition++];
				int[] elem = new int[currentArrayLength];
				for (int j = 0; j < currentArrayLength; j++)
				{
					elem[j] = arr[currentPosition++];
				}
				headerList.add(new UIProxyJNA(elem));
			}
			ArrayList<String> returnedList = new ArrayList<>();
			for (int i = 0; i < headerList.size(); i++)
			{
				returnedList.add(useNumericHeader ? "" + i : this.driver.getProperty(headerList.get(i).getIdString(), WindowProperty.NameProperty.getId()));
			}
			return returnedList;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getHeaders(%s,%b)", table, useNumericHeader));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private int[] findTopRow(UIProxyJNA table) throws Exception
	{
		try
		{
			int length = 100;
			int[] arr = new int[length];

			String headName = "Top Row";
			int res = this.driver.findAll(arr, length, table.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.NameProperty
					.getId(), headName);
			if (res > length)
			{
				length = res;
				arr = new int[length];
				this.driver.findAll(arr, length, table.getIdString(), WindowTreeScope.Children.getValue(), WindowProperty.NameProperty
						.getId(), headName);
			}
			int foundElementCount = arr[0];
			if (foundElementCount > 1)
			{
				throw new Exception("Found " + foundElementCount + " headers instead 1");
			}
			int[] headerRuntimeId = new int[arr[1]];
			System.arraycopy(arr, 2, headerRuntimeId, 0, arr[1]);
			return headerRuntimeId;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("findTopRow(%s)", table));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}
}
