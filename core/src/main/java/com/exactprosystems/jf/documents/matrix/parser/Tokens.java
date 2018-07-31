/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser;

import java.util.Arrays;

public enum Tokens
{
	TempItem,
	This,
	Off,
	RepOff,
	Global,
	Id,
	Action,
	IgnoreErr,
	TestCase,
	Assert,
	Message,
	SubCase,
	EndSubCase,
	Call,
	If,
	Else,
	EndIf,
	Switch,
	Case,
	Default,
	EndSwitch,
	While,
	EndWhile,
	For,
	From,
	To,
	Step,
	EndFor,
	ForEach,
	In,
	EndForEach,
	Break,
	Continue,
	Return,
	OnError,
	Fail,
	RawTable,
	EndRawTable,
	RawMessage,
	Client,
	EndRawMessage,
	RawText,
	EndRawText,
	NameSpace,
	EndNameSpace,
	Let,
	EndStep,
	SetHandler,
	Kind,
	Depends,
	;

	public String get()
	{
		return this.name();
	}
	
	public static boolean contains(String name)
	{
		return Arrays.stream(values())
				.anyMatch(token -> token.name().equals(name));
	}

	public static boolean containsIgnoreCase(String name)
	{
		return Arrays.stream(values())
				.anyMatch(token -> token.name().equalsIgnoreCase(name));
	}
}
