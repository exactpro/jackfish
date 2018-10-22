/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.FieldParameter;
import com.exactprosystems.jf.api.common.i18n.R;

import java.awt.*;
import java.util.List;

/**
 * if use table :
 * 1st parameter (x) - column index
 * 2nd parameter (y) - row index
 */

@DescriptionAttribute(text = R.DO_DESCRIPTION)
public class Do
{
	//region Utilities operations
	@DescriptionAttribute(text = R.DO_FOREACH)
	public static Operation foreach(@FieldParameter(name = "operation") Operation operation)
	{
		return new Operation().foreach(operation);
	}

	@DescriptionAttribute(text = R.DO_FOREACH_MAX)
	public static Operation foreach(@FieldParameter(name = "operation") Operation operation, @FieldParameter(name = "max") int max)
	{
		return new Operation().foreach(operation, max);
	}

	@DescriptionAttribute(text = R.DO_REPEAT)
	public static Operation repeat(@FieldParameter(name = "i") int i, @FieldParameter(name = "operation") Operation operation)
	{
		return new Operation().repeat(i, operation);
	}

	@DescriptionAttribute(text = R.DO_COUNT)
	public static Operation count()
	{
		return new Operation().count();
	}

	@DescriptionAttribute(text = R.DO_USE)
	public static Operation use(@FieldParameter(name = "i") int i)
	{
		return new Operation().use(i);
	}

	@DescriptionAttribute(text = R.DO_APPLY)
	public static Operation apply(@FieldParameter(name = "path") String path, @FieldParameter(name = "operation") Operation operation)
	{
		return new Operation().apply(path, operation);
	}
	//endregion

	//region Checking operations
    @DescriptionAttribute(text = R.DO_IS_ENABLED)
    public static Operation isEnabled()
    {
        return new Operation().isEnabled();
    }
	
    @DescriptionAttribute(text = R.DO_IS_VISIBLE)
    public static Operation isVisible()
    {
        return new Operation().isVisible();
    }
    
    @DescriptionAttribute(text = R.DO_CHECK_LIST)
    public static Operation checkList(@FieldParameter(name = "list") List<String> list, @FieldParameter(name = "ignoreOrder") boolean ignoreOrder)
    {
        return new Operation().checkList(list, ignoreOrder);
    }
	
