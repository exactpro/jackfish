package com.exactprosystems.jf.common.parser;

public enum Tokens
{
	TempItem,
	This,
	Off,
	Global,
	Id,
	Action,
	Description,
	IgnoreErr,
	Out,
	Result,
	Reason,
	TestCase,
	AssertOutIs,
	AssertOutIsNot,
	Assert,
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
	ReportOn,
	ReportOff,
	Fail,
	RawTable,
	EndRawTable,
	RawMessage,
	Client,
	EndRawMessage,
	RawText,
	EndRawText;

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
}
