////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import static com.exactprosystems.jf.api.app.OperationKind.*;

public enum ControlKind 
{	// 											default			allOther
	Any				("Any", 			false, 	CLICK,			DRAG_N_DROP, FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY,CLICK_XY),
	Wait        	("Wait", 			true,  	WAIT, 			DELAY,	USE_LOCATOR),
	Button      	("Button", 			false, 	PUSH, 			DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, PRESS, KEY_UP, KEY_DOWN, CLICK, CLICK_XY, GET, GET_VALUE, CHECK, CHECK_REGEXP),
	CheckBox    	("CheckBox", 		false, 	TOGGLE,			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, CLICK, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET),
	ComboBox    	("ComboBox", 		false,	SELECT,			SELECT_BY_INDEX, FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK,CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET, CLICK, GET_LIST),
	Dialog      	("Dialog", 			false,	CLICK, 			DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN),
	Frame       	("Frame", 			false, 	CLICK, 			DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN),
	Image			("Image", 			false,	CLICK, 			DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN),
	Label       	("Label", 			false,	CLICK, 			DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET, GET_VALUE, CHECK, CHECK_REGEXP),
	ListView    	("ListView", 		false, 	CLICK,			SELECT, SELECT_BY_INDEX, 	FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET_LIST),
	Menu    		("Menu", 			false,	CLICK, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET, COLLAPSE, EXPAND),
	MenuItem    	("MenuItem", 		false,	CLICK, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, GET, MOVE_XY, CLICK_XY),
	Panel       	("Panel", 			false,	CLICK, 			DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, TEXT, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP),
	ProgressBar		("ProgressBar", 	false,	GET_VALUE, 		FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, CLICK, CHECK, CHECK_REGEXP),
	RadioButton		("RadioButton", 	false,	TOGGLE, 		FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, CLICK, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET),
	Row         	("Row", 			false,	CLICK, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, GET, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN),
	ScrollBar		("ScrollBar", 		false,	SET, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CLICK, CHECK, CHECK_REGEXP),
	Slider			("Slider", 			false, 	SET, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, CLICK),
	Splitter		("Splitter", 		false,	SET, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, CLICK),
	Spinner			("Spinner", 		false,	CLICK, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, CLICK, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, SET),
	Table       	("Table", 			false,	GET_TABLE, 		FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE_XY, CLICK_XY, GET_VALUE_XY, TEXT_XY, CHECK_XY, CHECK_REGEXP_XY, PRESS, KEY_UP, KEY_DOWN, GET_ROW, GET_ROW_INDEXES, GET_ROW_BY_INDEX, GET_ROW_WITH_COLOR, GET_TABLE, GET_TABLE_SIZE),
	TabPanel    	("TabPanel", 		false,	SELECT, SELECT_BY_INDEX,	FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, CLICK, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP),
	TextBox     	("TextBox", 		false,	TEXT, 			DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, CLICK, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET, SCRIPT),
	ToggleButton	("ToggleButton", 	false,	TOGGLE, 		DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, CLICK, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET),
	Tooltip     	("ToolTip", 		false,	GET, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, GET_VALUE, CHECK, CHECK_REGEXP),
	Tree        	("Tree", 			false, 	CLICK, 			FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, COLLAPSE, EXPAND),
	TreeItem		("TreeItem", 		false,	CLICK, 			DRAG_N_DROP,FOREACH, REPEAT, USE_LOCATOR, USE, COUNT, DELAY, GET_RECTANGLE, GET_ATTR, CHECK_ATTR, CHECK_ATTR_REGEXP, MOVE, MOVE_XY, CLICK_XY, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP)
	;
	
	ControlKind(String clazz, boolean virtual, OperationKind defaultOperation, OperationKind ... allOperations)
	{
		this.clazzName = clazz;
		this.virtual = virtual;
		
		this.defaultOperation = defaultOperation;
		this.allOperations = new LinkedHashSet<OperationKind>();
		this.allOperations.add(defaultOperation);
		this.allOperations.addAll(Arrays.asList(allOperations));
	}

	public static ControlKind findByClazz(String clazz)
	{
		for (ControlKind kind : ControlKind.values())
		{
			if (kind.getClazz().equals(clazz))
			{
				return kind;
			}
		}
		return Any;
	}

	public OperationKind defaultOperation()
	{
		return this.defaultOperation;
	}
	
	public Set<OperationKind> allOperations()
	{
		return this.allOperations;
	}

	public boolean isAllowed(OperationKind kind)
	{
		if (kind == null)
		{
			return false;
		}
		return this.allOperations.contains(kind);
	}
	
	
	public boolean isVirtual()
	{
		return this.virtual;
	}
	
    public String getClazz()
    {
        return clazzName;
    }

    private String clazzName;
    private boolean virtual;
    private OperationKind defaultOperation = null;
    private Set<OperationKind> allOperations = null;
}