    @DescriptionAttribute(text = R.DO_CHECK_COLOR)
    public static Operation checkColor(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "color") Color color)
    {
        return new Operation().checkColor(x, y, color);
    }

	@DescriptionAttribute(text = R.DO_CHECK)
	public static Operation check(@FieldParameter(name = "word") String word)
	{
		return new Operation().check(word);
	}

	@DescriptionAttribute(text = R.DO_CHECK_WITH_COOR)
	public static Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().check(word, x, y);
	}

	@DescriptionAttribute(text = R.DO_CHECK_WITH_COOR_AND_FLAG)
	public static Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().check(word, x, y, flag);
	}

	@DescriptionAttribute(text = R.DO_CHECK_WITH_FLAG)
	public static Operation check(@FieldParameter(name = "word") String word, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().check(word, flag);
	}

	@DescriptionAttribute(text = R.DO_CHECK_REGEXP_COOR)
	public static Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().checkRegexp(regexp, x, y);
	}

	@DescriptionAttribute(text = R.DO_CHECK_REGEXP_COOR_AND_FLAG)
	public static Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().checkRegexp(regexp, x, y, flag);
	}

	@DescriptionAttribute(text = R.DO_CHECK_REGEXP)
	public static Operation checkRegexp(@FieldParameter(name = "regexp") String regexp)
	{
		return new Operation().checkRegexp(regexp);
	}

	@DescriptionAttribute(text = R.DO_CHECK_REGEXP_WITH_FLAG)
	public static Operation checkRegexp(@FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().checkRegexp(regexp, flag);
	}

	@DescriptionAttribute(text = R.DO_CHECK_ATTR)
	public static Operation checkAttr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value)
	{
		return new Operation().checkAttr(name, value);
	}

	@DescriptionAttribute(text = R.DO_CHECK_ATTR_WITH_FLAG)
	public static Operation checkAttr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().checkAttr(name, value, flag);
	}

	static final String checkAttrRegexp = R.DO_CHECK_ATTR_REGEXP.get();
	@DescriptionAttribute(text = R.DO_CHECK_ATTR_REGEXP)
	public static Operation checkAttrRegexp(@FieldParameter(name = "name") String name, @FieldParameter(name = "regexp") String regexp)
	{
		return new Operation().checkAttrRegexp(name, regexp, true);
	}

	@DescriptionAttribute(text = R.DO_CHECK_ATTR_REGEXP_WITH_FLAG)
	public static Operation checkAttrRegexp(@FieldParameter(name = "name") String name, @FieldParameter(name = "regexp") String regexp, @FieldParameter(name = "flag") boolean flag)
	{
		return new Operation().checkAttrRegexp(name, regexp, flag);
	}
	//endregion

	//region Executing operations
	@DescriptionAttribute(text = R.DO_SET_VALUE)
	public static Operation setValue(@FieldParameter(name = "value") double value)
	{
		return new Operation().setValue(value);
	}

    @DescriptionAttribute(text = R.DO_GET_COLOR)
    public static Operation getColor(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
    {
        return new Operation().getColor(x, y);
    }

	@DescriptionAttribute(text = R.DO_GET_VALUE)
	public static Operation getValue()
	{
		return new Operation().getValue();
	}

	@DescriptionAttribute(text = R.DO_GET_VALUE_WITH_COOR)
	public static Operation getValue(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().getValue(x, y);
	}

	@DescriptionAttribute(text = R.DO_GET_LIST)
	public static Operation getList()
	{
		return new Operation().getList();
	}

	@DescriptionAttribute(text = R.DO_GET_RECTANGLE)
	public static Operation getRectangle()
	{
		return new Operation().getRectangle();
	}

	@DescriptionAttribute(text = R.DO_PUSH)
	public static Operation push()
	{
		return new Operation().push();
	}

    @DescriptionAttribute(text = R.DO_SEQUENCE)
    public static Operation sequence(@FieldParameter(name = "text") String text)
    {
        return new Operation().sequence(text);
    }

	@DescriptionAttribute(text = R.DO_PRESS)
	public static Operation press(@FieldParameter(name = "key") Keyboard key)
	{
		return new Operation().press(key);
	}

	@DescriptionAttribute(text = R.DO_KEY_DOWN)
	public static Operation keyDown(@FieldParameter(name = "key") Keyboard key)
	{
		return new Operation().keyDown(key);
	}

	@DescriptionAttribute(text = R.DO_KEY_UP)
	public static Operation keyUp(@FieldParameter(name = "key") Keyboard key)
	{
		return new Operation().keyUp(key);
	}

	@DescriptionAttribute(text = R.DO_GET)
	public static Operation get()
	{
		return new Operation().get();
	}

	@DescriptionAttribute(text = R.DO_GET_ATTR)
	public static Operation getAttr(@FieldParameter(name = "name") String name)
	{
		return new Operation().getAttr(name);
	}

	@DescriptionAttribute(text = R.DO_GET_TREE)
	public static Operation getTree()
	{
		return new Operation().getTree();
	}

	@DescriptionAttribute(text = R.DO_GET_TABLE)
	public static Operation getTable()
	{
		return new Operation().getTable();
	}

	@DescriptionAttribute(text = R.DO_GET_ROW)
	public static Operation getRow(@FieldParameter(name = "valueCondition") ICondition valueCondition, @FieldParameter(name = "colorCondition") ICondition colorCondition)
	{
		return new Operation().getRow(valueCondition, colorCondition);
	}

	@DescriptionAttribute(text = R.DO_GET_ROW_ONE_CONDITION)
	public static Operation getRow(@FieldParameter(name = "valueCondition") ICondition valueCondition)
	{
		return new Operation().getRow(valueCondition, null);
	}

	@DescriptionAttribute(text = R.DO_GET_ROW_INDEXES)
	public static Operation getRowIndexes(@FieldParameter(name = "valueCondition") ICondition valueCondition, @FieldParameter(name = "colorCondition") ICondition colorCondition)
	{
		return new Operation().getRowIndexes(valueCondition, colorCondition);
	}

	@DescriptionAttribute(text = R.DO_GET_ROW_INDEXES_ONE_CONDITION)
	public static Operation getRowIndexes(@FieldParameter(name = "valueCondition") ICondition valueCondition)
	{
		return new Operation().getRowIndexes(valueCondition, null);
	}

	@DescriptionAttribute(text = R.DO_GET_ROW_BY_INDEX)
	public static Operation getRowByIndex(@FieldParameter(name = "index") int index)
	{
		return new Operation().getRowByIndex(index);
	}

	@DescriptionAttribute(text = R.DO_GET_ROW_WITH_COLOR)
	public static Operation getRowWithColor(@FieldParameter(name = "index") int index)
	{
		return new Operation().getRowWithColor(index);
	}

	@DescriptionAttribute(text = R.DO_GET_TABLE_SIZE)
	public static Operation getTableSize()
	{
		return new Operation().getTableSize();
	}

	@DescriptionAttribute(text = R.DO_MOVE)
	public static Operation move()
	{
		return new Operation().move();
	}

	@DescriptionAttribute(text = R.DO_MOVE_WITH_COOR)
	public static Operation move(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().move(x, y);
	}

	@DescriptionAttribute(text = R.DO_CLICK)
	public static Operation click()
	{
		return new Operation().click();
	}

	@DescriptionAttribute(text = R.DO_CLICK_WITH_ACTION)
	public static Operation click(@FieldParameter(name = "action") MouseAction action)
	{
		return new Operation().click(action);
	}

	@DescriptionAttribute(text = R.DO_CLICK_WITH_COOR)
	public static Operation click(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y)
	{
		return new Operation().click(x, y);
	}

	@DescriptionAttribute(text = R.DO_CLICK_WITH_COOR_AND_ACTION)
	public static Operation click(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "action") MouseAction action)
	{
		return new Operation().click(x, y, action);
	}

	@DescriptionAttribute(text = R.DO_TEXT)
	public static Operation text(@FieldParameter(name = "text") String text)
	{
		return new Operation().text(text, false);
	}

	@DescriptionAttribute(text = R.DO_TEXT_WITH_BOOL)
	public static Operation text(@FieldParameter(name = "text") String text, @FieldParameter(name = "clear") boolean clear)
	{
		return new Operation().text(text, clear);
	}

	@DescriptionAttribute(text = R.DO_TEXT_COLUMN_ROW)
	public static Operation text(@FieldParameter(name = "text") String text, @FieldParameter(name = "column") int column, @FieldParameter(name = "row") int row)
	{
		return new Operation().text(text, column, row);
	}

	@DescriptionAttribute(text = R.DO_TOGGLE)
	public static Operation toggle(@FieldParameter(name = "bool") boolean bool)
	{
		return new Operation().toggle(bool);
	}

	@DescriptionAttribute(text = R.DO_SELECT_BY_INDEX)
	public static Operation select(@FieldParameter(name = "index") int index)
	{
		return new Operation().select(index);
	}

	@DescriptionAttribute(text = R.DO_SELECT)
	public static Operation select(@FieldParameter(name = "selectItem") String selectItem)
	{
		return new Operation().select(selectItem);
	}

	@DescriptionAttribute(text = R.DO_EXPAND)
	public static Operation expand(@FieldParameter(name = "path") String path)
	{
		return new Operation().expand(path);
	}

	@DescriptionAttribute(text = R.DO_COLLAPSE)
	public static Operation collapse(@FieldParameter(name = "path") String path)
	{
		return new Operation().collapse(path);
	}

	public static Operation script(@FieldParameter(name = "script") String script)
	{
		return new Operation().script(script);
	}

	@DescriptionAttribute(text = R.DO_DRAG_N_DROP_CURSOR)
	public static Operation dragNdrop(@FieldParameter(name = "x1") int x1, @FieldParameter(name = "y1") int y1, @FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2, @FieldParameter(name="moveCursor") boolean moveCursor)
	{
		return new Operation().dragNdrop(x1, y1, another, x2, y2, moveCursor);
	}

	@DescriptionAttribute(text = R.DO_DRAG_N_DROP)
	public static Operation dragNdrop(@FieldParameter(name = "x1") int x1, @FieldParameter(name = "y1") int y1, @FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2)
	{
		return new Operation().dragNdrop(x1, y1, another, x2, y2);
	}

	@DescriptionAttribute(text = R.DO_DRAG_N_DROP_CENTER_OF_ELEMENT_CURSOR)
	public static Operation dragNdrop(@FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2, @FieldParameter(name = "moveCursor") boolean moveCursor)
	{
		return new Operation().dragNdropFromCenterOfElement(another, x2, y2, moveCursor);
	}

	@DescriptionAttribute(text = R.DO_DRAG_N_DROP_CENTER_OF_ELEMENT)
	public static Operation dragNdrop(@FieldParameter(name = "another") String another, @FieldParameter(name = "x2") int x2, @FieldParameter(name = "y2") int y2)
	{
		return new Operation().dragNdropFromCenterOfElement(another, x2, y2);
	}

	@DescriptionAttribute(text = R.DO_SCROLL_TO_INDEX)
	public static Operation scrollTo(@FieldParameter(name = "index") int index)
	{
		return new Operation().scrollTo(index);
	}
	//endregion

	//region Operations with locators
	@DescriptionAttribute(text = R.DO_USE_LOCATOR_ID)
	public static Operation use(@FieldParameter(name = "locator") String locator)
	{
		return new Operation().use(locator);
	}

	@DescriptionAttribute(text = R.DO_USE_LOCATOR)
	public static Operation use(@FieldParameter(name = "locator") Locator locator)
	{
		return new Operation().use(locator);
	}

	@DescriptionAttribute(text = R.DO_USE_LOCATOR_KIND)
	public static Operation use(@FieldParameter(name = "locator") Locator locator, @FieldParameter(name = "locatorKind") LocatorKind locatorKind)
	{
		return new Operation().use(locator, locatorKind);
	}

	@DescriptionAttribute(text = R.DO_USE_LOCATOR_ID_KIND)
	public static Operation use(@FieldParameter(name = "locatorId") String locatorId, @FieldParameter(name = "locatorKind") LocatorKind locatorKind)
	{
		return new Operation().use(locatorId, locatorKind);
	}

	@DescriptionAttribute(text = R.DO_USE_LOCATOR_FOR_TABLE_CELL)
	public static Operation use(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "locator") Locator locator)
	{
		return new Operation().use(x, y, locator);
	}

	@DescriptionAttribute(text = R.DO_USE_LOCATOR_ID_FOR_TABLE_CELL)
	public static Operation use(@FieldParameter(name = "x") int x, @FieldParameter(name = "y") int y, @FieldParameter(name = "locatorId") String locatorId)
	{
		return new Operation().use(x, y, locatorId);
	}

	@DescriptionAttribute(text = R.DO_USE_LOCATOR_FOR_TABLE_ROW)
	public static Operation use(@FieldParameter(name = "y") int y, @FieldParameter(name = "locator") Locator locator)
	{
		return new Operation().use(y, locator);
	}

	@DescriptionAttribute(text = R.DO_USE_LOCATOR_ID_FOR_TABLE_ROW)
	public static Operation use(@FieldParameter(name = "y") int y, @FieldParameter(name = "locatorId") String locatorId)
	{
		return new Operation().use(y, locatorId);
	}
	//endregion

	//region Time operations
	@DescriptionAttribute(text = R.DO_DELAY)
	public static Operation delay(@FieldParameter(name = "ms") int ms)
	{
		return new Operation().delay(ms);
	}

	@DescriptionAttribute(text = R.DO_WAIT)
	public static Operation wait(@FieldParameter(name = "str") String str)
	{
		return new Operation().wait(str);
	}

	@DescriptionAttribute(text = R.DO_WAIT)
	public static Operation wait(@FieldParameter(name = "str") String str, @FieldParameter(name = "ms") int ms, @FieldParameter(name = "toAppear") boolean toAppear)
	{
		return new Operation().wait(str, ms, toAppear);
	}

	@DescriptionAttribute(text = R.DO_WAIT)
	public static Operation wait(@FieldParameter(name = "locator") Locator locator, @FieldParameter(name = "ms") int ms, @FieldParameter(name = "toAppear") boolean toAppear)
	{
		return new Operation().wait(locator, ms, toAppear);
	}
	//endregion
}
