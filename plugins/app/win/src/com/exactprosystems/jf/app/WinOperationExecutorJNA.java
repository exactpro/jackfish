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
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.OperationNotAllowedException;
import com.exactprosystems.jf.api.error.app.WrongParameterException;
import org.apache.log4j.Logger;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WinOperationExecutorJNA implements OperationExecutor<UIProxyJNA>
{
	static final String RECTANGLE_PATTERN = "(-?\\d+),(-?\\d+),(\\d+),(\\d+)";
	private static final String SEPARATOR_CELL = "###";
	private static final String SEPARATOR_ROWS = ";;;";
	private static final String SEPARATOR_COMMA = ",";
	private static final String EMPTY_CELL = "EMPTY_CELL_EMPTY";
	private static final String EMPTY_HEADER_CELL = "EMPTY_HEADER_CELL_EMPTY";

	private Logger logger;
	private JnaDriverImpl driver;
	private PluginInfo info;

	public WinOperationExecutorJNA(Logger logger, JnaDriverImpl driver)
	{
		this.logger = logger;
		this.driver = driver;
	}

    @Override
    public void setPluginInfo(PluginInfo info)
    {
        this.info = info;
    }

	@Override
	public Rectangle getRectangle(UIProxyJNA component) throws Exception
	{
		try
		{
			String property = this.driver.getProperty(component, WindowProperty.BoundingRectangleProperty);
			if (property.equalsIgnoreCase("Empty"))
			{
				return new Rectangle(0,0,0,0);
			}
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
				throw new RemoteException("returned rectangle not matches pattern " + RECTANGLE_PATTERN+" , rect : " + property);
			}
			return rectangle;
		}
		catch (RemoteException e)
		{
			throw e;
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
			boolean many = locator.getAddition() != null && locator.getAddition() == Addition.Many;
			UIProxyJNA owner = window == null ? new UIProxyJNA(null) : window;
			int count = this.driver.findAllForLocator(result, owner, controlKind, locator.getUid(),	locator.getXpath(),	locator.getClazz(),	locator.getName(), locator.getTitle(), locator.getText(), many);
			if (count > length)
			{
				length = count;
				result = new int[length];
				this.driver.findAllForLocator(result, owner, locator.getControlKind(), locator.getUid(), locator.getXpath(), locator.getClazz(), locator.getName(), locator.getTitle(), locator.getText(), many);
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
		catch (RemoteException e)
		{
			throw e;
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
			boolean many = element.getAddition() != null && element.getAddition() == Addition.Many;
			int length = 100;
			int[] result = new int[length];
			int count = this.driver.findAllForLocator(result, ownerElement, element.getControlKind(), element.getUid(),
					element.getXpath(), element.getClazz(), element.getName(), element.getTitle(), element.getText(), many);
			if (count > length)
			{
				length = count;
				result = new int[length];
				this.driver.findAllForLocator(result, ownerElement, element.getControlKind(), element.getUid(),
						element.getXpath(), element.getClazz(), element.getName(), element.getTitle(), element.getText(), many);
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
		catch (RemoteException e)
		{
			throw e;
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
				if (element.isWeak())
				{
					return UIProxyJNA.DUMMY;
				}
				throw new ElementNotFoundException(element);
			}
			if (list.size() > 1)
			{
				throw new ElementNotFoundException("Found " + list.size() + " elements instead 1. Element : ", element);
			}
			return list.get(0);
		}
		catch (RemoteException e)
		{
			throw e;
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
		throw new FeatureNotSupportedException("lookAtTable");
	}

	@Override
	public boolean elementIsEnabled(UIProxyJNA component) throws Exception
	{
		return this.driver.elementIsEnabled(component);
	}

    @Override
    public boolean elementIsVisible(UIProxyJNA component) throws Exception
    {
        return this.driver.elementIsVisible(component);
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
		catch (RemoteException e)
		{
			throw e;
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
		catch (RemoteException e)
		{
			throw e;
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
		try
		{
			this.driver.upAndDown(key.name(), b);
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("push(%s)", component));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean push(UIProxyJNA component) throws Exception
	{
		try
		{
			this.driver.doPatternCall(component, WindowPattern.InvokePattern, "Invoke", null, -1);
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
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
            int length = 100;
            int[] arr = new int[length];
            int count = this.driver.getPatterns(arr, component);
            if (count > length)
            {
                length = count;
                arr = new int[length];
                count = this.driver.getPatterns(arr, component);
            }

            int patternsCount = count;
            int[] patterns = new int[patternsCount];
            System.arraycopy(arr, 0, patterns, 0, patternsCount);

            List<WindowPattern> collect = Arrays.stream(patterns)
                    .mapToObj(WindowPattern::byId)
                    .collect(Collectors.toList());

            if (collect.contains(WindowPattern.TogglePattern))
            {
                String property = this.driver.getProperty(component, WindowProperty.ToggleStateProperty);
                boolean isSelected = property.equals("On");
                if (value ^ isSelected)
                {
                    this.driver.doPatternCall(component, WindowPattern.TogglePattern, "Toggle", null, -1);
                }
            }
            else if (collect.contains(WindowPattern.SelectionItemPattern))
            {
                String property = this.driver.getProperty(component, WindowProperty.IsSelectedProperty);
                boolean isSelected = Boolean.parseBoolean(property);
                if (value ^ isSelected)
                {
                    this.driver.doPatternCall(component, WindowPattern.SelectionItemPattern, "Select", null, -1);
                }
            }
            else
            {
                return false;
            }
            return true;
        }
        catch (RemoteException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            this.logger.error(String.format("toggle(%s,%b)", component, value));
            this.logger.error(e.getMessage(), e);
            throw e;
        }
    }

	@Override
	public boolean selectByIndex(UIProxyJNA component, int index) throws Exception
	{
		throw new OperationNotAllowedException("selectByIndex()"); // TODO need to implement
	}
		
		
	@Override
	public boolean select(UIProxyJNA component, String selectedText) throws Exception
	{
		try
		{
			WindowTreeScope treeScope = WindowTreeScope.Descendants;
			if (this.driver.elementAttribute(component, AttributeKind.TYPE_NAME).toLowerCase().contains("tab"))
			{
				treeScope = WindowTreeScope.Children;
			}
			int length = 100;
			int[] arr = new int[length];
			int count = this.driver.findAll(arr, component, treeScope, WindowProperty.NameProperty, selectedText);
			if (count > length)
			{
				length = count;
				arr = new int[length];
				this.driver.findAll(arr, component, treeScope, WindowProperty.NameProperty, selectedText);
			}
			int foundElementCount = arr[0];
			if (foundElementCount > 1)
			{
				throw new WrongParameterException("Inside current component found " + foundElementCount + " elements with name " + selectedText);
			}
			this.logger.info("Getting array : " + Arrays.toString(arr));
			int itemLength = arr[1];
			int[] itemId = new int[itemLength];
			System.arraycopy(arr, 2, itemId, 0, itemLength);
			this.logger.info("Element id array : " + Arrays.toString(itemId));
			this.driver.doPatternCall(new UIProxyJNA(itemId), WindowPattern.SelectionItemPattern, "Select", null, -1);
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("select(%s,%s)", component, selectedText));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean expand(UIProxyJNA component, String path, boolean expandOrCollapse) throws Exception
	{
		try
		{
			//TODO call to menu and tree - doPatternCall 
			if (expandOrCollapse)
			{

			}
			else
			{

			}
			return true;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("fold(%s,%s,%b)", component, path, expandOrCollapse));
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
				oldText = this.driver.getProperty(component, WindowProperty.ValueProperty);
			}
//			this.driver.doPatternCall(component, WindowPattern.ValuePattern, "SetValue", oldText + text, 0);
			this.driver.setText(component, oldText + text);
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
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
			this.logger.debug("Locator : " + locator);
			long time = System.currentTimeMillis();
			while (System.currentTimeMillis() < time + ms)
			{
				try
				{
					this.driver.clearCache();
					List<UIProxyJNA> elements = this.findAll(null, locator);
					this.logger.debug("Found : " + elements.size() + " elements on method wait. Expected : " + (toAppear ? ">0" : "0") );
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
			this.driver.doPatternCall(component, WindowPattern.RangeValuePattern, "SetValue", "" + value, 2);
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("setValue(%s,%e)", component, value));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<String> getList(UIProxyJNA component) throws Exception
    {
        try
        {
            String result = this.driver.getList(component);
			if (result.isEmpty()) {
				return new ArrayList<>();
			}
			return Arrays.asList(result.split(SEPARATOR_COMMA));
        }
        catch (RemoteException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            this.logger.error(String.format("getRowIndexes(%s)", component));
            this.logger.error(e.getMessage(), e);
            throw e;
        }
	}

	@Override
	public String getValue(UIProxyJNA component) throws Exception
	{
		try
		{
			int length = 100;
			int[] arr = new int[length];
			int res = this.driver.getPatterns(arr, component);
			if (res > length)
			{
				length = res;
				arr = new int[length];
				this.driver.getPatterns(arr, component);
			}
			boolean isSelectionPatternPresent = false;
			boolean isSelectionItemPatternPresent = false;
			boolean isTogglePattern = false;
			boolean isTextPattern = false;
			boolean isRangeValuePattern = false;

			for (int p : arr)
			{
				if (WindowPattern.TogglePattern.getId() == p)
				{
					isTogglePattern = true;
				}
				if (WindowPattern.SelectionItemPattern.getId() == p)
				{
					isSelectionItemPatternPresent = true;
				}
				if (WindowPattern.SelectionPattern.getId() == p)
				{
					isSelectionPatternPresent = true;
				}
				if (WindowPattern.TextPattern.getId() == p)
				{
					isTextPattern = true;
				}
				if (WindowPattern.RangeValuePattern.getId() == p)
				{
					isRangeValuePattern = true;
				}
			}
			String result;
			if (isSelectionPatternPresent)
			{
				result = this.driver.getProperty(component, WindowProperty.SelectionProperty);
			}
			else if (isSelectionItemPatternPresent)
			{
				result = this.driver.getProperty(component, WindowProperty.IsSelectedProperty);
			}
			else if (isTogglePattern)
			{
				result = this.driver.getProperty(component, WindowProperty.ToggleStateProperty);
			}
			else if (isTextPattern)
			{
				result = this.driver.getProperty(component, WindowProperty.IsTextPatternAvailableProperty);
			}
			else if (isRangeValuePattern)
			{
				result = this.driver.getProperty(component, WindowProperty.IsRangeValuePatternAvailableProperty);
			}
			else {
				result = this.driver.getProperty(component, WindowProperty.ValueProperty);
				if (Str.IsNullOrEmpty(result)) {
					result = this.driver.getProperty(component, WindowProperty.NameProperty);
				}
			}

			return result;
		}
		catch (RemoteException e)
		{
			throw e;
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
			int length = 100;
			int[] arr = new int[length];
			int res = this.driver.getPatterns(arr, component);
			if (res > length)
			{
				length = res;
				arr = new int[length];
				this.driver.getPatterns(arr, component);
			}
			boolean isSelectionPatternPresent = false;
			boolean isSelectionItemPatternPresent = false;
			boolean isTogglePattern = false;
			boolean isTextPattern = false;
			boolean isRangeValuePattern = false;

			for (int p : arr)
			{
				if (WindowPattern.TogglePattern.getId() == p)
				{
					isTogglePattern = true;
				}
				if (WindowPattern.SelectionItemPattern.getId() == p)
				{
					isSelectionItemPatternPresent = true;
				}
				if (WindowPattern.SelectionPattern.getId() == p)
				{
					isSelectionPatternPresent = true;
				}
				if (WindowPattern.TextPattern.getId() == p)
				{
					isTextPattern = true;
				}
				if (WindowPattern.RangeValuePattern.getId() == p)
				{
					isRangeValuePattern = true;
				}
			}
			String result;
			if (isSelectionPatternPresent)
			{
				result = this.driver.getProperty(component, WindowProperty.SelectionProperty);
			}
			else if (isTogglePattern)
			{
				result = this.driver.getProperty(component, WindowProperty.NameProperty);
			}
			else if (isTextPattern)
			{
				result = this.driver.getProperty(component, WindowProperty.IsTextPatternAvailableProperty);
			}
			else if (isRangeValuePattern)
			{
				result = this.driver.getProperty(component, WindowProperty.IsRangeValuePatternAvailableProperty);
			}
			else {
				result = this.driver.getProperty(component, WindowProperty.ValueProperty);
				if (Str.IsNullOrEmpty(result)) {
					result = this.driver.getProperty(component, WindowProperty.NameProperty);
				}
			}

			return result;
		}
		catch (RemoteException e)
		{
			throw e;
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
				throw new OperationNotAllowedException("Unsupported attribute value. Can use only : " + Arrays.toString(AttributeKind.values()));
			}

			AttributeKind kind = AttributeKind.valueOf(name.toUpperCase());

			if(kind == AttributeKind.ITEMS)
			{
				Rectangle rect = getRectangle(component);
				int[] xy = {rect.x + 20, rect.y + rect.height + 1};
				return this.driver.elementAttribute(new UIProxyJNA(xy), kind);
			}

			return this.driver.elementAttribute(component, kind);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getAttr(%s,%s)", component, name));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String script(UIProxyJNA component, String script) throws Exception
	{
		throw new FeatureNotSupportedException("script");
	}

	private boolean isCoordsDidNotIntroduce(int x, int y)
	{
		return (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE);
	}

	private Point getCenterOf(Rectangle rectangle)
	{
		int x = rectangle.x + rectangle.width / 2;
		int y = rectangle.y + rectangle.height / 2;
		return new Point(x, y);
	}

	@Override
	public boolean dragNdrop(UIProxyJNA drag, int x1, int y1, UIProxyJNA drop, int x2, int y2, boolean moveCursor) throws Exception
	{
		try
		{
			Rectangle dragRect = this.getRectangle(drag);
			if (isCoordsDidNotIntroduce(x1,y1))
			{
				Point point = getCenterOf(dragRect);
				x1 = point.x;
				y1 = point.y;
			}
			else
			{
				x1 += dragRect.x;
				y1 += dragRect.y;
			}

			if (drop == null)
			{
				x2 += x1;
				y2 += y1;
			}
			else
			{
				Rectangle rDrop = this.getRectangle(drop);
				x2 += rDrop.x;
				y2 += rDrop.y;
			}

			this.driver.dragNdrop(x1, y1, x2, y2);
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("dragNdrop(%s,%s,%d,%d,%d,%d)", drag, drop, x1, y1, x2, y2));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean mouseTable(UIProxyJNA component, int column, int row, MouseAction action) throws Exception
	{
		try
		{
			this.driver.mouseTableCell(component, column, row, action);
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
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
			this.driver.textTableCell(component, column, row, text);
			return true;
		}
		catch (RemoteException e)
		{
			throw e;
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
			return this.driver.getValueTableCell(component, column, row);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getValueTableCell(%s, %d, %d)", component, column, row));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, String> getRow(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		try
		{
			String result = this.driver.getRowByConditions(component, useNumericHeader, (Condition) valueCondition, columnsToString(columns));
			String[] split = result.split(SEPARATOR_ROWS);
			if (split.length < 2)
			{
				throw new ElementNotFoundException("Row is not found");
			}
			String headerRow = split[0];
			String row = split[1];
			Map<String, String> map = new LinkedHashMap<>();
			String[] headerCells = headerRow.split(SEPARATOR_CELL);
			String[] rowCell = row.split(SEPARATOR_CELL);
			List<String> newHeaders = Converter.convertColumns(Arrays.asList(headerCells));
			for (int i = 0; i < newHeaders.size(); i++)
			{
				String value = rowCell[i];
				map.put(headerCells[i], value.equals(EMPTY_CELL) ? "" : value);
			}
			return map;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRow(%s,%s,%s,%b,%s,%s)", component, additional, header, useNumericHeader, valueCondition, colorCondition));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<String> getRowIndexes(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		try
		{
			String result = this.driver.getRowIndexes(component, useNumericHeader, valueCondition, columnsToString(columns));
			if (result.isEmpty()) {
				return new ArrayList<>();
			}
			List<String> returnedList = new ArrayList<>();
			String[] indexes = result.split(SEPARATOR_CELL);
			Collections.addAll(returnedList, indexes);
			return returnedList;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRowIndexes(%s,%s,%s,%b,%s,%s)", component, additional, header, useNumericHeader, valueCondition, colorCondition));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, String> getRowByIndex(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		try
		{
			String result = this.driver.getRowByIndex(component, useNumericHeader, i);
			String[] split = result.split(SEPARATOR_ROWS);
			String headerRow = split[0];
			String row = split[1];
			Map<String, String> map = new LinkedHashMap<>();
			String[] headerCells = convertColumns(headerRow.split(SEPARATOR_CELL), columns);
			String[] rowCell = row.split(SEPARATOR_CELL);
			List<String> newColumns = Converter.convertColumns(Arrays.asList(headerCells));
			for (int j = 0; j < newColumns.size(); j++)
			{
				String value = rowCell[j];
				map.put(headerCells[j], value.equals(EMPTY_CELL) ? "" : value);
			}
			return map;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRowByIndex(%s,%s,%s,%b,%d)", component, additional, header, useNumericHeader, i));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String, ValueAndColor> getRowWithColor(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		return null; // TODO WTF?
	}

	@Override
	public String[][] getTable(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns) throws Exception
	{
		try
		{
			String res = this.driver.getTable(component, useNumericHeader);
			String[] split = res.split(SEPARATOR_ROWS);
			String headerRow = split[0];
			String[] headerCells = convertColumns(headerRow.split(SEPARATOR_CELL), columns);
			List<String> newHeader = Converter.convertColumns(Arrays.asList(headerCells));
			String[][] table = new String[split.length][newHeader.size()];
			System.arraycopy(newHeader.toArray(new String[newHeader.size()]), 0, table[0], 0, newHeader.size());
			for (int i = 1; i < split.length; i++)
			{
				String row = split[i];
				String[] rowCells = row.split(SEPARATOR_CELL);
				for (int j = 0; j < rowCells.length; j++)
				{
					if (rowCells[j].equals(EMPTY_CELL))
					{
						rowCells[j] = "";
					}
				}
				System.arraycopy(rowCells, 0, table[i], 0, Math.min(rowCells.length, newHeader.size()));
			}
			return table;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getTable(%s,%s,%s,%b)", component, additional, header, useNumericHeader));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public int getTableSize(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		try
		{
			return this.driver.getTableSize(component);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getTableSize(%s,%s,%s,%b)", component, additional, header, useNumericHeader));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

    @Override
    public Color getColorXY(UIProxyJNA component, int x, int y) throws Exception
    {
		Rectangle rectangle = getRectangle(component);
		return new Robot().getPixelColor(rectangle.x + x, rectangle.y + y);
    }

    private String columnsToString(String[] a)
	{
		if (a == null || a.length == 0)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (String s : a)
		{
			sb.append(sep).append(s);
			sep = SEPARATOR_CELL;
		}
		return sb.toString();
	}

	private String[] convertColumns(String[] arrayFromGui, String[] columns)
	{
		ArrayList<String> res = new ArrayList<>();
		if (columns == null)
		{
			return arrayFromGui;
		}
		Iterator<String> iterator = Arrays.asList(arrayFromGui).iterator();
		int i;
		for (i = 0; i < columns.length; i++)
		{
			if (iterator.hasNext())
			{
				iterator.next();
				res.add(columns[i]);
			}
			else
			{
				break;
			}
		}
		while (iterator.hasNext())
		{
			iterator.next();
			res.add(String.valueOf(i++));
		}

		return res.toArray(new String[res.size()]);
	}
}
