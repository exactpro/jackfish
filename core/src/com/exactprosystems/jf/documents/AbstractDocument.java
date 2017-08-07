////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.common.undoredo.ActionTrackProvider;
import com.exactprosystems.jf.common.undoredo.Command;
import java.io.Reader;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractDocument implements Document
{
	public AbstractDocument(String fileName, DocumentFactory factory)
	{
		this.factory = factory;
		this.name = fileName;
		this.hasName = true;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.name);
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
		return Objects.equals(this.name, other.name);
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
	public void display() throws Exception
	{
	}

	@Override
	public void close() throws Exception
	{
		Optional.ofNullable(getFactory().getConfiguration()).ifPresent(c -> c.unregister(this));
	}
	
    @Override
    public void addCommand(Command undo, Command redo)
    {
        redo.execute();
        this.provider.addCommand(undo, redo);
        afterRedoUndo();
        this.changed(true);
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
    public DocumentFactory getFactory()
    {
        return this.factory;
    }

    
	@Override
	public void setOnChange(Consumer<Boolean> listener)
	{
		this.listener = listener;
	}


	
	protected void changed(boolean flag)
	{
		Optional.ofNullable(listener).ifPresent(l -> l.accept(flag));
	}
	
	protected void afterRedoUndo()
	{
	}
	
	protected 	DocumentFactory 	factory;
	private 	boolean 			hasName = false;
	private 	String 				name;
	private 	ActionTrackProvider provider = new ActionTrackProvider();
	private 	Consumer<Boolean>   listener;
}
