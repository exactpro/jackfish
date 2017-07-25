////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import static com.exactprosystems.jf.api.app.OperationKind.*;

public enum ControlKind 
{	
    // 											default			allOther
	Any				("Any", 			false, 	CLICK,			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP),
	Wait        	("Wait", 			true,  	WAIT 		    ),
	Button      	("Button", 			false, 	PUSH, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN,   GET, GET_VALUE, CHECK, CHECK_REGEXP),
	CheckBox    	("CheckBox", 		false, 	TOGGLE,			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET),
	ComboBox    	("ComboBox", 		false,	SELECT,			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, SELECT_BY_INDEX, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET,  GET_LIST, CHECK_LIST, TEXT),
	Dialog      	("Dialog", 			false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN),
	Frame       	("Frame", 			false, 	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN),
	Image			("Image", 			false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN),
	Label       	("Label", 			false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN, GET, GET_VALUE, CHECK, CHECK_REGEXP),
	ListView    	("ListView", 		false, 	CLICK,			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, GET_LIST, CHECK_LIST, SELECT, SELECT_BY_INDEX),
	Menu    		("Menu", 			false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, GET, COLLAPSE, EXPAND),
	MenuItem    	("MenuItem", 		false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, GET,  CLICK_XY, COLLAPSE, EXPAND),
	Panel       	("Panel", 			false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, TEXT, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP),
	ProgressBar		("ProgressBar", 	false,	GET_VALUE, 		USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, CHECK, CHECK_REGEXP),
	RadioButton		("RadioButton", 	false,	TOGGLE, 		USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET),
	Row         	("Row", 			false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, GET,   PRESS, KEY_UP, KEY_DOWN),
	ScrollBar		("ScrollBar", 		false,	SET, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, GET_VALUE,  CHECK, CHECK_REGEXP),
	Slider			("Slider", 			false, 	SET, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, CLICK),
	Splitter		("Splitter", 		false,	SET, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, CLICK),
	Spinner			("Spinner", 		false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, SET),
	Table       	("Table", 			false,	GET_TABLE, 		USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK_XY, GET_VALUE_XY, TEXT_XY, CHECK_XY, CHECK_REGEXP_XY, PRESS, KEY_UP, KEY_DOWN, GET_ROW, GET_ROW_INDEXES, GET_ROW_BY_INDEX, GET_ROW_WITH_COLOR, GET_TABLE, GET_TABLE_SIZE),
	TabPanel    	("TabPanel", 		false,	SELECT, 		USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, SELECT_BY_INDEX, GET_LIST, CHECK_LIST),
	TextBox     	("TextBox", 		false,	TEXT, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET),
	ToggleButton	("ToggleButton", 	false,	TOGGLE, 		USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP, GET),
	Tooltip     	("ToolTip", 		false,	GET, 			USE, GET_VALUE, CHECK, CHECK_REGEXP),
	Tree        	("Tree", 			false, 	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, PRESS, KEY_UP, KEY_DOWN, COLLAPSE, EXPAND, GET_ROW_INDEXES, SELECT, GET_VALUE, GET_TREE),
	TreeItem		("TreeItem", 		false,	CLICK, 			USE, FOREACH, GET_ATTR, CHECK_ATTR, MOVE_XY, CLICK, CLICK_XY, MOVE, DRAG_N_DROP, PRESS, KEY_UP, KEY_DOWN, GET_VALUE, CHECK, CHECK_REGEXP)
	;
	
    private static OperationKind[] almostAll = new OperationKind[] { IS_ENABLED, IS_VISIBLE, GET_COLOR_XY, CHECK_COLOR_XY, COUNT, GET_RECTANGLE,
            CHECK_ATTR_REGEXP, SCRIPT };
    private static OperationKind[] all = new OperationKind[] { WAIT, DELAY, REPEAT, USE_LOCATOR, };
    private static Set<OperationKind> almostAllSet;
    private static Set<OperationKind> allSet;
    static
    {
        almostAllSet = new HashSet<>();
        almostAllSet.addAll(Arrays.asList(almostAll));
        allSet = new HashSet<>();
        allSet.addAll(Arrays.asList(all));
    }

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
		if (this != Wait && almostAllSet.contains(kind))
		{
		    return true;
		}
		if (allSet.contains(kind))
		{
		    return true;
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

