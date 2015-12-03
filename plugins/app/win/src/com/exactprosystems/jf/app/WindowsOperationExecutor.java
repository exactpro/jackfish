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

import java.awt.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class WindowsOperationExecutor implements OperationExecutor<UIProxy>
{
	public WindowsOperationExecutor(Driver driver, Logger logger)
	{
		this.driver = driver;
		this.logger = logger;
	}

	@Override
	public Rectangle getRectangle(UIProxy component) throws Exception
	{
		logger.trace("::getRectangle " + component);
		
		Rectangle ret = new Rectangle(component.getX(), component.getY(), component.getWidth(), component.getHeight());

		
		logger.trace("::getRectangle << " + ret);
		return ret;
	}
	
	@Override
	public Color getColor(String color)
	{
		logger.trace("::getColor " + color);
		Color ret = null;
		if (color != null)
		{
			if (color.equalsIgnoreCase("transparent"))
			{
				ret = new Color(255, 255, 255, 0);
			}
			else
			{
				StringBuilder colorSB = new StringBuilder(color);
				colorSB.delete(0, 5);
				colorSB.deleteCharAt(colorSB.length() - 1);
				String[] colors = colorSB.toString().split(", ");
				ret = new Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]), Integer.parseInt(colors[3]));
			}
		}

		logger.trace("::getColor << " + ret);
		return ret;
	}


	@Override
	public String get(UIProxy component) throws Exception
	{
		logger.trace("::get " + component);

		updateDataTable(component, null, KindInformation.Value);
		
		String ret = null;
		DataTable data = component.getData(KindInformation.Value);
		if (data != null)
		{
			ret = data.toString();
		}
		else
		{
			ret = this.driver.driverGetProperty(component, WindowProperty.ValueProperty);
		}

		logger.trace("::get << " + ret);
		return ret;
	}
	
	@Override
	public String getAttr(UIProxy component, String name) throws Exception
	{
		// TODO make it
		return "";
	}
	

	@Override
	public Map<String, String> getRow(UIProxy component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		logger.trace("::getRow " + component + " " + valueCondition + " " + colorCondition);
		try
		{
			if (valueCondition != null)
			{
				updateDataTable(component, additional, KindInformation.Value);
			}
			if (colorCondition != null)
			{
				updateDataTable(component, additional, KindInformation.Color);
			}
			
			Map<String, String> res = null;
			List<Map<String, String>> list = new ArrayList<>();

			
			String[] headers = component.getData(KindInformation.Value).getHeades();

			for (String[] row : component.getData(KindInformation.Value).getData())
			{
				if (rowMatches(row, valueCondition, colorCondition, headers))
				{
					list.add(getRowValues(row, headers));
				}
			}
			if (list.size() == 1)
			{
				res = list.get(0);
			}
			else 
			{
				throw new Exception("Found " + list.size() + " rows instead 1.");
			}

			logger.trace("::getRow << " + res);
			return res;
		}
		catch (Exception e)
		{
			logger.error(String.format("Error getRow(%s, %s, %s)", component, valueCondition, colorCondition));
			logger.error(e.getMessage(), e);
			throw new RemoteException(e.getMessage());
		}

	}

	@Override
	public List<String> getRowIndexes(UIProxy component, Locator additional, Locator header, boolean useNumericHeader, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		logger.trace("::getRowIndexes " + component + " " + valueCondition + " " + colorCondition);
		
		try
		{
			if (valueCondition != null)
			{
				updateDataTable(component, additional, KindInformation.Value);
			}
			if (colorCondition != null)
			{
				updateDataTable(component, additional, KindInformation.Color);
			}

			List<String> result = new ArrayList<>();

			String[] headers = component.getData(KindInformation.Value).getHeades();
			int i = 0;
			for (String[] row : component.getData(KindInformation.Value).getData())
			{
				if (rowMatches(row, valueCondition, colorCondition, headers))
				{
					result.add(String.valueOf(i));
				}
				i++;
			}

			logger.trace("::getRowIndexes << " + result);
			return result;
		}
		catch (Exception e)
		{
			logger.error(String.format("Error getRowIndexes(%s, %s, %s)", component, valueCondition, colorCondition));
			logger.error(e.getMessage(), e);
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public Map<String, String> getRowByIndex(UIProxy component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		logger.trace("::getRowByIndex " + component + " " + i);
		try
		{
			updateDataTable(component, additional, KindInformation.Value);
			
			String[] headers = component.getData(KindInformation.Value).getHeades();
			String[] row = component.getData(KindInformation.Value).getData()[i];
			Map<String, String> res = getRowValues(row, headers);

			logger.trace("::getRowByIndexe << " + res);
			return res;
		}
		catch (Exception e)
		{
			logger.error("Error on");
			logger.error(e.getMessage(), e);
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public Map<String, ValueAndColor> getRowWithColor(UIProxy component, Locator additional, Locator header, boolean useNumericHeader, int i) throws Exception
	{
		logger.trace("::getRowWithColor " + component + " " + i);
		try
		{
			updateDataTable(component, additional, KindInformation.Value);
			updateDataTable(component, additional, KindInformation.Color);
			updateDataTable(component, additional, KindInformation.BackColor);
			
			String[] headers = component.getData(KindInformation.Value).getHeades();
			String[] rowValue = component.getData(KindInformation.Value).getData()[i];
			String[] rowColor = component.getData(KindInformation.Value).getData()[i];
			String[] rowBackColor = component.getData(KindInformation.Value).getData()[i];
			Map<String, ValueAndColor> res = new HashMap<String, ValueAndColor>(); 
			
			
			for (int col = 0; col < headers.length; col++)
			{
				res.put(headers[col], new ValueAndColor(rowValue[col], getColor(rowColor[col]), getColor(rowBackColor[col])));
			}

			logger.trace("::getRowWithColor << " + res);
			return res;
		}
		catch (Exception e)
		{
			logger.error("Error on");
			logger.error(e.getMessage(), e);
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public String[][] getTable(UIProxy component, Locator additional, Locator header, boolean useNumericHeader) throws Exception
	{
		return new String[][]{{}};
	}

	public List<UIProxy> findAll(ControlKind controlKind, UIProxy window, Locator locator) throws Exception
	{
		logger.trace("::findAll " + controlKind + " " + window + " " + locator);
		System.out.println("::findAll " + controlKind + " " + window + " " + locator);
		
		List<UIProxy> ret = this.driver.driverFindAllForLocator(controlKind, window, locator);
		
		logger.trace("::findAll << " + ret);
		System.out.println("::findAll <<" + ret);
		return ret;
	}

	public List<UIProxy> findAll(Locator owner, Locator locator) throws Exception
	{
		logger.trace("::findAll " + owner + " " + locator);
		System.out.println("::findAll " + owner + " " + locator);
		
		UIProxy window = null;

		List<UIProxy> ret = this.driver.driverFindAllForLocator(locator.getControlKind(), window, locator);
		if (owner != null)
		{
			window = driver.driverFindFirstForLocator(owner.getControlKind(), null, owner);
		}
		
		logger.trace("::findAll << " + ret);
		System.out.println("::findAll <<" + ret);
		return ret;
	}

	@Override
	public UIProxy find(Locator owner, Locator locator) throws Exception
	{
		logger.trace("::find " + owner + "  " + locator);
		UIProxy window = null;
		UIProxy ret = null;

		if (owner != null)
		{
			window = driver.driverFindFirstForLocator(owner.getControlKind(), null, owner);
		}

		ControlKind controlKind = locator.getControlKind();
		ret = driver.driverFindFirstForLocator(controlKind, window, locator);
		
		logger.trace("::find << " + ret);
		return ret;
	}

	@Override
	public UIProxy lookAtTable(UIProxy tableComp, Locator additional, Locator header, int x, int y) throws Exception
	{
		logger.trace("::lookAtTable " + tableComp + " " + x + " " + y);

		try
		{
//			if (y < 0)
//			{
//				return new DummyUIProxy();
//			}
//			else if (x < 0)
//			{
//				List<UIProxy> rows = tableComp.findElement(By.tagName(tag_tbody)).findElements(By.tagName(tag_tr));
//				return  rows.get(y);
//			}
//			else
//			{
//				logger.debug("Row = " + y);
//				List<UIProxy> rows = tableComp.findElement(By.tagName(tag_tbody)).findElements(By.tagName(tag_tr));
//				logger.debug("Rows count = " + rows.size());
//				UIProxy row1 = rows.get(y);
//				logger.debug("Column = " + x);
//				List<UIProxy> cells1 = row1.findElements(By.tagName(tag_td));
//				logger.debug("Column count "+cells1.size());
//				return cells1.get(x);
//			}

			logger.trace("::lookAtTable << " + null);
			return null;
		}
		catch (Exception e)
		{
			logger.error("Error on find into table");
			logger.error(e.getMessage(), e);
		}
		throw new Exception("Error on find into table");
	}

	
	@Override
	public boolean press(UIProxy component, Keyboard key) throws Exception
	{
		logger.trace("::press " + component + " " + key);
		this.driver.driverSendKey(key);
		logger.trace("::press << " + true);
		return true;
	}

	@Override
	public boolean upAndDown(UIProxy component, Keyboard key, boolean b) throws Exception
	{
		//TODO implements this method
		return false;
	}

	@Override
	public boolean push(UIProxy component) throws Exception
	{
		logger.trace("::push " + component);
		this.driver.driverDoPatternCall(component, WindowPattern.InvokePattern, "Invoke");
		logger.trace("::push<< " + true);
		return true;
	}

	@Override
	public boolean mouse(UIProxy component, int x, int y, MouseAction action) throws Exception
	{
		logger.trace("::click " + component + " " + x + " " + y);
		this.driver.driverMouseClick(component, action, x, y);
		logger.trace("::click << " + true);
		return true;
	}

	@Override
	public boolean text(UIProxy component, String text, boolean clear) throws Exception
	{
		logger.trace("::text " + component + " " + text + " " + clear);
		this.driver.driverDoPatternCall(component, WindowPattern.ValuePattern, "SetValue", text);
		logger.trace("::text << " + true);
		return true;
	}

	@Override
	public boolean toggle(UIProxy component, boolean value) throws Exception
	{
		logger.trace("::toggle " + component + " " + value);

		String state = this.driver.driverGetProperty(component, WindowProperty.ToggleStateProperty);
		if (state.equals("Off") && value)
		{
			this.driver.driverDoPatternCall(component, WindowPattern.TogglePattern, "Toggle");
		}

		logger.trace("::toggle << " + true);
		return true;
	}

    @Override
    public boolean select(UIProxy component, String selectedText) throws Exception
    {
        logger.trace("::select " + component + " " + selectedText);

        UIProxy item = this.driver.driverFindFirst(component, WindowTreeScope.Subtree, WindowProperty.NameProperty, selectedText);
        if (item == null)
        {
        	throw new Exception("Item with name '" + selectedText + "' is not found.");
        }
        this.driver.driverDoPatternCall(item, WindowPattern.SelectionItemPattern, "Select");

        logger.trace("::select << " + true);
        return true;
    }

	@Override
	public boolean fold(UIProxy component, String path, boolean collaps) throws Exception
	{
		// TODO process the parameter path
        logger.trace("::mark " + component);

        if (collaps)
        {
        	this.driver.driverDoPatternCall(component, WindowPattern.ExpandCollapsePattern, "Collapse");
        }
        else
        {
        	this.driver.driverDoPatternCall(component, WindowPattern.ExpandCollapsePattern, "Expand");
        }

        logger.trace("::mark << " + true);
        return true;
	}
    
    
    @Override
	public boolean wait(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong) throws Exception
	{
		logger.trace("::wait " + locator + " " + ms + " " + toAppear);

		boolean ret = false;
		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() < time + ms)
		{
			try
			{
				List<UIProxy> elements = findAll(ControlKind.Wait, null, locator);
				if (toAppear)
				{
					if (elements.size() > 0)
					{
						ret = true;
						break;
					}
				}
				else
				{
					if (elements.size() == 0)
					{
						ret = true;
						break;
					}
				}
			}
			catch (Exception e)
			{
				logger.error("Error on waiting");
				logger.error(e.getMessage(), e);
			}
		}
		logger.trace("::wait << " + ret);
		return ret;
	}

	@Override
	public boolean setValue(UIProxy component, double value) throws Exception
	{
		logger.trace("::set " + component + " " + value);
		String maxS = this.driver.driverGetProperty(component, WindowProperty.MaximumProperty);
		String minS = this.driver.driverGetProperty(component, WindowProperty.MinimumProperty);
		double max = Double.parseDouble(maxS);
		double min = Double.parseDouble(minS);
		double v = ((max - min) / 100) * value;
		this.driver.driverDoPatternCall(component, WindowPattern.RangeValuePattern, "SetValue", v);
		logger.trace("::set <<" + true);
		return true;
	}

	@Override
	public String getValue(UIProxy component) throws Exception
	{
		return get(component);
	}

	@Override
	public boolean tableIsContainer()
	{
		return false;
	}

	@Override
	public boolean mouseTable(UIProxy component, int column, int row, MouseAction action) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getValueTableCell(UIProxy component, int column, int row) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean textTableCell(UIProxy component, int column, int row, String text) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}


	private boolean rowMatches(String[] row, ICondition valueCondition, ICondition colorCondition, String[] headers) throws Exception
	{
		for (int i = 0; i < headers.length; i++)
		{
			String value = row[i];
			String name = headers[i];
			
			// TODO: add colorCondition
			if (!valueCondition.isMatched(name, value))
			{
				return false;
			}
		}
		
		return true;
	}

	private Map<String, String> getRowValues(String[] row, String[] headers)
	{
		Map<String, String> res = new HashMap<String, String>();
		for (int i = 0; i < headers.length; i++)
		{
			res.put(headers[i], row[i]);
		}
		
		return res;
	}


	private void updateDataTable(UIProxy component, Locator additional, KindInformation kind) throws Exception
	{
		if (component == null)
		{
			return;
		}
		if (component.getData(kind) == null)
		{
			DataTable data = this.driver.driverGetDataTable(component, additional, kind);
			component.setData(kind, data);
		}
	}
	
	private Driver driver;
	
	private Logger logger;
}
