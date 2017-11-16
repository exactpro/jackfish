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
	private ArrayDeque<DoubleCommand> undoArray = new ArrayDeque<>();
	private ArrayDeque<DoubleCommand> redoArray = new ArrayDeque<>();

	public ActionTrackProvider()
	{

	}

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
			try 
			{
				command.undo.execute();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			this.redoArray.add(last);
		});
		return last != null;
	}

	public boolean redo()
	{
		DoubleCommand last = this.redoArray.pollLast();
		Optional.ofNullable(last).ifPresent(command -> 
		{
			try 
			{
				command.redo.execute();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			this.undoArray.add(last);
		});
		return last != null;
	}

	private class DoubleCommand
	{
	    public Command undo;
	    public Command redo;

		public DoubleCommand(Command undo, Command redo)
		{
			this.undo = undo;
			this.redo = redo;
		}
	}
}
