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
import com.exactprosystems.jf.api.common.FieldParameter;

import java.awt.*;
import java.util.List;

/**
 * if use table :
 * 1st parameter (x) - column index
 * 2nd parameter (y) - row index
 */
public class Do
{
	//region Utilities operations
	static final String foreach = "Repeat @operation for every element found by Many locator.";
	@DescriptionAttribute(text = Do.foreach)
	public static Operation foreach(@FieldParameter(name = "operation") Operation operation)
	{
		return new Operation().foreach(operation);
	}

	static final String foreach_max = "Repeat @operation for every element found by Many locator but not more than @max times";
	@DescriptionAttribute(text = Do.foreach_max)
	public static Operation foreach(@FieldParameter(name = "operation") Operation operation, @FieldParameter(name = "max") int max)
	{
		return new Operation().foreach(operation, max);
	}

	static final String repeat = "Repeat @operation @i times";
	@DescriptionAttribute(text = Do.repeat)
	public static Operation repeat(@FieldParameter(name = "i") int i, @FieldParameter(name = "operation") Operation operation)
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
	public static Operation use(@FieldParameter(name = "i") int i)
	{
		return new Operation().use(i);
	}
	//endregion

	//region Checking operations
    static final String isEnabled = "Return true, if current component is enabled.";
    @DescriptionAttribute(text = Do.isEnabled)
    public static Operation isEnabled()
    {
        return new Operation().isEnabled();
    }
	
    static final String isVisible = "Return true, if current component is visible.";
    @DescriptionAttribute(text = Do.isVisible)
    public static Operation isVisible()
    {
        return new Operation().isVisible();
    }
    
    static final String checkList = "Check, that value list of current component equals @list. The item order may be ignored by @ignoreOrder";
    @DescriptionAttribute(text = Do.checkList)
    public static Operation checkList(@FieldParameter(name = "list") List<String> list, @FieldParameter(name = "ignoreOrder") boolean ignoreOrder)
    {
        return new Operation().checkList(list, ignoreOrder);
    }
	
