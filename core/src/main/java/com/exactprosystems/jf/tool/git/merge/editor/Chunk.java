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
