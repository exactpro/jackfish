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

/**
 * if use table :
 * 1st parameter (x) - column index
 * 2nd parameter (y) - row index
 */
public class Do
{
	static final String foreach = "Repeat @operation for every element found by Many locator.";
	@DescriptionAttribute(text = Do.foreach)
	public static Operation foreach(Operation operation)
	{
		return new Operation().foreach(operation);
	}
	
	static final String foreach_max = "Repeat @operation for every element found by Many locator but not more than @max times";
	@DescriptionAttribute(text = Do.foreach_max)
	public static Operation foreach(Operation operation, int max)
	{
		return new Operation().foreach(operation, max);
	}

	static final String repeat = "Repeat @operation @i times";
	@DescriptionAttribute(text = Do.repeat)
	public static Operation repeat(int i, Operation operation)
	{
		return new Operation().repeat(i, operation);
	}

	static final String count = "Return count of objects ( if element have attribute addition Many)";
	@DescriptionAttribute(text = Do.count)
	public static Operation count()
	{
		return new Operation().count();
	}

	static final String use = "Use element with index @i. (if element have attribute addition Many)";
	@DescriptionAttribute(text = Do.use)
	public static Operation use(int i)
	{
		return new Operation().use(i);
	}

	static final String setValue = "Set @value to component. For example, set progress to any sliders";
	@DescriptionAttribute(text = Do.setValue)
	public static Operation setValue(double value)
	{
		return new Operation().setValue(value);
	}

	static final String getValue= "Return value from current component.For example, for checkbox this will be true or false (selected or unselected).";
	@DescriptionAttribute(text = Do.getValue)
	public static Operation getValue()
	{
		return new Operation().getValue();
	}

	static final String getRectangle= "Return rectangle in which current component is placed.";
	@DescriptionAttribute(text = Do.getRectangle)
	public static Operation getRectangle()
	{
		return new Operation().getRectangle();
	}

	static final String push = "Push to the current component";
	@DescriptionAttribute(text = Do.push)
	public static Operation push()
	{
		return new Operation().push();
	}

	static final String press = "Press @key on current component. See valid variant of @Keyboard";
	@DescriptionAttribute(text = Do.press)
	public static Operation press(Keyboard key)
	{
		return new Operation().press(key);
	}

	static final String keyDown = "Press down and hold @key. See valid variant of @Keyboard";
	@DescriptionAttribute(text = Do.keyDown)
	public static Operation keyDown(Keyboard key)
	{
		return new Operation().keyDown(key);
	}

	static final String keyUp = "Press up and unhold @key. See valid variant of @Keyboard";
	@DescriptionAttribute(text = Do.keyUp)
	public static Operation keyUp(Keyboard key)
	{
		return new Operation().keyUp(key);
	}

	static final String check = "Check, that value of current component equals @word";
	@DescriptionAttribute(text = Do.check)
	public static Operation check(String word)
	{
		return new Operation().check(word);
	}

	static final String checkWithCoor = "If current component is table and x and y >= 0, then get check, that value of cell with column @x and row @y equals word";
	@DescriptionAttribute(text = Do.checkWithCoor)
	public static Operation check(String word, int x, int y)
	{
		return new Operation().check(word, x, y);
	}

	static final String checkWithCoorAndFlag = "If current component is table and x and y >= 0, then get check, that value of cell with column @x and row @y equals word.\nIf @flag equals true and  value don't equals value of cell, will be throw exception";
	@DescriptionAttribute(text = Do.checkWithCoorAndFlag)
	public static Operation check(String word, int x, int y, boolean flag)
	{
		return new Operation().check(word, x, y, flag);
	}

	static final String checkWithFlag = "Check, that value of current component equals @word.\n If @flag equals true and @text don't equals value of component, will be throw exception";
	@DescriptionAttribute(text = Do.checkWithFlag)
	public static Operation check(String word, boolean flag)
	{
		return new Operation().check(word, flag);
	}