    static final String checkColor = "Check, that color of pixel with coords @x, @y equals @color";
    @DescriptionAttribute(text = Do.checkColor)
    public static Operation checkColor(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "color") Color color)
    {
        return new Operation().checkColor(x, y, color);
    }

    static final String check = "Check, that value of current component equals @word";
	@DescriptionAttribute(text = Do.check)
	public static Operation check(@FieldParameter(name = "word") String word)
	{
		return new Operation().check(word);
	}

	static final String checkWithCoor = "If current component is table and x and y >= 0, then get check, that value of cell with column @x and row @y equals word";
	@DescriptionAttribute(text = Do.checkWithCoor)
	public static Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().check(word, x, y);
	}

	static final String checkWithCoorAndFlag = "If current component is table and x and y >= 0, then get check, that value of cell with column @x and row @y equals word.\nIf @flag equals true and  value don't equals value of cell, will be throw exception";
	@DescriptionAttribute(text = Do.checkWithCoorAndFlag)
	public static Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().check(word, x, y, flag);
	}

	static final String checkWithFlag = "Check, that value of current component equals @word.\n If @flag equals true and @text don't equals value of component, will be throw exception";
	@DescriptionAttribute(text = Do.checkWithFlag)
	public static Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().check(word, flag);
	}

	static final String checkRegexpCoor = "If current component is table and x and y >=0, check, that value of cell with column @x and row @y is matching to @regexp";
	@DescriptionAttribute(text = Do.checkRegexpCoor)
	public static Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().checkRegexp(regexp, x, y);
	}

	static final String checkRegexpCoorAndFlag = "If current component is table and x and y >=0, check, that value of cell with column @x and row @y is matching to @regexp.\nIf @flag equals true and @regexp don't matching value of cell, will be throw exception";
	@DescriptionAttribute(text = Do.checkRegexpCoorAndFlag)
	public static Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().checkRegexp(regexp, x, y, flag);
	}

	static final String checkRegexp = "Check, that value of current component is matching to @regexp";
	@DescriptionAttribute(text = Do.checkRegexp)
	public static Operation checkRegexp(@FieldParameter(name = "regexp") String regexp)
	{
		return new Operation().checkRegexp(regexp);
	}

	static final String checkRegexpWithFlag = "Check, that value of current component is matching @regexp.\n If @flag equals true and @regexp don't matching value of component, will be throw exception";
	@DescriptionAttribute(text = Do.checkRegexpWithFlag)
	public static Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().checkRegexp(regexp, flag);
	}

	static final String checkAttr = "Check, that value of attribute @name of current component equals @value";
	@DescriptionAttribute(text = Do.checkAttr)
	public static Operation checkAttr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value)
	{
		return new Operation().checkAttr(name, value);
	}

	static final String checkAttrWithFlag = "Check, that value of attribute @name of current component equals @value. If @flag is true and @value not equals attribute with name @name, will throw " +
			"exception";
	@DescriptionAttribute(text = Do.checkAttrWithFlag)
	public static Operation checkAttr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().checkAttr(name, value, flag);
	}

	static final String checkAttrRegexp = "Check, that value of attribute @name of current component is matching to @regexp";
	@DescriptionAttribute(text = Do.checkAttrRegexp)
	public static Operation checkAttrRegexp(@FieldParameter(name = "name") String name, @FieldParameter(name = "regexp") String regexp)
	{
		return new Operation().checkAttrRegexp(name, regexp, true);
	}

	static final String checkAttrRegexpWithFlag = "Check, that value of attribute @name of current component is matching to @regexp. If @flag is true and regexp not mathing, will throw exception";
	@DescriptionAttribute(text = Do.checkAttrRegexp)
	public static Operation checkAttrRegexp(@FieldParameter(name = "name") String name, @FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().checkAttrRegexp(name, regexp, flag);
	}
	//endregion

	//region Executing operations
	static final String setValue = "Set @value to component. For example, set progress to any sliders";
	@DescriptionAttribute(text = Do.setValue)
	public static Operation setValue(@FieldParameter(name = "value") double value)
	{
		return new Operation().setValue(value);
	}

    static final String getColor= "Return color for point in coords @x, @y.";
    @DescriptionAttribute(text = Do.getColor)
    public static Operation getColor(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
    {
        return new Operation().getColor(x, y);
    }

	static final String getValue= "Return value from current component.For example, for checkbox this will be true or false (selected or unselected).";
	@DescriptionAttribute(text = Do.getValue)
	public static Operation getValue()
	{
		return new Operation().getValue();
	}

	static final String getValueWithCoor = "If current component is table and x and y >= 0, then get text of table from @x column and @y row";
	@DescriptionAttribute(text = Do.getValueWithCoor)
	public static Operation getValue(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().getValue(x, y);
	}

	static final String getList= "Return values from all items of this list";
	@DescriptionAttribute(text = Do.getList)
	public static Operation getList()
	{
		return new Operation().getList();
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

	static final String sequence = "Converts @text to keypress sequence for current component";
    @DescriptionAttribute(text = Do.sequence)
    public static Operation sequence(@FieldParameter(name = "text") String text)
    {
        return new Operation().sequence(text);
    }

	static final String press = "Press @key on current component. See valid variant of @Keyboard";
	@DescriptionAttribute(text = Do.press)
	public static Operation press(@FieldParameter(name = "key") Keyboard key)
	{
		return new Operation().press(key);
	}

	static final String keyDown = "Press down and hold @key. See valid variant of @Keyboard";
	@DescriptionAttribute(text = Do.keyDown)
	public static Operation keyDown(@FieldParameter(name = "key") Keyboard key)
	{
		return new Operation().keyDown(key);
	}

	static final String keyUp = "Press up and unhold @key. See valid variant of @Keyboard";
	@DescriptionAttribute(text = Do.keyUp)
	public static Operation keyUp(@FieldParameter(name = "key") Keyboard key)
	{
		return new Operation().keyUp(key);
	}

	static final String get = "Get text of current component";
	@DescriptionAttribute(text = Do.get)
	public static Operation get()
	{
		return new Operation().get();
	}

	static final String getAttr = "Get attribute with @name of current component";
	@DescriptionAttribute(text = Do.getAttr)
	public static Operation getAttr(@FieldParameter(name = "name") String name)
	{
		return new Operation().getAttr(name);
	}

	static final String getTable = "Get table of current table component. See @Table";
	@DescriptionAttribute(text = Do.getTable)
	public static Operation getTable()
	{
		return new Operation().getTable();
	}

	static final String getRow = "Return row of current table, that fits @valueCondition and @colorCondition. See @Condition";
	@DescriptionAttribute(text = Do.getRow)
	public static Operation getRow(@FieldParameter(name = "valueCondition") ICondition valueCondition, @FieldParameter(name = "colorCondition") ICondition colorCondition)
	{
		return new Operation().getRow(valueCondition, colorCondition);
	}

	static final String getRowOneCondition = "Return row of current table, that fits @valueCondition. See @Condition";
	@DescriptionAttribute(text = Do.getRowOneCondition)
	public static Operation getRow(@FieldParameter(name = "valueCondition") ICondition valueCondition)
	{
		return new Operation().getRow(valueCondition, null);
	}

	static final String getRowIndexes = "Return indexes of row in current table, that fits @valueCondition and @colorCondition. See @Condition";
	@DescriptionAttribute(text = Do.getRowIndexes)
	public static Operation getRowIndexes(@FieldParameter(name = "valueCondition") ICondition valueCondition, @FieldParameter(name = "colorCondition") ICondition colorCondition)
	{
		return new Operation().getRowIndexes(valueCondition, colorCondition);
	}

	static final String getRowIndexesOneCondition = "Return indexes of row in current table, that fits @valueCondition. See @Condition";
	@DescriptionAttribute(text = Do.getRowIndexesOneCondition)
	public static Operation getRowIndexes(@FieldParameter(name = "valueCondition") ICondition valueCondition)
	{
		return new Operation().getRowIndexes(valueCondition, null);
	}

	static final String getRowByIndex = "Return row with index @index of current table";
	@DescriptionAttribute(text = Do.getRowByIndex)
	public static Operation getRowByIndex(@FieldParameter(name = "index") int index)
	{
		return new Operation().getRowByIndex(index);
	}

	static final String getRowWithColor = "Return row with color, that have index @index of current table";
	@DescriptionAttribute(text = Do.getRowWithColor)
	public static Operation getRowWithColor(@FieldParameter(name = "index") int index)
	{
		return new Operation().getRowWithColor(index);
	}

	static final String getTableSize = "Return size of current table";
	@DescriptionAttribute(text = Do.getRowWithColor)
	public static Operation getTableSize()
	{
		return new Operation().getTableSize();
	}

	static final String move = "Move mouse to current component";
	@DescriptionAttribute(text = Do.move)
	public static Operation move()
	{
		return new Operation().move();
	}

	static final String moveWithCoor = "Mouse mouse to current component with index @x and @y.\nIf current component is table, then move mouse to cell with index @x column and @y row";
	@DescriptionAttribute(text = Do.moveWithCoor)
	public static Operation move(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
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
	public static Operation click(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().click(x, y);
	}

	static final String clickWithCoorAndAction = "Do @action to current component with coordinates @x and @y.\nIf current component is table, then do @action to cell with index @x column and @y row";
	@DescriptionAttribute(text = Do.clickWithCoorAndAction)
	public static Operation click(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "action") MouseAction action)
	{
		return new Operation().click(x, y, action);
	}

	static final String text = "Set @text to current component";
	@DescriptionAttribute(text = Do.text)
	public static Operation text(@FieldParameter(name = "text") String text)
	{
		return new Operation().text(text, false);
	}

	static final String textWithBool = "Set @text to current component. If @clear is true, then before typed text, component will be cleared";
	@DescriptionAttribute(text = Do.textWithBool)
	public static Operation text(@FieldParameter(name = "text") String text, @FieldParameter(name = "clear") boolean clear)
	{
		return new Operation().text(text, clear);
	}

	static final String textColumnRow = "Set @text to cell of table with coordinates @row and @column";
	@DescriptionAttribute(text = Do.text)
	public static Operation text(@FieldParameter(name = "text") String text, @FieldParameter(name = "column") int column, @FieldParameter(name = "row") int row)
	{
		return new Operation().text(text, column, row);
	}

	static final String toggle = "Set current component to state @bool";
	@DescriptionAttribute(text = Do.toggle)
	public static Operation toggle(@FieldParameter(name = "bool") boolean bool)
	{
		return new Operation().toggle(bool);
	}

	static final String selectByIndex = "Select from current component item with index @index";
	@DescriptionAttribute(text = Do.selectByIndex)
	public static Operation select(@FieldParameter(name = "index") int index)
	{
		return new Operation().select(index);
	}

	static final String select = "Select from current component item with text @selectItem";
	@DescriptionAttribute(text = Do.select)
	public static Operation select(@FieldParameter(name = "selectItem") String selectItem)
	{
		return new Operation().select(selectItem);
	}

	static final String expand = "Expand tree by path @path. Use symbol '/' for separate items";
	@DescriptionAttribute(text = Do.expand)
	public static Operation expand(@FieldParameter(name = "path") String path)
	{
		return new Operation().expand(path);
	}

	static final String collapse = "Collapse tree by path @path. Use symbol '/' for separate items";
	@DescriptionAttribute(text = Do.collapse)
	public static Operation collapse(@FieldParameter(name = "path") String path)
	{
		return new Operation().collapse(path);
	}

	public static Operation script(@FieldParameter(name = "script") String script)
	{
		return new Operation().script(script);
	}

	static final String dragNdropCursor = "DragNdrop method, where starting coordinates are x1 and x2 inside current element and  ending coordinates are x2 and y2 inside another element. Method has parameter for physically moving cursor.";
	@DescriptionAttribute(text = Do.dragNdropCursor)
	public static Operation dragNdrop(@FieldParameter(name = "x1") int x1, @FieldParameter(name = "y1") int y1, @FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2, @FieldParameter(name="moveCursor") boolean moveCursor)
	{
		return new Operation().dragNdrop(x1, y1, another, x2, y2, moveCursor);
	}

	static final String dragNdrop = "DragNdrop method, where starting coordinates are x1 and x2 inside current element and  ending coordinates are x2 and y2 inside another element.";
	@DescriptionAttribute(text = Do.dragNdrop)
	public static Operation dragNdrop(@FieldParameter(name = "x1") int x1, @FieldParameter(name = "y1") int y1, @FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2)
	{
		return new Operation().dragNdrop(x1, y1, another, x2, y2);
	}

	static final String dragNdropFromCenterOfElementCursor = "DragNdrop method, where starting coordinates is a center of current element and ending coordinates are x2 and y2 inside another element. Method has parameter for physically moving cursor.";
	@DescriptionAttribute(text = Do.dragNdropFromCenterOfElementCursor)
	public static Operation dragNdrop(@FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2, @FieldParameter(name = "moveCursor") boolean moveCursor)
	{
		return new Operation().dragNdropFromCenterOfElement(another, x2, y2, moveCursor);
	}

	static final String dragNdropFromCenterOfElement = "DragNdrop method, where starting coordinates is a center of current element and ending coordinates are x2 and y2 inside another element";
	@DescriptionAttribute(text = Do.dragNdropFromCenterOfElement)
	public static Operation dragNdrop(@FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2)
	{
		return new Operation().dragNdropFromCenterOfElement(another, x2, y2);
	}
	//endregion

	//region Operations with locators
	static final String useLocatorId = "Change context to locator with id @locator";
	@DescriptionAttribute(text = Do.useLocatorId)
	public static Operation use(@FieldParameter(name = "locator") String locator)
	{
		return new Operation().use(locator);
	}

	static final String useLocator = "Change context to locator with dynamic @locator";
	@DescriptionAttribute(text = Do.useLocator)
	public static Operation use(@FieldParameter(name = "locator") Locator locator)
	{
		return new Operation().use(locator);
	}

	static final String useLocatorKind = "Change context to locator with dynamic @locator and locator kind @locatorKind";
	@DescriptionAttribute(text = Do.useLocator)
	public static Operation use(@FieldParameter(name = "locator") Locator locator, @FieldParameter(name = "locatorKind") LocatorKind locatorKind)
	{
		return new Operation().use(locator, locatorKind);
	}

	static final String useLocatorIdKind = "Change context to locator with id @locatorId and locator kind @locatorKind";
	@DescriptionAttribute(text = Do.useLocator)
	public static Operation use(@FieldParameter(name = "locatorId") String locatorId, @FieldParameter(name = "locatorKind") LocatorKind locatorKind)
	{
		return new Operation().use(locatorId, locatorKind);
	}
	//endregion

	//region Time operations
	static final String delay = "Delay @ms";
	@DescriptionAttribute(text = Do.delay)
	public static Operation delay(@FieldParameter(name = "ms") int ms)
	{
		return new Operation().delay(ms);
	}

	static final String wait = "Wait, while component stay @toAppear ? visible : invisible for @ms";
	@DescriptionAttribute(text = Do.wait)
	public static Operation wait(@FieldParameter(name = "str") String str)
	{
		return new Operation().wait(str);
	}

	@DescriptionAttribute(text = Do.wait)
	public static Operation wait(@FieldParameter(name = "str") String str, @FieldParameter(name = "ms") int ms, @FieldParameter(name = "toAppear") boolean toAppear)
	{
		return new Operation().wait(str, ms, toAppear);
	}

	@DescriptionAttribute(text = Do.wait)
	public static Operation wait(@FieldParameter(name = "locator") Locator locator, @FieldParameter(name = "ms") int ms, @FieldParameter(name = "toAppear") boolean toAppear)
	{
		return new Operation().wait(locator, ms, toAppear);
	}
	//endregion
}
