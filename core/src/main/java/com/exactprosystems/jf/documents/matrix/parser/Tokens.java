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
