////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

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
		for (Tokens token : values())
		{
			if (token.name().equals(name))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean containsIgnoreCase(String name)
	{
		for (Tokens token : values())
		{
			if (token.name().equals(name))
			{
				return true;
			}
		}
		return false;
	}
}
