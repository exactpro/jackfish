////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
