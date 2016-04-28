////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool;

import com.exactprosystems.jf.common.*;
import com.exactprosystems.jf.common.undoredo.ActionTrackProvider;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.config.Configuration;

import java.io.File;
import java.io.Reader;
import java.util.Optional;

public abstract class AbstractDocument implements Document
{
	public AbstractDocument(String fileName, Configuration config)
	{
		this.config = config;
		this.name = fileName;
		this.hasName = true;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof AbstractDocument))
		{
			return false;
		}
		AbstractDocument other = (AbstractDocument) obj;
		if (this.name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!new File(this.name).getAbsolutePath().equals(new File(other.name).getAbsolutePath()))
		{
			return false;
		}
		return true;
	}

	@Override
	public Configuration getConfiguration()
	{
		return this.config;
	}
	
	@Override
	public boolean hasName()
	{
		return this.hasName;
	}
	
	@Override
	public void create() throws Exception
	{
		DocumentInfo annotation = getClass().getAnnotation(DocumentInfo.class);
		if (annotation != null)
		{
			this.name = annotation.newName();
		}
		this.hasName = false;
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
		this.hasName = true;
	}
	
	@Override
	public void save(String fileName) throws Exception
	{
		this.name = fileName;
		this.hasName = true;
	}
	
	@Override
	public void undo()
	{
		if (this.provider.undo())
		{
			changed(true);
			afterRedoUndo();
		}
	}

	@Override
	public void redo()
	{
		if (this.provider.redo())
		{
			changed(true);
			afterRedoUndo();
		}
	}

	@Override
	public void display() throws Exception
	{
	}

	@Override
	public void close(Settings settings) throws Exception
	{
		Optional.ofNullable(getConfiguration()).ifPresent(c -> c.unregister(this));
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void saved()
	{
		changed(false);
		this.provider.clear();
	}

	@Override
	public void setOnChange(ChangeListener listener)
	{
		this.listener = listener;
	}

	
	
	protected void changed(boolean flag)
	{
		Optional.ofNullable(listener).ifPresent(l -> l.change(flag));
	}
	
	protected void afterRedoUndo()
	{
	}
	
	public void addCommand(Command undo, Command redo)
	{
		redo.execute();
		this.provider.addCommand(undo, redo);
		afterRedoUndo();
		this.changed(true);
	}

	private Configuration config;
	private boolean hasName = false;
	private String name;
	private ActionTrackProvider provider = new ActionTrackProvider();
	private ChangeListener listener;
}
