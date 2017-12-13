////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.common.undoredo;

import java.util.ArrayDeque;
import java.util.Optional;

public class ActionTrackProvider
{
	private final ArrayDeque<DoubleCommand> undoArray = new ArrayDeque<>();
	private final ArrayDeque<DoubleCommand> redoArray = new ArrayDeque<>();

	public void addCommand(Command undo, Command redo)
	{
		DoubleCommand doubleCommand = new DoubleCommand(undo, redo);
		this.undoArray.add(doubleCommand);
		this.redoArray.clear();
	}
	
	public void clear()
	{
		this.undoArray.clear();
		this.redoArray.clear();
	}

	public boolean undo()
	{
		DoubleCommand last = this.undoArray.pollLast();
		Optional.ofNullable(last).ifPresent(command ->
		{
			command.undo.execute();
			this.redoArray.add(last);
		});
		return last != null;
	}

	public boolean redo()
	{
		DoubleCommand last = this.redoArray.pollLast();
		Optional.ofNullable(last).ifPresent(command ->
		{
			command.redo.execute();
			this.undoArray.add(last);
		});
		return last != null;
	}

	private class DoubleCommand
	{
		Command undo;
		Command redo;

		DoubleCommand(Command undo, Command redo)
		{
			this.undo = undo;
			this.redo = redo;
		}
	}
}
