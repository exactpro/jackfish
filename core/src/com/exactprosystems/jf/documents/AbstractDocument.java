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
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;

import java.io.Reader;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractDocument implements Document
{
    protected DocumentFactory     factory;
    private ActionTrackProvider   provider = new ActionTrackProvider();

    private MutableValue<String>  nameProperty;
    private MutableValue<Boolean> changedProperty;

    @Deprecated
    private boolean               hasName  = false;
    @Deprecated
    private String                name;

    public AbstractDocument(String fileName, DocumentFactory factory)
	{
		this.factory = factory;
		this.nameProperty = new MutableValue<>(fileName);
		this.changedProperty = new MutableValue<>(false);
		
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
			this.nameProperty.set(annotation.newName());
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
        this.nameProperty.set(fileName);
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
        this.changedProperty.set(true);
    }
    
    @Override
    public void undo()
    {
        if (this.provider.undo())
        {
            this.changedProperty.set(true);
            afterRedoUndo();
        }
    }

    @Override
    public void redo()
    {
        if (this.provider.redo())
        {
            this.changedProperty.set(true);
            afterRedoUndo();
        }
    }

    @Override
    public MutableValue<String> getNameProperty()
    {
        return this.nameProperty;
    }
    
    @Override
    public MutableValue<Boolean> getChangedProperty()
    {
        return this.changedProperty;
    }

    @Deprecated
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void saved()
	{
	    this.changedProperty.set(false);
		this.provider.clear();
	}

    @Override
    public DocumentFactory getFactory()
    {
        return this.factory;
    }

	protected void afterRedoUndo()
	{
	}
}
