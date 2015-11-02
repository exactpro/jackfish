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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
		return getClass().getSimpleName() + Arrays.toString(this.list.toArray());
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


	public <T> void operate(OperationExecutor<T> executor, Locator element, T main) throws Exception
	{
		OperationResult result = new OperationResult();
		Holder<T> holder = new Holder<T>(main);

		for (Part part : this.list)
		{
			processOnePart(executor, null, holder, element, null, null, result, part);
		}
	}

	public <T> OperationResult operate(OperationExecutor<T> executor, Locator owner, Locator element, Locator rows, Locator header) throws Exception
	{
		OperationResult result = new OperationResult();
		T main = null;
		List<T> list = null;
		
		owner 	= this.owner   == null ? owner   : this.owner;
		element = this.locator == null ? element : this.locator;
		rows 	= this.rows    == null ? rows    : this.rows;
		header 	= this.header  == null ? header  : this.header;

		if (element.getAddition() == Addition.Many)
		{
			T dialog = null;
			if (isReal(owner))
			{
				dialog = executor.find(null, owner);
			}
			
			if (isReal(element))
			{
				list = executor.findAll(element.getControlKind(), dialog, element);
			}
			main = list == null || list.size() == 0 ? null : list.get(0);
		}
		else
		{
			if (isReal(element))
			{
				main = executor.find(owner, element);
			}
		}

		boolean ok = false;
		
		Holder<T> holder = new Holder<>(main);
		for (Part part : this.list)
		{
			ok = processOnePart(executor, list, holder, element, rows, header, result, part);
			if (!ok)
			{
				break;
			}
		}

		result.setOk(ok);
		return result;
	}

	private <T> boolean processOnePart(OperationExecutor<T> executor, List<T> list, Holder<T> holder, Locator element, 
			Locator rows, Locator header, OperationResult result, Part part) throws Exception
	{
		T component = holder.value;
		Locator locator = element;
		ControlKind controlKind = element.getControlKind();
		
		boolean haveX = part.x >= 0;
		boolean haveY = part.y >= 0;
		boolean haveOneOfCoords = haveX || haveY;
		if (this.locator != null)
		{
			component = null;
			locator = this.locator;
			if (isReal(locator))
			{
				component = executor.find(this.owner, locator);
			}
			holder.value = component;
			controlKind = locator.getControlKind();
		}
		else if (part.element != null)
		{
			component = null;
			locator = part.element;
			if (isReal(part.element) && part.kind != OperationKind.WAIT )
			{
				component = executor.find(part.owner, part.element);
			}
			holder.value = component;
		}

		if (controlKind == ControlKind.Table && executor.tableIsContainer() && haveOneOfCoords)
		{
			component = executor.lookAtTable(component, rows, header, part.x, part.y);
			holder.value = component;
			part.x = Integer.MIN_VALUE;
			part.y = Integer.MIN_VALUE;
		}
		
		// check permitions for this part
		OperationKind kind = part.kind;
		
		
		if (!locator.getControlKind().isAllowed(kind))
		{
			throw new Exception("Operation " + kind + " is not allowed for " + locator.getControlKind());
		}
		
		return kind.operate(part, executor, locator, rows, header, list, component, holder, result);
	}

	private static boolean isReal(Locator locator)
	{
		return locator != null && locator.getControlKind() != null && !locator.getControlKind().isVirtual();
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
	
	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}
	
	public Part addPart(OperationKind kind)
	{
		Part part = new Part(kind);
		this.list.add(part);
		return part;
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

	@DescriptionAttribute(text = Do.useLocatorId)
	public Operation use(String locator)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocatorId(locator));
		return this;
	}

	@DescriptionAttribute(text = Do.useLocator)
	public Operation use(Locator locator)
	{
		this.list.add(new Part(OperationKind.USE_LOCATOR).setLocator(locator));
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

	@DescriptionAttribute(text = Do.mark)
    public Operation mark()
    {
        this.list.add(new Part(OperationKind.MARK));
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


	
	
	
	
	
	@DescriptionAttribute(text = Do.locatorOwnerLocator)
	public Operation locator(Locator owner, Locator locator)
	{
		this.owner = owner;
		this.locator = locator;
		return this;
	}

	@DescriptionAttribute(text = Do.locatorOwnerLocatorAddition)
	public Operation locator(Locator owner, Locator locator, Locator addition)
	{
		this.owner = owner;
		this.locator = locator;
		this.rows = addition;
		return this;
	}

	@DescriptionAttribute(text = Do.locatorOwnerLocatorAdditionHeader)
	public Operation locator(Locator owner, Locator locator, Locator addition, Locator header)
	{
		this.owner = owner;
		this.locator = locator;
		this.rows = addition;
		this.header = header;
		return this;
	}

	
	
	
	
	
	
	@DescriptionAttribute(text = Do.owner)
	public Locator owner(String id, ControlKind kind)
	{
		this.owner = new Locator(this, id, kind); 
		return this.owner;
	}
	
	@DescriptionAttribute(text = Do.locator)
	public Locator locator(String id, ControlKind kind)
	{
		this.locator = new Locator(this, id, kind);
		return this.locator;
	}

	@DescriptionAttribute(text = Do.addition)
	public Locator addition(String id, ControlKind kind)
	{
		this.rows = new Locator(this, id, kind);
		return this.rows;
	}

	@DescriptionAttribute(text = Do.header)
	public Locator header(String id, ControlKind kind)
	{
		this.header = new Locator(this, id, kind);
		return this.header;
	}
	
	protected List<Part> list;
	protected Locator locator;
	protected Locator owner;
	protected Locator rows;
	protected Locator header;
}
