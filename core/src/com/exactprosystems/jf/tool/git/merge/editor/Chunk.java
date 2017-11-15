////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge.editor;

public class Chunk
{
	public enum ChunkState
	{
		Your,
		Their
	}

	private boolean hasConflict;

	private int start;
	private int end;

	private ChunkState state;

	public Chunk(boolean hasConflict, int start, int end, ChunkState state)
	{
		this.hasConflict = hasConflict;
		this.start = start;
		this.end = end;
		this.state = state;
	}

	public Chunk()
	{
	}

	public boolean isHasConflict()
	{
		return hasConflict;
	}

	public void setHasConflict(boolean hasConflict)
	{
		this.hasConflict = hasConflict;
	}

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getEnd()
	{
		return end;
	}

	public void setEnd(int end)
	{
		this.end = end;
	}

	public ChunkState getState()
	{
		return state;
	}

	public void setState(ChunkState state)
	{
		this.state = state;
	}

	@Override
	public String toString()
	{
		return "[ " + start + ", " + end + " ), " + (hasConflict ? state.name() : " No conflict");
	}

}
