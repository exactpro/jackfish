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
import com.exactprosystems.jf.api.client.LimitedArrayList;
import com.exactprosystems.jf.api.common.Str;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class WinOperationExecutorJNA implements OperationExecutor<UIProxyJNA>
{
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
		//TODO we can get this on Visibility BoundingRectangle ( this information from UIVerify)
		return null;
	}

	@Override
	public Color getColor(String color) throws Exception
	{
		//TODO think about it
		return null;
	}

	//TODO this method call only if element are many
	@Override
	public List<UIProxyJNA> findAll(ControlKind controlKind, UIProxyJNA window, Locator locator) throws Exception
	{
		//TODO need be implemented
		return null;
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
//			UIProxyJNA ownerElement = new UIProxyJNA(null);
//			if (owner != null)
//			{
//				ownerElement = find(null, owner);
//			}
//			int length = 100;
//			int[] result = new int[length];
//			int count = this.driver.findAllForLocator(result, length, ownerElement.getIdString(), element.getControlKind()
//					.ordinal(), element.getUid(), element.getXpath(), element.getClazz(), element.getName(), element.getTitle(), element
//					.getText());
//			if (count > length)
//			{
//				length = count;
//				result = new int[length];
//				this.driver.findAllForLocator(result, length, ownerElement.getIdString(), element.getControlKind()
//						.ordinal(), element.getUid(), element.getXpath(), element.getClazz(), element.getName(), element
//						.getTitle(), element.getText());
//			}
//			int foundElement = result[0];
//
//			int[] r = new int[count];
//			System.arraycopy(result, 0, r, 0, count);
//			return new UIProxyJNA(r);
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
			this.driver.mouse(component.getIdString(), action.getId(), x, y);
		}
		catch (Exception e)
		{
			logger.error(String.format("mouse(%s,%d,%d,%s)", component, x, y, action));
			logger.error(e.getMessage(), e);
			throw e;
		}
		return false;
	}

	@Override
	public boolean press(UIProxyJNA component, Keyboard key) throws Exception
	{
		//TODO need release
		return false;
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
			this.driver.doPatternCall(component.getIdString(), WindowPattern.InvokePattern.getId(), "Invoke", null);
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
		//TODO call to checkbox, radiobutton and togglebutton - doCallPattern
		return false;
	}

	@Override
	public boolean select(UIProxyJNA component, String selectedText) throws Exception
	{
		//TODO call to listView, comboBox and tabPanel - doPatternCall
		return false;
	}

	@Override
	public boolean fold(UIProxyJNA component, String path, boolean collaps) throws Exception
	{
		//TODO call to menu and tree - doPatternCall
		return false;
	}

	@Override
	public boolean text(UIProxyJNA component, String text, boolean clear) throws Exception
	{
		try
		{
			if (clear)
			{
				this.driver.doPatternCall(component.getIdString(), WindowPattern.ValuePattern.getId(), "SetValue", new Object[]{""});
			}
			this.driver.doPatternCall(component.getIdString(), WindowPattern.ValuePattern.getId(), "SetValue", new Object[]{text});
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
		return false;
	}

	@Override
	public boolean setValue(UIProxyJNA component, double value) throws Exception
	{
		//TODO doPatternCall from many controlKind
		return false;
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
		//get Property?
		return null;
	}

	@Override
	public boolean mouseTable(UIProxyJNA component, int column, int row, MouseAction action) throws Exception
	{
		return false;
	}

	@Override
	public boolean textTableCell(UIProxyJNA component, int column, int row, String text) throws Exception
	{
		return false;
	}

	@Override
	public String getValueTableCell(UIProxyJNA component, int column, int row) throws Exception
	{
		return null;
	}

	@Override
	public Map<String, String> getRow(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		return null;
	}

	@Override
	public List<String> getRowIndexes(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		return null;
	}

	@Override
	public Map<String, String> getRowByIndex(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		return null;
	}

	@Override
	public Map<String, ValueAndColor> getRowWithColor(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		return null;
	}

	@Override
	public String[][] getTable(UIProxyJNA component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		return new String[0][];
	}

	public Locator locatorFromUIProxy(UIProxyJNA element) throws Exception
	{
		String uid = this.driver.getProperty(element.getIdString(), WindowProperty.AutomationIdProperty.getId());
		String clazz = this.driver.getProperty(element.getIdString(), WindowProperty.ClassNameProperty.getId());
		String name = this.driver.getProperty(element.getIdString(), WindowProperty.NameProperty.getId());
		String tooltip = this.driver.getProperty(element.getIdString(), WindowProperty.HelpTextProperty.getId());
		Locator locator = new Locator(null, "newElement", ControlKind.findByClazz(clazz));
		locator.uid(uid).clazz(clazz).name(name).tooltip(tooltip);
		return locator;
	}

	public Locator locatorFromUIProxy(int[] id) throws Exception
	{
		return locatorFromUIProxy(new UIProxyJNA(id));
	}
}
