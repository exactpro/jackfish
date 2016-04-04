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
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.List;
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
	public List<UIProxyJNA> findAll(Locator owner, Locator element) throws Exception {
        try {
            logger.trace(":: find all");
            UIProxyJNA ownerElement = new UIProxyJNA(null);
            if (owner != null) {
                ownerElement = this.find(null, owner);
            }
            int length = 10;
            int[] arr = new int[length];
            this.driver.findAll(arr, length, ownerElement.getId(), WindowTreeScope.Children.ordinal(), WindowProperty.NameProperty.getId(), element.getName());
            logger.trace("find all ::");
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

	@Override
	public UIProxyJNA find(Locator owner, Locator element) throws Exception
	{
        try {
            logger.trace(":: find");
            UIProxyJNA ownerElement = new UIProxyJNA(null);
            if (owner != null) {
                ownerElement = find(null, owner);
            }
            //TODO need check returned length and recall if need
            int length = 100;
            int[] result = new int[length];
            int count = this.driver.findAllForLocator(result, length, ownerElement.getId(), element.getControlKind().ordinal(), element.getUid(), element.getXpath(), element.getClazz(), element.getName(), element.getTitle(), element.getText());
            logger.trace("find ::");
            return new UIProxyJNA(result);
        } catch (Exception e) {
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
			this.logger.trace(":: mouse");
			this.driver.mouse(component.getId(), action.getId(), x, y);
			this.logger.trace("mouse ::");

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
        //TODO need call InvokerPattern, because push allowed
		return false;
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
        //todo doPatternCall for TextBox and Panel( O_O ??? )
		return false;
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
	public String getValue(UIProxyJNA component) throws Exception {
        try {
            this.logger.info(":: getvalue");
//            this.driver.doPatternCall(component.getId(), WindowPattern.ValuePattern.getId(),);
            this.logger.info("getvalue ::");
            return null;
        } catch (Exception e) {
            this.logger.error(String.format("getValue(%s)", component));
            this.logger.error(e.getMessage(), e);
            throw e;
        }
    }

	@Override
	public String get(UIProxyJNA component) throws Exception
	{
        //get property name or what?
		return null;
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
}
