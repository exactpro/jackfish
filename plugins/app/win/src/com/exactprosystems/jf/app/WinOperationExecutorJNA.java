////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.app.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.awt.*;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WinOperationExecutorJNA extends AbstractOperationExecutor<UIProxyJNA>
{
	private static final String RECTANGLE_PATTERN = "(-?\\d+)[,;](-?\\d+)[,;](\\d+)[,;](\\d+)";

	private static final String SEPARATOR_CELL = "###";
	private static final String SEPARATOR_ROWS = ";;;";
	private static final String SEPARATOR_COMMA = ",";
	private static final String EMPTY_CELL = "EMPTY_CELL_EMPTY";
	private static final String EMPTY_HEADER_CELL = "EMPTY_HEADER_CELL_EMPTY";
	private static final String RUNTIME_ID_ATTRIBUTE = "runtimeId";
	private static final String STATE = "state";

	private Logger logger;
	private JnaDriverImpl driver;

	public WinOperationExecutorJNA(Logger logger, JnaDriverImpl driver, boolean useTrimText)
	{
		super(useTrimText);
		this.logger = logger;
		this.driver = driver;
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
			return stringToRect(property);
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
	public Color getColor(UIProxyJNA component, boolean isForeground) throws Exception
	{
		return null;
	}

	@Override
	public List<UIProxyJNA> findAll(ControlKind controlKind, UIProxyJNA window, Locator locator) throws Exception
	{
		try
		{
			return this.driver.findAllForLocator(window, controlKind, locator);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("findAll(%s,%s,%s)", controlKind, window, locator));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public List<UIProxyJNA> findAll(Locator owner, Locator element) throws Exception
	{
		try
		{
			UIProxyJNA ownerElement = new UIProxyJNA();
			//find owner if present
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

			return this.driver.findAllForLocator(ownerElement, element.getControlKind(), element);
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
	public List<UIProxyJNA> findByXpath(UIProxyJNA component, String path) throws Exception
	{
		String attribute = this.driver.elementAttribute(component, AttributeKind.TYPE_NAME);
		List<UIProxyJNA> returnedList = new ArrayList<>();
		if (attribute.equalsIgnoreCase(ControlType.Tree.getName()))
		{
			NodeList nodes = findNodesInTreeByXpath(convertTreeToXMLDoc(component), path);
			for (int i = 0; i < nodes.getLength(); i++)
			{
				String runtimeId = nodes.item(i).getAttributes().getNamedItem(RUNTIME_ID_ATTRIBUTE).getNodeValue();
				returnedList.add(new UIProxyJNA(runtimeId));
			}
			return returnedList;
		}
		return Collections.emptyList();
	}

	@Override
	public UIProxyJNA lookAtTable(UIProxyJNA table, Locator additional, Locator header, int x, int y) throws Exception
	{
		throw new FeatureNotSupportedException("lookAtTable");
	}

	@Override
	public boolean elementIsEnabled(UIProxyJNA component) throws Exception
	{
		return Boolean.parseBoolean(this.driver.elementAttribute(component, AttributeKind.ENABLED));
	}

    @Override
    public boolean elementIsVisible(UIProxyJNA component) throws Exception
    {
		return Boolean.parseBoolean(this.driver.elementAttribute(component, AttributeKind.VISIBLE));
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
			this.driver.sendKeys(component, key.name());
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
			this.driver.upAndDown(component, key.name(), b);
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
			List<WindowPattern> availablePatterns = this.driver.getAvailablePatterns(component);

			if (availablePatterns.contains(WindowPattern.TogglePattern))
			{
				String property = this.driver.getProperty(component, WindowProperty.ToggleStateProperty);
				boolean isSelected = property.equals("On");
				if (value ^ isSelected)
				{
					this.driver.doPatternCall(component, WindowPattern.TogglePattern, "Toggle", null, -1);
				}
			}
			else if (availablePatterns.contains(WindowPattern.SelectionItemPattern))
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
		try
		{
			String controlTypeId;
			String attribute = this.driver.elementAttribute(component, AttributeKind.TYPE_NAME);
			if (attribute.equalsIgnoreCase(ControlType.List.getName()) ||
				attribute.equalsIgnoreCase(ControlType.ComboBox.getName()))
			{
				controlTypeId = ControlType.ListItem.getStringId();
			}
			else if (attribute.equalsIgnoreCase(ControlType.Tab.getName()))
			{
				controlTypeId = ControlType.TabItem.getStringId();
			}
			else if (attribute.equalsIgnoreCase(ControlType.Tree.getName()))
			{
				controlTypeId = ControlType.TreeItem.getStringId();
			}
			else
			{
				return true;
			}
			List<UIProxyJNA> elementsList = findComponents(component,
					WindowTreeScope.Descendants,
					WindowProperty.ControlTypeProperty,
					controlTypeId);
			this.driver.doPatternCall(elementsList.get(index), WindowPattern.SelectionItemPattern, "Select", null, -1);
			return true;
		}
		catch(WrongParameterException ignored)
		{
			return true;
		}
	}

	@Override
	public boolean select(UIProxyJNA component, String selectedText) throws Exception
	{
		try
		{
			String attribute = this.driver.elementAttribute(component, AttributeKind.TYPE_NAME);
			if (attribute.equalsIgnoreCase(ControlType.Tree.getName()))
			{
				NodeList nodes = findNodesInTreeByXpath(convertTreeToXMLDoc(component), selectedText);
				for (int i = 0; i < nodes.getLength(); i++)
				{
					UIProxyJNA element = new UIProxyJNA(nodes.item(i).getAttributes().getNamedItem(RUNTIME_ID_ATTRIBUTE).getNodeValue());
					if(checkVisible(element))
					{
						this.driver.doPatternCall(element, WindowPattern.SelectionItemPattern, "Select", null, -1);
					}
				}
			}
			else
			{
				UIProxyJNA element = new UIProxyJNA(findItem(component, selectedText));
				if(checkVisible(element))
				{
					this.driver.doPatternCall(element, WindowPattern.SelectionItemPattern, "Select", null, -1);
				}
			}
			return true;
		}
		catch (WrongParameterException ignore)
		{
			return true;
		}
	}

	@Override
	public boolean expand(UIProxyJNA component, String path, boolean expandOrCollapse) throws Exception
	{
		try
		{
			String attribute = this.driver.elementAttribute(component, AttributeKind.TYPE_NAME);
			if(attribute.equalsIgnoreCase(ControlType.Tree.getName()))
			{
				boolean doNext;
				do
				{
					doNext = false;
					NodeList nodes = findNodesInTreeByXpath(convertTreeToXMLDoc(component), path);
					if (nodes.getLength() == 0)
					{
						throw new WrongParameterException("Path '" + path + "' is not found in the tree.");
					}

					for (int i = nodes.getLength() - 1; i >= 0; i--)
					{
						Node stateAttr = nodes.item(i).getAttributes().getNamedItem(STATE);
						if(stateAttr != null)
						{
							String runtimeId = nodes.item(i).getAttributes().getNamedItem(RUNTIME_ID_ATTRIBUTE).getNodeValue();
							if (expandOrCollapse)
							{
								if("Collapsed".equalsIgnoreCase(stateAttr.getNodeValue()))
								{
									expandCollapse(new UIProxyJNA(runtimeId), true);
									doNext = true;
								}
							}
							else
							{
								if("Expanded".equalsIgnoreCase(stateAttr.getNodeValue()))
								{
									expandCollapse(new UIProxyJNA(runtimeId), false);
									doNext = true;
								}
							}
						}
					}
				} while (doNext);
			}

			if (attribute.equalsIgnoreCase(ControlType.MenuItem.getName()))
			{
				List<String> split = new LinkedList<>(Arrays.asList(path.split("/")));
				if (split.size() == 1)
				{
					expandCollapse(component, expandOrCollapse);
					return true;
				}
				else
				{
					expandCollapse(component, true);
					split.remove(0);
				}

				for (int i = 0; i < split.size() - 1; i++)
				{
					expandCollapse(new UIProxyJNA(findItem(component, split.get(i))), true);
				}
				expandCollapse(new UIProxyJNA(findItem(component, split.get(split.size() - 1))), expandOrCollapse);
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
					int size = -1;
					try
					{
						List<UIProxyJNA> elements = this.findAll(null, locator);
						size = elements.size();
					}
					catch (ElementNotFoundException e)
					{
						size = 0;
					}

					this.logger.debug("Found : " + size + " elements on method wait. Expected : " + (toAppear ? ">0" : "0") );
					if (toAppear)
					{
						if (size > 0)
						{
							return true;
						}
					}
					else
					{
						if (size == 0)
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
	public List<String> getListDerived(UIProxyJNA component, boolean onlyVisible) throws Exception
	{
		try
		{
			String result = this.driver.getList(component, onlyVisible);
			return result.isEmpty() ? new ArrayList<String>() : Arrays.asList(result.split(SEPARATOR_COMMA));
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
	public Document getTree(UIProxyJNA component) throws Exception
	{
		Document document = convertTreeToXMLDoc(component);

		NodeList nodes = findNodesInTreeByXpath(document, "//*");
		for (int i = nodes.getLength() - 1; i >= 0; i--)
		{
			NamedNodeMap attributes = nodes.item(i).getAttributes();
			if(attributes.getNamedItem(RUNTIME_ID_ATTRIBUTE) != null)
			{
				attributes.removeNamedItem(RUNTIME_ID_ATTRIBUTE);
			}
			if(attributes.getNamedItem(STATE) != null)
			{
				attributes.removeNamedItem(STATE);
			}
		}

		return document;
    }

    @Override
	public String getValueDerived(UIProxyJNA component) throws Exception
	{
		try
		{
			List<WindowPattern> availablePatterns = this.driver.getAvailablePatterns(component);

			String result;
			if (availablePatterns.contains(WindowPattern.SelectionPattern))
			{
				result = this.driver.getProperty(component, WindowProperty.SelectionProperty);
			}
			else if (availablePatterns.contains(WindowPattern.SelectionItemPattern))
			{
				result = this.driver.getProperty(component, WindowProperty.IsSelectedProperty);
			}
			else if (availablePatterns.contains(WindowPattern.TogglePattern))
			{
				result = this.driver.getProperty(component, WindowProperty.ToggleStateProperty);
			}
			else if (availablePatterns.contains(WindowPattern.TextPattern))
			{
				result = this.driver.getProperty(component, WindowProperty.IsTextPatternAvailableProperty);
			}
			else if (availablePatterns.contains(WindowPattern.RangeValuePattern))
			{
				result = this.driver.getProperty(component, WindowProperty.IsRangeValuePatternAvailableProperty);
			}
			else
			{
				result = this.driver.getProperty(component, WindowProperty.ValueProperty);
				if (Str.IsNullOrEmpty(result))
				{
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
	public String getDerived(UIProxyJNA component) throws Exception
	{
		//TODO need remake, cause get() need return text of component, not value;
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
	public String getAttrDerived(UIProxyJNA component, String name) throws Exception
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
				String property = this.driver.getProperty(component, WindowProperty.BoundingRectangleProperty);
				Rectangle rect = stringToRect(property);
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
	public String scriptDerived(UIProxyJNA component, String script) throws Exception
	{
		throw new FeatureNotSupportedException("script");
	}

	@Override
	public boolean dragNdrop(UIProxyJNA drag, int x1, int y1, UIProxyJNA drop, int x2, int y2, boolean moveCursor) throws Exception
	{
		try
		{
			String property = this.driver.getProperty(drag, WindowProperty.BoundingRectangleProperty);
			Rectangle dragRect = stringToRect(property);
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
				Rectangle rDrop = stringToRect(this.driver.getProperty(drop, WindowProperty.BoundingRectangleProperty));
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
	public boolean scrollTo(UIProxyJNA component, int index) throws Exception
	{
		try
		{
			List<UIProxyJNA> elementsList = Collections.emptyList();
			String attribute = this.driver.elementAttribute(component, AttributeKind.TYPE_NAME);
			if (attribute.equalsIgnoreCase(ControlType.ComboBox.getName()))
			{
				elementsList = findComponents(component, WindowTreeScope.Children, WindowProperty.ControlTypeProperty, ControlType.List.getStringId());
				if(elementsList.isEmpty())
				{
					elementsList = findComponents(component, WindowTreeScope.Children, WindowProperty.ControlTypeProperty, ControlType.ListItem.getStringId());
				}
				else
				{
					elementsList = findComponents(elementsList.get(0), WindowTreeScope.Children, WindowProperty.ControlTypeProperty, ControlType.ListItem.getStringId());
				}
			}
			if (attribute.equalsIgnoreCase(ControlType.List.getName()))
			{
				elementsList = findComponents(component, WindowTreeScope.Children, WindowProperty.ControlTypeProperty, ControlType.ListItem.getStringId());
			}
			if (attribute.equalsIgnoreCase(ControlType.Tab.getName()))
			{
				elementsList = findComponents(component, WindowTreeScope.Children, WindowProperty.ControlTypeProperty, ControlType.TabItem.getStringId());
			}
			if (attribute.equalsIgnoreCase(ControlType.Tree.getName()))
			{
				elementsList = findComponents(component, WindowTreeScope.Descendants, WindowProperty.ControlTypeProperty, ControlType.TreeItem.getStringId());
			}
			if (index > elementsList.size() || index < 0)
			{
				throw new WrongParameterException("Cant scroll to index " + index + ". Child size : " + elementsList.size());
			}

			UIProxyJNA element = elementsList.get(index);
			if(checkPatternIsAvailable(element, WindowPattern.ScrollItemPattern))
			{
				this.driver.doPatternCall(element, WindowPattern.ScrollItemPattern, "ScrollIntoView", null, -1);
			}
			return true;
		}
		catch(WrongParameterException ignored)
		{
			return true;
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
	public String getValueTableCellDerived(UIProxyJNA component, int column, int row) throws Exception
	{
		try
		{
			String valueTableCell = this.driver.getValueTableCell(component, column, row);
			return EMPTY_CELL.equals(valueTableCell) ? "" : valueTableCell;
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
	public Map<String, String> getRowDerived(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
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
			List<String> returnedList = new ArrayList<>();
			String attribute = this.driver.elementAttribute(component, AttributeKind.TYPE_NAME);
			if (attribute.equalsIgnoreCase(ControlType.Tree.getName()))
			{
				List<UIProxyJNA> elementsList = findComponents(component, WindowTreeScope.Descendants, WindowProperty.ControlTypeProperty, ControlType.TreeItem.getStringId());
				Map<String, Object> values = new HashMap<>();
				for (int i = 0; i < elementsList.size(); i++) {
					values.clear();
					values.put(valueCondition.getName(), this.driver.elementAttribute(elementsList.get(i), AttributeKind.NAME));
					if(valueCondition.isMatched(values))
					{
						returnedList.add(String.valueOf(i));
					}
				}
				return returnedList;
			}
			else
			{
				String result = this.driver.getRowIndexes(component, useNumericHeader, valueCondition, columnsToString(columns));
				if (result.isEmpty())
				{
					return new ArrayList<>();
				}
				String[] indexes = result.split(SEPARATOR_CELL);
				Collections.addAll(returnedList, indexes);
				return returnedList;
			}
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
	public Map<String, String> getRowByIndexDerived(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
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
	public Map<String, ValueAndColor> getRowWithColorDerived(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		throw new FeatureNotSupportedException("getRowWithColor");
	}

	@Override
	public String[][] getTableDerived(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, String[] columns) throws Exception
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
		String property = this.driver.getProperty(component, WindowProperty.BoundingRectangleProperty);
    	Rectangle rectangle = stringToRect(property);
		return new Robot().getPixelColor(rectangle.x + x, rectangle.y + y);
    }

    //region private methods
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
				res.add(EMPTY_HEADER_CELL.equals(columns[i]) ? "" : columns[i]);
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

	private NodeList findNodesInTreeByXpath(Document document, String selectedText) throws XPathExpressionException
	{
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(selectedText);

		Object result = expr.evaluate(document, XPathConstants.NODESET);
		return (NodeList) result;
	}

	private Document convertTreeToXMLDoc(UIProxyJNA component) throws Exception
	{
		String xmlStr = this.driver.getXMLFromTree(component);
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlStr)));
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

	private boolean checkPatternIsAvailable(UIProxyJNA element, WindowPattern pattern) throws Exception
	{
		List<WindowPattern> availablePatterns = this.driver.getAvailablePatterns(element);
		return availablePatterns.contains(pattern);
	}

	private List<UIProxyJNA> findComponents(UIProxyJNA component, WindowTreeScope scope, WindowProperty property, String controlTypeId) throws Exception
	{
		try
		{
			int length = 100;
			int[] arr = new int[length];
			int count = this.driver.findAll(arr, component, scope, property, controlTypeId);
			if (count > length)
			{
				length = count;
				arr = new int[length];
				this.driver.findAll(arr, component, scope, property, controlTypeId);
			}
			ArrayList<UIProxyJNA> list = new ArrayList<>();

			int itemsCount = arr[0];
			int itemLength = arr[1];
			int[] items = Arrays.copyOfRange(arr, 2, arr.length);
			for (int i = 0; i < itemsCount; i++)
			{
				UIProxyJNA elem = new UIProxyJNA(Arrays.copyOfRange(items, 0, itemLength));
				if(checkVisible(elem))
				{
					list.add(elem);
				}
				items = Arrays.copyOfRange(items, itemLength+1, items.length);
			}
			return list;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("select(%s)", component));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private int[] findItem(UIProxyJNA component, String selectedText) throws Exception
	{
		try
		{
			WindowTreeScope treeScope = WindowTreeScope.Descendants;
			if (this.driver.elementAttribute(component, AttributeKind.TYPE_NAME).toLowerCase().contains(ControlType.Tab.getName()))
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
			return itemId;
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

	private void expandCollapse(UIProxyJNA component, boolean expandOrCollapse) throws Exception
	{
		if (expandOrCollapse)
		{
			this.driver.doPatternCall(component, WindowPattern.ExpandCollapsePattern, "Expand", null, -1);
		}
		else
		{
			this.driver.doPatternCall(component, WindowPattern.ExpandCollapsePattern, "Collapse", null, -1);
		}
	}

	private boolean checkVisible(UIProxyJNA elem) throws Exception
	{
		return "true".equalsIgnoreCase(this.driver.elementAttribute(elem, AttributeKind.VISIBLE));
	}

	private Rectangle stringToRect(String stringRect) throws RemoteException
	{
		Rectangle rectangle = new Rectangle();
		Pattern pattern = Pattern.compile(RECTANGLE_PATTERN);
		Matcher matcher = pattern.matcher(stringRect);
		if (matcher.matches())
		{
			rectangle.setBounds(
					Integer.parseInt(matcher.group(1))
					, Integer.parseInt(matcher.group(2))
					, Integer.parseInt(matcher.group(3))
					, Integer.parseInt(matcher.group(4)));
		}
		else
		{
			throw new RemoteException("returned rectangle not matches pattern " + RECTANGLE_PATTERN+" , rect : " + stringRect);
		}
		return rectangle;
	}
	//endregion
}