	static final String checkRegexpCoor = "If current component is table and x and y >=0, check, that value of cell with column @x and row @y is matching to @regexp";
	@DescriptionAttribute(text = Do.checkRegexpCoor)
	public static Operation checkRegexp(String regexp, int x, int y)
	{
		return new Operation().checkRegexp(regexp, x, y);
	}

	static final String checkRegexpCoorAndFlag = "If current component is table and x and y >=0, check, that value of cell with column @x and row @y is matching to @regexp.\nIf @flag equals true and @regexp don't matching value of cell, will be throw exception";
	@DescriptionAttribute(text = Do.checkRegexpCoorAndFlag)
	public static Operation checkRegexp(String regexp, int x, int y, boolean flag)
	{
		return new Operation().checkRegexp(regexp, x, y, flag);
	}

	static final String checkRegexp = "Check, that value of current component is matching to @regexp";
	@DescriptionAttribute(text = Do.checkRegexp)
	public static Operation checkRegexp(String regexp)
	{
		return new Operation().checkRegexp(regexp);
	}

	static final String checkRegexpWithFlag = "Check, that value of current component is matching @regexp.\n If @flag equals true and @regexp don't matching value of component, will be throw exception";
	@DescriptionAttribute(text = Do.checkRegexpWithFlag)
	public static Operation checkRegexp(String regexp, boolean flag)
	{
		return new Operation().checkRegexp(regexp, flag);
	}

	static final String checkAttr = "Check, that value of attribute @name of current component equals @value";
	@DescriptionAttribute(text = Do.checkAttr)
	public static Operation checkAttr(String name, String value)
	{
		return new Operation().checkAttr(name, value);
	}

	static final String checkAttrRegexp = "Check, that value of attribute @name of current component is matching to @regexp";
	@DescriptionAttribute(text = Do.checkAttrRegexp)
	public static Operation checkAttrRegexp(String name, String regexp)
	{
		return new Operation().checkAttrRegexp(name, regexp);
	}
	
	static final String get = "Get text of current component";
	@DescriptionAttribute(text = Do.get)
	public static Operation get()
	{
		return new Operation().get();
	}

	static final String getAttr = "Get attribute with @name of current component";
	@DescriptionAttribute(text = Do.getAttr)
	public static Operation getAttr(String name)
	{
		return new Operation().getAttr(name);
	}

	static final String getValueWithCoor = "If current component is table and x and y >= 0, then get text of table from @x column and @y row";
	@DescriptionAttribute(text = Do.getValueWithCoor)
	public static Operation getValue(int x, int y)
	{
		return new Operation().getValue(x, y);
	}

	static final String getTable = "Get table of current table component. See @Table";
	@DescriptionAttribute(text = Do.getTable)
	public static Operation getTable()
	{
		return new Operation().getTable();
	}

	static final String getRow = "Return row of current table, that fits @valueCondition and @colorCondition. See @Condition";
	@DescriptionAttribute(text = Do.getRow)
	public static Operation getRow(ICondition valueCondition, ICondition colorCondition)
	{
		return new Operation().getRow(valueCondition, colorCondition);
	}
	
	static final String getRowIndexes = "Return indexes of row in current table, that fits @valueCondition and @colorCondition. See @Condition";
	@DescriptionAttribute(text = Do.getRowIndexes)
	public static Operation getRowIndexes(ICondition valueCondition, ICondition colorCondition)
	{
		return new Operation().getRowIndexes(valueCondition, colorCondition);
	}

	static final String getRowByIndex = "Return row with index @index of current table";
	@DescriptionAttribute(text = Do.getRowByIndex)
	public static Operation getRowByIndex(int index)
	{
		return new Operation().getRowByIndex(index);
	}

	static final String getRowWithColor = "Return row with color, that have index @index of current table";
	@DescriptionAttribute(text = Do.getRowWithColor)
	public static Operation getRowWithColor(int index)
	{
		return new Operation().getRowWithColor(index);
	}

	static final String useLocatorId = "Change context to locator with id @locator";
	@DescriptionAttribute(text = Do.useLocatorId)
	public static Operation use(String locator)
	{
		return new Operation().use(locator);
	}

