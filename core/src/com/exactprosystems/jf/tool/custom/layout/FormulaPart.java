////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.PieceKind;
import com.exactprosystems.jf.api.app.Range;

class FormulaPart 
{
	public FormulaPart(PieceKind kind)
	{
		this.kind = kind;
		this.range = Range.EQUAL;
		this.name = "";
		this.first = "";
		this.second = "";
	}
	
	@Override
	public String toString()
	{
		return this.kind.toFormula(this.name, this.range, this.first, this.second);
	}
	
	public PieceKind getKind()
	{
		return kind;
	}

	public String getName()
	{
		return name;
	}

	public Range getRange()
	{
		return range;
	}

	public String getFirst()
	{
		return first;
	}

	public String getSecond()
	{
		return second;
	}
	
	
	public void setRange(Range range)
	{
		this.range = range;
	}

	public void addName(String str)
	{
		this.name += str;
	}
	
	public void addFirst(String str)
	{
		this.first += str;
	}
	
	public void addSecond(String str)
	{
		this.second += str;
	}

	private PieceKind kind;
	private String name;
	private Range range;
	private String first;
	private String second;
}