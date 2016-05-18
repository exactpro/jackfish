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

	DIG1, DIG2, DIG3, DIG4, DIG5, DIG6, DIG7, DIG8, DIG9, DIG0,BACK_SPACE,		INSERT, HOME, PAGE_UP,
	TAB,		Q, W, E, R, T, Y, U, I, O, P,SLASH, BACK_SLASH,					DELETE, END, PAGE_DOWN,
	CAPS_LOCK,	A, S, D, F, G, H, J, K, L, SEMICOLON, QUOTE, DOUBLE_QUOTE, ENTER,
	SHIFT,		Z, X, C, V, B, N, M,							 					UP,
	CONTROL,	ALT, SPACE,														LEFT,DOWN,RIGHT,

	PLUS, MINUS,
	UNDERSCORE
	;

	private static final long serialVersionUID = -1378685462384062328L;
}
