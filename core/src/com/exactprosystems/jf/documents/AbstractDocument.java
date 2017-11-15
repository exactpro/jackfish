////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.common.undoredo.ActionTrackProvider;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;

import java.io.Reader;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractDocument implements Document
{
    protected DocumentFactory     factory;
    private ActionTrackProvider   provider = new ActionTrackProvider();

    private MutableValue<String>  nameProperty;
    private MutableValue<Boolean> changedProperty;

    private Consumer<Document>    closeConsumer;
    private Consumer<Document>    saveConsumer;

    public AbstractDocument(String fileName, DocumentFactory factory)
	{
		this.factory = factory;
		this.nameProperty = new MutableValue<>(fileName);
		this.changedProperty = new MutableValue<>(false);
	}
	
	@Override
	public int hashCode()
	{
		return this.getNameProperty().hashCode();
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
		return Objects.equals(this.getNameProperty(), other.getNameProperty());
	}

	@Override
	public void create() throws Exception
	{
		DocumentInfo annotation = getClass().getAnnotation(DocumentInfo.class);
		if (annotation != null)
		{
			this.nameProperty.set(annotation.newName());
		}
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
	}
	
	@Override
	public void save(String fileName) throws Exception
	{
        this.nameProperty.set(fileName);
		Optional.ofNullable(this.saveConsumer).ifPresent(consumer -> consumer.accept(this));
	}

	@Override
	public void onSave(Consumer<Document> consumer)
	{
		this.saveConsumer = consumer;
	}

	@Override
	public void display() throws Exception
	{
	    this.nameProperty.fire();
	}

	@Override
	public void close() throws Exception
	{
		Optional.ofNullable(getFactory().getConfiguration()).ifPresent(c -> c.unregister(this));
		Optional.ofNullable(this.closeConsumer).ifPresent(consumer -> consumer.accept(this));
	}

	@Override
	public final void onClose(Consumer<Document> consumer)
	{
		this.closeConsumer = consumer;
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
