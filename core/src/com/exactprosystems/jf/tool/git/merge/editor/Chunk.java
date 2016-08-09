////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge.editor;

public class Chunk
{
	private int firstStart;
	private int firstEnd;

	private int secondStart;
	private int secondEnd;

	public Chunk()
	{
	}

	public Chunk(int firstStart, int firstEnd, int secondStart, int secondEnd)
	{
		this.firstStart = firstStart;
		this.firstEnd = firstEnd;
		this.secondStart = secondStart;
		this.secondEnd = secondEnd;
	}

	public Chunk(int firstStart, int firstEnd)
	{
		this.firstStart = firstStart;
		this.firstEnd = firstEnd;
	}

	public int getFirstStart()
	{
		return firstStart;
	}

	public int getSecondStart()
	{
		return secondStart;
	}

	public void setSecondStart(int secondStart)
	{
		this.secondStart = secondStart;
	}

	public void setFirstStart(int firstStart)
	{
		this.firstStart = firstStart;
	}

	public int getFirstEnd()
	{
		return firstEnd;
	}

	public void setFirstEnd(int firstEnd)
	{
		this.firstEnd = firstEnd;
	}

	public int getSecondEnd()
	{
		return secondEnd;
	}

	public void setSecondEnd(int secondEnd)
	{
		this.secondEnd = secondEnd;
	}

	@Override
	public String toString()
	{
		return	"1st : ["+this.firstStart+"; " + this.firstEnd + ")\n"
				+
				"2nd : ["+this.secondStart+";"+this.secondEnd + ")";
	}
}
