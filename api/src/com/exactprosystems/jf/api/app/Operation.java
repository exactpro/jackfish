////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.FieldParameter;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.app.NullParameterException;

import java.awt.*;
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
	
	public OperationKind getOperationKind(int index)
	{
	    if (this.list != null && index < this.list.size())
	    {
	        return this.list.get(index).kind;
	    }
	    return OperationKind.NONE;
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
	public Operation foreach(@FieldParameter(name = "operation") Operation operation)
	{
		this.list.add(new Part(OperationKind.FOREACH).setInt(Integer.MAX_VALUE).setOperation(operation));
		return this;
	}

	@DescriptionAttribute(text = Do.foreach_max)
	public Operation foreach(@FieldParameter(name = "operation") Operation operation, @FieldParameter(name = "max") int max)
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
	public Operation use(@FieldParameter(name = "i") int i)
	{
		this.list.add(new Part(OperationKind.USE).setInt(i));
		return this;
	}

    @DescriptionAttribute(text = Do.apply)
    public Operation apply(String path, Operation operation)
    {
        this.list.add(new Part(OperationKind.APPLY).setStr(path).setOperation(operation));
        return this;
    }

    @DescriptionAttribute(text = Do.setValue)
	public Operation setValue(@FieldParameter(name = "value") double value)
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

    @DescriptionAttribute(text = Do.sequence)
    public Operation sequence(@FieldParameter(name = "text") String text)
    {
        for (char ch : text.toCharArray())
        {
            ch = Character.toUpperCase(ch);
            Keyboard key = Keyboard.byChar(ch);
            if (key != null)
            {
                this.list.add(new Part(OperationKind.PRESS).setKey(key));
            }
        }
        return this;
    }

	@DescriptionAttribute(text = Do.press)
	public Operation press(@FieldParameter(name = "key") Keyboard key)
	{
		this.list.add(new Part(OperationKind.PRESS).setKey(key));
		return this;
	}

	@DescriptionAttribute(text = Do.keyDown)
	public Operation keyDown(@FieldParameter(name = "key") Keyboard key)
	{
		this.list.add(new Part(OperationKind.KEY_DOWN).setKey(key));
		return this;
	}

	@DescriptionAttribute(text = Do.keyUp)
	public Operation keyUp(@FieldParameter(name = "key") Keyboard key)
	{
		this.list.add(new Part(OperationKind.KEY_UP).setKey(key));
		return this;
	}

    @DescriptionAttribute(text = Do.isEnabled)
    public Operation isEnabled()
    {
        this.list.add(new Part(OperationKind.IS_ENABLED));
        return this;
    }
	
    @DescriptionAttribute(text = Do.isVisible)
    public Operation isVisible()
    {
        this.list.add(new Part(OperationKind.IS_VISIBLE));
        return this;
    }
    
    @DescriptionAttribute(text = Do.checkList)
    public Operation checkList(@FieldParameter(name = "list") List<String> list, @FieldParameter(name = "ignoreOrder") boolean ignoreOrder)
    {
        this.list.add(new Part(OperationKind.CHECK_LIST).setList(list).setBool(ignoreOrder));
        return this;
    }

    @DescriptionAttribute(text = Do.checkColor)
    public Operation checkColor(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "color") Color color)
    {
        this.list.add(new Part(OperationKind.CHECK_COLOR_XY).setColorCondition(Condition.color("color", color)).setX(x).setY(y));
        return this;
    }

    @DescriptionAttribute(text = Do.check)
	public Operation check(@FieldParameter(name = "word") String word)
	{
		return check(word, true);
	}

	@DescriptionAttribute(text = Do.checkWithCoor)
	public Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return check(word, x, y, true);
	}

	@DescriptionAttribute(text = Do.checkWithCoorAndFlag)
	public Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "flag") boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_XY).setText(word).setX(x).setY(y).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkWithFlag)
	public Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "flag") boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK).setText(word).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkRegexp)
	public Operation checkRegexp(@FieldParameter(name = "regexp") String regexp)
	{
		return checkRegexp(regexp, true);
	}

	@DescriptionAttribute(text = Do.checkRegexpCoor)
	public Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return checkRegexp(regexp, x, y, true);
	}

	@DescriptionAttribute(text = Do.checkAttr)
	public Operation checkAttr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value)
	{
		return checkAttr(name, value, true);
	}

	@DescriptionAttribute(text = Do.checkAttrWithFlag)
	public Operation checkAttr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value, @FieldParameter(name = "flag") boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_ATTR).setStr(name).setText(value).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkAttrRegexp)
	public Operation checkAttrRegexp(@FieldParameter(name = "name") String name, @FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "flag") boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_ATTR_REGEXP).setStr(name).setText(regexp).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkRegexpCoorAndFlag)
	public Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "flag") boolean flag)
	{
		this.list.add(new Part(OperationKind.CHECK_REGEXP_XY).setText(regexp).setX(x).setY(y).setBool(flag));
		return this;
	}

	@DescriptionAttribute(text = Do.checkRegexpWithFlag)
	public Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "flag") boolean flag)
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
	public Operation getAttr(@FieldParameter(name = "name") String name)
	{
		this.list.add(new Part(OperationKind.GET_ATTR).setStr(name));
		return this;
	}

    @DescriptionAttribute(text = Do.getColor)
    public Operation getColor(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
    {
        this.list.add(new Part(OperationKind.GET_COLOR_XY).setX(x).setY(y));
        return this;
    }
	
	@DescriptionAttribute(text = Do.getValueWithCoor)
	public Operation getValue(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
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

	@DescriptionAttribute(text = Do.getTree)
	public Operation getTree() {
		this.list.add(new Part(OperationKind.GET_TREE));
		return this;
	}

	@DescriptionAttribute(text = Do.getRow)
	public Operation getRow(@FieldParameter(name = "valueCondition") ICondition valueCondition, @FieldParameter(name = "colorCondition") ICondition colorCondition)
	{
		this.list.add(new Part(OperationKind.GET_ROW).setValueCondition(valueCondition).setColorCondition(colorCondition));
		return this;
	}

	@DescriptionAttribute(text = Do.getRowOneCondition)
	public Operation getRow(@FieldParameter(name = "valueCondition") ICondition valueCondition)
	{
		this.list.add(new Part(OperationKind.GET_ROW).setValueCondition(valueCondition));
		return this;
	}

	@DescriptionAttribute(text = Do.getRowIndexes)
	public Operation getRowIndexes(@FieldParameter(name = "valueCondition") ICondition valueCondition, @FieldParameter(name = "colorCondition") ICondition colorCondition)
	{
		this.list.add(new Part(OperationKind.GET_ROW_INDEXES).setValueCondition(valueCondition).setColorCondition(colorCondition));
		return this;
	}

	@DescriptionAttribute(text = Do.getRowIndexesOneCondition)
	public Operation getRowIndexes(@FieldParameter(name = "valueCondition") ICondition valueCondition)
	{
		this.list.add(new Part(OperationKind.GET_ROW_INDEXES).setValueCondition(valueCondition));
		return this;
	}

	@DescriptionAttribute(text = Do.getRowByIndex)
	public Operation getRowByIndex(@FieldParameter(name = "index") int index)
	{
		this.list.add(new Part(OperationKind.GET_ROW_BY_INDEX).setInt(index));
		return this;
	}

	@DescriptionAttribute(text = Do.getRowWithColor)
	public Operation getRowWithColor(@FieldParameter(name = "index") int index)
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
	public Operation use(@FieldParameter(name = "locator") String locator)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator).setLocatorKind(LocatorKind.Element));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator).setLocatorKind(LocatorKind.Owner));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator).setLocatorKind(LocatorKind.Header));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator).setLocatorKind(LocatorKind.Rows));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocator)
	public Operation use(@FieldParameter(name = "locator") Locator locator)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(locator).setLocatorKind(LocatorKind.Element));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(null).setLocatorKind(LocatorKind.Owner));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(null).setLocatorKind(LocatorKind.Header));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(null).setLocatorKind(LocatorKind.Rows));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorKind)
	public Operation use(@FieldParameter(name = "locator") Locator locator, @FieldParameter(name = "locatorKind") LocatorKind locatorKind)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(locator).setLocatorKind(locatorKind));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorIdKind)
	public Operation use(@FieldParameter(name = "locatorId") String locatorId, @FieldParameter(name = "locatorKind") LocatorKind locatorKind)
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
	public Operation move(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
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
	public Operation click(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return click(x, y, MouseAction.LeftClick);
	}

	@DescriptionAttribute(text = Do.clickWithCoorAndAction)
	public Operation click(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "action") MouseAction action)
	{
		this.list.add(new Part(OperationKind.CLICK_XY).setX(x).setY(y).setMouseAction(action));
		return this;
	}

	@DescriptionAttribute(text = Do.text)
	public Operation text(@FieldParameter(name = "str") String str)
	{
		return text(str, false);
	}

	@DescriptionAttribute(text = Do.textWithBool)
	public Operation text(@FieldParameter(name = "str") String str, @FieldParameter(name = "clear") boolean clear)
	{
		this.list.add(new Part(OperationKind.TEXT).setText(str).setBool(clear));
		return this;
	}

	@DescriptionAttribute(text = Do.textColumnRow)
	public Operation text(@FieldParameter(name = "str") String str, @FieldParameter(name = "column") int column, @FieldParameter(name = "row") int row)
	{
		this.list.add(new Part(OperationKind.TEXT_XY).setText(str).setX(column).setY(row));
		return this;
	}

	@DescriptionAttribute(text = Do.toggle)
	public Operation toggle(@FieldParameter(name = "bool") boolean bool)
	{
		this.list.add(new Part(OperationKind.TOGGLE).setBool(bool));
		return this;
	}

	@DescriptionAttribute(text = Do.selectByIndex)
    public Operation select(@FieldParameter(name = "index") int index)
    {
        this.list.add(new Part(OperationKind.SELECT_BY_INDEX).setInt(index));
        return this;
    }

	@DescriptionAttribute(text = Do.select)
    public Operation select(@FieldParameter(name = "selectText") String selectText)
    {
        this.list.add(new Part(OperationKind.SELECT).setText(selectText));
        return this;
    }

	@DescriptionAttribute(text = Do.expand)
	public Operation expand(@FieldParameter(name = "path") String path)
	{
        this.list.add(new Part(OperationKind.EXPAND).setText(path));
        return this;
	}

	@DescriptionAttribute(text = Do.collapse)
	public Operation collapse(@FieldParameter(name = "path") String path)
	{
        this.list.add(new Part(OperationKind.COLLAPSE).setText(path));
        return this;
	}

	public Operation script(@FieldParameter(name = "script") String script)
	{
		this.list.add(new Part(OperationKind.SCRIPT).setText(script));
		return this;
	}

	@DescriptionAttribute(text = Do.dragNdrop)
	public Operation dragNdrop(@FieldParameter(name = "x1") int x1, @FieldParameter(name = "y1") int y1, @FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.Dropped));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.DroppedOwner));
		this.list.add(new Part(OperationKind.DRAG_N_DROP)
				.setX(x1)
				.setY(y1)
				.setX2(x2)
				.setY2(y2)
		);
		return this;
	}

	@DescriptionAttribute(text = Do.dragNdropFromCenterOfElement)
	public Operation dragNdropFromCenterOfElement(@FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.Dropped));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.DroppedOwner));
		this.list.add(new Part(OperationKind.DRAG_N_DROP)
				.setX2(x2)
				.setY2(y2)
		);
		return this;
	}

	@DescriptionAttribute(text = Do.dragNdropCursor)
	public Operation dragNdrop(@FieldParameter(name = "x1") int x1, @FieldParameter(name = "y1") int y1, @FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2, @FieldParameter(name="moveCursor") boolean moveCursor)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.Dropped));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.DroppedOwner));
		this.list.add(new Part(OperationKind.DRAG_N_DROP)
				.setX(x1)
				.setY(y1)
				.setX2(x2)
				.setY2(y2)
				.setBool(moveCursor)
		);
		return this;
	}

	@DescriptionAttribute(text = Do.dragNdropFromCenterOfElementCursor)
	public Operation dragNdropFromCenterOfElement(@FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2, @FieldParameter(name = "moveCursor") boolean moveCursor)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.Dropped));
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(another).setLocatorKind(LocatorKind.DroppedOwner));
		this.list.add(new Part(OperationKind.DRAG_N_DROP)
				.setX2(x2)
				.setY2(y2)
				.setBool(moveCursor)
		);
		return this;
	}

	@DescriptionAttribute(text = Do.scrollToIndex)
	public Operation scrollTo(@FieldParameter(name = "index") int index)
	{
		this.list.add(new Part(OperationKind.SCROLL_TO).setInt(index));
		return this;
	}

	@DescriptionAttribute(text = Do.delay)
	public Operation delay(@FieldParameter(name = "ms") int ms)
	{
		this.list.add(new Part(OperationKind.DELAY).setInt(ms));
		return this;
	}

	@DescriptionAttribute(text = Do.wait)
	public Operation wait(@FieldParameter(name = "str") String str)
	{
		this.list.add(new Part(OperationKind.WAIT).setLocatorId(str).setInt(10000).setToAppear(true).setLocatorKind(LocatorKind.Element));
		return this;
	}

	@DescriptionAttribute(text = Do.wait)
	public Operation wait(@FieldParameter(name = "str") String str, @FieldParameter(name = "ms") int ms, @FieldParameter(name = "toAppear") boolean toAppear)
	{
		this.list.add(new Part(OperationKind.WAIT).setInt(ms).setLocatorId(str).setLocatorKind(LocatorKind.Element).setToAppear(toAppear));
		return this;
	}

	@DescriptionAttribute(text = Do.wait)
	public Operation wait(@FieldParameter(name = "locator") Locator locator, @FieldParameter(name = "ms") int ms, @FieldParameter(name = "toAppear") boolean toAppear)
	{
		this.list.add(new Part(OperationKind.WAIT).setInt(ms).setLocator(locator).setToAppear(toAppear));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorForTableCell)
	public Operation use(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "locator") Locator locator)
	{
		this.list.add(new Part(OperationKind.USE_CELL_COMPONENT).setX(x).setY(y).setLocator(locator));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorIdForTableCell)
	public Operation use(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "locatorId") String locatorId)
	{
		this.list.add(new Part(OperationKind.USE_CELL_COMPONENT).setLocatorKind(LocatorKind.Element).setX(x).setY(y).setLocatorId(locatorId));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorForTableRow)
	public Operation use(@FieldParameter(name = "y") int y, @FieldParameter(name = "locator") Locator locator)
	{
		this.list.add(new Part(OperationKind.USE_CELL_COMPONENT).setY(y).setLocator(locator));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocatorIdForTableRow)
	public Operation use(@FieldParameter(name = "y") int y, @FieldParameter(name = "locatorId") String locatorId)
	{
		this.list.add(new Part(OperationKind.USE_CELL_COMPONENT).setLocatorKind(LocatorKind.Element).setY(y).setLocatorId(locatorId));
		return this;
	}

	protected List<Part> list;
}
