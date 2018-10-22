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