	static final String useLocator = "Change context to locator with dynamic @locator";
	@DescriptionAttribute(text = Do.useLocator)
	public static Operation use(Locator locator)
	{
		return new Operation().use(locator);
	}

	static final String useLocatorKind = "Change context to locator with dynamic @locator and locator kind @locatorKind";
	@DescriptionAttribute(text = Do.useLocator)
	public static Operation use(Locator locator, LocatorKind locatorKind)
	{
		return new Operation().use(locator, locatorKind);
	}

	static final String move = "Move mouse to current component";
	@DescriptionAttribute(text = Do.move)
	public static Operation move()
	{
		return new Operation().move();
	}

	static final String moveWithCoor = "Mouse mouse to current component with index @x and @y.\nIf current component is table, then move mouse to cell with index @x column and @y row";
	@DescriptionAttribute(text = Do.moveWithCoor)
	public static Operation move(int x, int y)
	{
		return new Operation().move(x, y);
	}

	static final String click = "Click to current component";
	@DescriptionAttribute(text = Do.click)
	public static Operation click()
	{
		return new Operation().click();
	}

	static final String clickWithCoor = "Click to current component with coordinates @x and @y.\nIf current component is table, then click to cell with index @x column and @y row";
	@DescriptionAttribute(text = Do.clickWithCoor)
	public static Operation click(int x, int y)
	{
		return new Operation().click(x, y);
	}

	static final String clickWithCoorAndAction = "Do @action to current component with coordinates @x and @y.\nIf current component is table, then do @action to cell with index @x column and @y row";
	@DescriptionAttribute(text = Do.clickWithCoorAndAction)
	public static Operation click(int x, int y, MouseAction action)
	{
		return new Operation().click(x, y, action);
	}

	static final String text = "Set @text to current component";
	@DescriptionAttribute(text = Do.text)
	public static Operation text(String text)
	{
		return new Operation().text(text, false);
	}

	static final String textWithBool = "Set @text to current component. If @clear is true, then before typed text, component will be cleared";
	@DescriptionAttribute(text = Do.textWithBool)
	public static Operation text(String text, boolean clear)
	{
		return new Operation().text(text, clear);
	}

	static final String textColumnRow = "Set @text to cell of table with coordinates @row and @column";
	@DescriptionAttribute(text = Do.text)
	public static Operation text(String text, int column, int row)
	{
		return new Operation().text(text, column, row);
	}

	static final String toggle = "Set current component to state @bool";
	@DescriptionAttribute(text = Do.toggle)
	public static Operation toggle(boolean bool)
	{
		return new Operation().toggle(bool);
	}

	static final String select = "Select from current component item with text @selectItem";
	@DescriptionAttribute(text = Do.select)
	public static Operation select(String selectItem)
	{
		return new Operation().select(selectItem);
	}

	static final String expand = "Expand tree by path @path. Use symbol '/' for separate items";
	@DescriptionAttribute(text = Do.expand)
	public static Operation expand(String path)
	{
		return new Operation().expand(path);
	}

	static final String collapse = "Collapse tree by path @path. Use symbol '/' for separate items";
	@DescriptionAttribute(text = Do.collapse)
	public static Operation collapse(String path)
	{
		return new Operation().collapse(path);
	}

	static final String delay = "Delay @ms";
	@DescriptionAttribute(text = Do.delay)
	public static Operation delay(int ms)
	{
		return new Operation().delay(ms);
	}

	static final String wait = "Wait, while component stay @toAppear ? visible : invisible for @ms";
	@DescriptionAttribute(text = Do.wait)
	public static Operation wait(String str)
	{
		return new Operation().wait(str);
	}

	@DescriptionAttribute(text = Do.wait)
	public static Operation wait(String str, int ms, boolean toAppear)
	{
		return new Operation().wait(str, ms, toAppear);
	}

	@DescriptionAttribute(text = Do.wait)
	public static Operation wait(Locator locator, int ms, boolean toAppear)
	{
		return new Operation().wait(locator, ms, toAppear);
	}
}
