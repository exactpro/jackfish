////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.error.app.NullParameterException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Operation implements Iterable<Part>, Serializable
{
	private static final long	serialVersionUID	= -5215749916211825468L;

	protected Operation()
	{
		this.list = new ArrayList<Part>();
	}

	//------------------------------------------------------------------------------------------------------------------
	// Object
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(Do.class.getSimpleName());
		for (Part p : list)
		{
			sb.append(p.toString());
		}
		return sb.toString();
	}

	//------------------------------------------------------------------------------------------------------------------
	// interface Iterable
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public Iterator<Part> iterator()
	{
		return this.list.iterator();
	}
	//------------------------------------------------------------------------------------------------------------------


	public <T> void operate(OperationExecutor<T> executor, Locator locator, T element) throws Exception
	{
		OperationResult result = new OperationResult();
		Holder<T> holder = new Holder<T>(element, locator);

		for (Part part : this.list)
		{
			part.kind.operate(part, executor, holder, result);
		}
	}

	public <T> OperationResult operate(OperationExecutor<T> executor, Locator owner, Locator locator, Locator rows, Locator header) throws Exception
	{
		if (locator == null)
		{
			throw new NullParameterException("locator");
		}
		
		OperationResult result = new OperationResult();
		Holder<T> holder = new Holder<T>(null, null);
		holder.put(LocatorKind.Element, 	locator);
		holder.put(LocatorKind.Owner, 	owner);
		holder.put(LocatorKind.Rows,		rows);
		holder.put(LocatorKind.Header, 	header);
		
		for (Part part : this.list)
		{
			if (!part.kind.operate(part, executor, holder, result))
			{
				result.setOk(false);
				return result;
			}
		}

		result.setOk(true);
		return result;
	}

	public static Operation create()
	{
		return new Operation();
	}

	public void tune(IWindow window) throws Exception
	{
		for (Part part : this.list)
		{
			part.tune(window);
		}
	}
	
	public Part addPart(OperationKind kind)
	{
		Part part = new Part(kind);
		this.list.add(part);
		return part;
	}
	
	
	
	

	@DescriptionAttribute(text = Do.foreach)
	public Operation foreach(Operation operation)
	{
		this.list.add(new Part(OperationKind.FOREACH).setInt(Integer.MAX_VALUE).setOperation(operation));
		return this;
	}

	@DescriptionAttribute(text = Do.foreach_max)
	public Operation foreach(Operation operation, int max)
	{
		this.list.add(new Part(OperationKind.FOREACH).setInt(max).setOperation(operation));
		return this;
	}

	@DescriptionAttribute(text = Do.repeat)
	public Operation repeat(int i,  Operation operation)
	{
		this.list.add(new Part(OperationKind.REPEAT).setInt(i).setOperation(operation));
		return this;
	}

	@DescriptionAttribute(text = Do.count)
	public Operation count()
	{
		this.list.add(new Part(OperationKind.COUNT));
		return this;
	}

	@DescriptionAttribute(text = Do.use)
	public Operation use(int i)
	{
		this.list.add(new Part(OperationKind.USE).setInt(i));
		return this;
	}

	@DescriptionAttribute(text = Do.setValue)
	public Operation setValue(double value)
	{
		this.list.add(new Part(OperationKind.SET).setValue(value));
		return this;
	}

	@DescriptionAttribute(text = Do.getValue)
	public Operation getValue()
	{
		this.list.add(new Part(OperationKind.GET_VALUE));
		return this;
	}

    @DescriptionAttribute(text = Do.getList)
    public Operation getList()
    {
        this.list.add(new Part(OperationKind.GET_LIST));
        return this;
    }

	@DescriptionAttribute(text = Do.getRectangle)
	public Operation getRectangle()
	{
		this.list.add(new Part(OperationKind.GET_RECTANGLE));
		return this;
	}

	@DescriptionAttribute(text = Do.push)
	public Operation push()
	{
		this.list.add(new Part(OperationKind.PUSH));
		return this;
	}

	@DescriptionAttribute(text = Do.press)
	public Operation press(Keyboard key)
	{
		this.list.add(new Part(OperationKind.PRESS).setKey(key));
		return this;
	}

	@DescriptionAttribute(text = Do.keyDown)
	public Operation keyDown(Keyboard key)
	{
		this.list.add(new Part(OperationKind.KEY_DOWN).setKey(key));
		return this;
	}

	@DescriptionAttribute(text = Do.keyUp)
	public Operation keyUp(Keyboard key)
	{
		this.list.add(new Part(OperationKind.KEY_UP).setKey(key));
		return this;
	}

	@DescriptionAttribute(text = Do.check)
	public Operation check(String word)
	{
		return check(word, true);
	}

	@DescriptionAttribute(text = Do.checkWithCoor)
	public Operation check(String word, int x, int y)
	{
		return check(word, x, y, true);
	}

	@DescriptionAttribute(text = Do.checkWithCoorAndFlag)
	public Operation check(String word, int x, int y, boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_XY).setText(word).setX(x).setY(y).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkWithFlag)
	public Operation check(String word, boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK).setText(word).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkRegexp)
	public Operation checkRegexp(String regexp)
	{
		return checkRegexp(regexp, true);
	}

	@DescriptionAttribute(text = Do.checkRegexpCoor)
	public Operation checkRegexp(String regexp, int x, int y)
	{
		return checkRegexp(regexp, x, y, true);
	}

	@DescriptionAttribute(text = Do.checkAttr)
	public Operation checkAttr(String name, String value)
	{
		return checkAttr(name, value, true);
	}

	@DescriptionAttribute(text = Do.checkAttrWithFlag)
	public Operation checkAttr(String name, String value, boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_ATTR).setStr(name).setText(value).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkAttrRegexp)
	public Operation checkAttrRegexp(String name, String regexp, boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_ATTR_REGEXP).setStr(name).setText(regexp).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkRegexpCoorAndFlag)
	public Operation checkRegexp(String regexp, int x, int y, boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_REGEXP_XY).setText(regexp).setX(x).setY(y).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkRegexpWithFlag)
	public Operation checkRegexp(String regexp, boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_REGEXP).setText(regexp).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.get)
	public Operation get()
	{
		this.list.add(new Part(OperationKind.GET));
		return this;
	}

	@DescriptionAttribute(text = Do.getAttr)
	public Operation getAttr(String name)
	{
		this.list.add(new Part(OperationKind.GET_ATTR).setStr(name));
		return this;
	}

	@DescriptionAttribute(text = Do.getValueWithCoor)
	public Operation getValue(int x, int y)
	{
		this.list.add(new Part(OperationKind.GET_VALUE_XY).setX(x).setY(y));
		return this;
	}

	@DescriptionAttribute(text = Do.getTable)
	public Operation getTable()
	{
		this.list.add(new Part(OperationKind.GET_TABLE));
		return this;
	}

	@DescriptionAttribute(text = Do.getRow)
	public Operation getRow(ICondition valueCondition, ICondition colorCondition)
	{
		this.list.add(new Part(OperationKind.GET_ROW).setValueCondition(valueCondition).setColorCondition(colorCondition));
		return this;
	}

	@DescriptionAttribute(text = Do.getRowIndexes)
	public Operation getRowIndexes(ICondition valueCondition, ICondition colorCondition)
	{
		this.list.add(new Part(OperationKind.GET_ROW_INDEXES).setValueCondition(valueCondition).setColorCondition(colorCondition));
		return this;
	}

	@DescriptionAttribute(text = Do.getRowByIndex)
	public Operation getRowByIndex(int index)
	{
		this.list.add(new Part(OperationKind.GET_ROW_BY_INDEX).setInt(index));
		return this;
	}

	@DescriptionAttribute(text = Do.getRowWithColor)
	public Operation getRowWithColor(int index)
	{
		this.list.add(new Part(OperationKind.GET_ROW_WITH_COLOR).setInt(index));
		return this;
	}

	@DescriptionAttribute(text = Do.getTableSize)
	public Operation getTableSize()
	{
		this.list.add(new Part(OperationKind.GET_TABLE_SIZE));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorId)
	public Operation use(String locator)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator).setLocatorKind(LocatorKind.Element));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator).setLocatorKind(LocatorKind.Owner));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator).setLocatorKind(LocatorKind.Header));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator).setLocatorKind(LocatorKind.Rows));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocator)
	public Operation use(Locator locator)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(locator).setLocatorKind(LocatorKind.Element));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(null).setLocatorKind(LocatorKind.Owner));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(null).setLocatorKind(LocatorKind.Header));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(null).setLocatorKind(LocatorKind.Rows));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorKind)
	public Operation use(Locator locator, LocatorKind locatorKind)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(locator).setLocatorKind(locatorKind));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorIdKind)
	public Operation use(String locatorId, LocatorKind locatorKind)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locatorId).setLocatorKind(locatorKind).setBool(true));
		return this;
	}

	@DescriptionAttribute(text = Do.move)
	public Operation move()
	{
		this.list.add(new Part(OperationKind.MOVE));
		return this;
	}

	@DescriptionAttribute(text = Do.moveWithCoor)
	public Operation move(int x, int y)
	{
		this.list.add(new Part(OperationKind.MOVE_XY).setX(x).setY(y));
		return this;
	}

	@DescriptionAttribute(text = Do.click)
	public Operation click()
	{
		this.list.add(new Part(OperationKind.CLICK).setMouseAction(MouseAction.LeftClick));
		return this;
	}

	@DescriptionAttribute(text = Do.clickWithCoor)
	public Operation click(int x, int y)
	{
		return click(x, y, MouseAction.LeftClick);
	}

	@DescriptionAttribute(text = Do.clickWithCoorAndAction)
	public Operation click(int x, int y, MouseAction action)
	{
		this.list.add(new Part(OperationKind.CLICK_XY).setX(x).setY(y).setMouseAction(action));
		return this;
	}

	@DescriptionAttribute(text = Do.text)
	public Operation text(String str)
	{
		return text(str, false);
	}

	@DescriptionAttribute(text = Do.textWithBool)
	public Operation text(String str, boolean clear)
	{
		this.list.add(new Part(OperationKind.TEXT).setText(str).setBool(clear));
		return this;
	}

	@DescriptionAttribute(text = Do.textColumnRow)
	public Operation text(String str, int column, int row)
	{
		this.list.add(new Part(OperationKind.TEXT_XY).setText(str).setX(column).setY(row));
		return this;
	}

	@DescriptionAttribute(text = Do.toggle)
	public Operation toggle(boolean bool)
	{
		this.list.add(new Part(OperationKind.TOGGLE).setBool(bool));
		return this;
	}

	@DescriptionAttribute(text = Do.selectByIndex)
    public Operation select(int index)
    {
        this.list.add(new Part(OperationKind.SELECT_BY_INDEX).setInt(index));
        return this;
    }

	@DescriptionAttribute(text = Do.select)
    public Operation select(String selectText)
    {
        this.list.add(new Part(OperationKind.SELECT).setText(selectText));
        return this;
    }

	@DescriptionAttribute(text = Do.expand)
	public Operation expand(String path)
	{
        this.list.add(new Part(OperationKind.EXPAND).setText(path));
        return this;
	}

	@DescriptionAttribute(text = Do.collapse)
	public Operation collapse(String path)
	{
        this.list.add(new Part(OperationKind.COLLAPSE).setText(path));
        return this;
	}

	public Operation script(String script)
	{
		this.list.add(new Part(OperationKind.SCRIPT).setText(script));
		return this;
	}

	public Operation dragNdrop(String another, int x, int y)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.Dropped));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.DroppedOwner));
		this.list.add(new Part(OperationKind.DRAG_N_DROP)
				.setX(x)
				.setY(y)
		);
		return this;
	}

	public Operation dragNdrop(Locator another, int x, int y)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(another).setLocatorKind(LocatorKind.Dropped));
		this.list.add(new Part(OperationKind.DRAG_N_DROP)
				.setX(x)
				.setY(y)
		);
		return this;
	}

	@DescriptionAttribute(text = Do.delay)
	public Operation delay(int ms)
	{
		this.list.add(new Part(OperationKind.DELAY).setInt(ms));
		return this;
	}

	@DescriptionAttribute(text = Do.wait)
	public Operation wait(String str)
	{
		this.list.add(new Part(OperationKind.WAIT).setLocatorId(str).setInt(10000).setBool(true));
		return this;
	}

	@DescriptionAttribute(text = Do.wait)
	public Operation wait(String str, int ms, boolean toAppear)
	{
		this.list.add(new Part(OperationKind.WAIT).setLocatorId(str).setInt(ms).setBool(toAppear));
		return this;
	}

	@DescriptionAttribute(text = Do.wait)
	public Operation wait(Locator locator, int ms, boolean toAppear)
	{
		this.list.add(new Part(OperationKind.WAIT).setLocator(locator).setInt(ms).setBool(toAppear));
		return this;
	}

	protected List<Part> list;
}
