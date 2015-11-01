////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.io.Serializable;

public enum Keyboard implements Serializable
{
	F1, F2, F3,
	F4, F5, F6,
	F7, F8, F9,
	F10, F11, F12,
	Down,
	Up,
	Right,
	Left,
	A,
	B,
	Escape,
	Enter,
	Tab,
	Delete,
	Backspase,
	Capslock,
	Shift;

	private static final long serialVersionUID = -1378685462384062328L;
}
