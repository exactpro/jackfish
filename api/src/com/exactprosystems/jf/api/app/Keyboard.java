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
	ESCAPE, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,

	DIG1, DIG2, DIG3, DIG4, DIG5, DIG6, DIG7, DIG8, DIG9, DIG0,

	PLUS, MINUS, SLASH, BACK_SLASH,

	Q, W, E, R, T, Y, U, I, O, P,
	A, S, D, F, G, H, J, K, L, SEMICOLON,
	Z, X, C, V, B, N, M,

	DOWN, UP, RIGHT, LEFT,

	TAB,
	CAPS_LOCK,
	SHIFT,
	CONTROL,
	ALT,
	SPACE,
	HOME,

	PAGE_UP,
	PAGE_DOWN,
	END,
	QUOTE,
	DOUBLE_QUOTE,

	ENTER,
	DELETE,
	INSERT,
	BACK_SPACE,
	;

	private static final long serialVersionUID = -1378685462384062328L;
}
