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
package com.exactprosystems.jf.tool.git.reset;

import javafx.scene.paint.Color;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;

public class FileWithStatusBean
{
	private final File file;
	private final DiffEntry.ChangeType changeType;

	public FileWithStatusBean(DiffEntry entry)
	{
		this.changeType = entry.getChangeType();
		this.file = new File(this.changeType == DiffEntry.ChangeType.DELETE ? entry.getOldPath() : entry.getNewPath());
	}

	public File getFile()
	{
		return file;
	}

	public DiffEntry.ChangeType getChangeType()
	{
		return changeType;
	}

	public Color getColor()
	{
		switch (changeType)
		{
			case DELETE:
				return Color.RED;
			default:
				return Color.GREEN;
		}
	}
}
