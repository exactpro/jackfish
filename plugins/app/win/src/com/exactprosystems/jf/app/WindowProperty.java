/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.app;

public enum WindowProperty
{
	AcceleratorKeyProperty(30006),
	AccessKeyProperty(30007),
	AutomationIdProperty(30011),
	BoundingRectangleProperty(30001),
	ClassNameProperty(30012),
	ClickablePointProperty(30014),
	ControlTypeProperty(30003),
	CultureProperty(30015),
	FrameworkIdProperty(30024),
	HasKeyboardFocusProperty(30008),
	HelpTextProperty(30013),
	IsContentElementProperty(30017),
	IsControlElementProperty(30016),
	IsDockPatternAvailableProperty(30027),
	IsEnabledProperty(30010),
	IsExpandCollapsePatternAvailableProperty(30028),
	IsGridItemPatternAvailableProperty(30029),
	IsGridPatternAvailableProperty(30030),
	IsInvokePatternAvailableProperty(30031),
	IsItemContainerPatternAvailableProperty(30108),
	IsKeyboardFocusableProperty(30009),
	IsMultipleViewPatternAvailableProperty(30032),
	IsOffscreenProperty(30022),
	IsPasswordProperty(30019),
	IsRangeValuePatternAvailableProperty(30033),
	IsRequiredForFormProperty(30025),
	IsScrollItemPatternAvailableProperty(30035),
	IsScrollPatternAvailableProperty(30034),
	IsSelectedProperty(30079),
	IsSelectionItemPatternAvailableProperty(30036),
	IsSelectionPatternAvailableProperty(30037),
	IsSynchronizedInputPatternAvailableProperty(30110),
	IsTableItemPatternAvailableProperty(30039),
	IsTablePatternAvailableProperty(30038),
	IsTextPatternAvailableProperty(30040),
	IsTogglePatternAvailableProperty(30041),
	IsTransformPatternAvailableProperty(30042),
	IsValuePatternAvailableProperty(30043),
	IsVirtualizedItemPatternAvailableProperty(30109),
	IsWindowPatternAvailableProperty(30044),
	ItemStatusProperty(30026),
	ItemTypeProperty(30021),
	LabeledByProperty(30018),
	LocalizedControlTypeProperty(30004),
	NameProperty(30005),
	NativeWindowHandleProperty(30020),
	OrientationProperty(30023),
	ProcessIdProperty(30002),
	RuntimeIdProperty(30000),
	
	ToggleStateProperty(30086),
	ValueProperty(30045),
	SelectionProperty(30059),
	MaximumProperty(30050),
	MinimumProperty(30049),

	//create by me property
	TrueProperty(-1),
	;
	
	WindowProperty(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public static WindowProperty byId(int id)
	{
		for (WindowProperty item : values())
		{
			if (item.id == id)
			{
				return item;
			}
		}
		
		return null;
	}
	
	private int id;
}
