/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
	private final ActionTrackProvider provider = new ActionTrackProvider();
	private final   MutableValue<String>  nameProperty;
	private final   MutableValue<Boolean> changedProperty;
	protected final DocumentFactory       factory;

	private Consumer<Document> closeConsumer;
	private Consumer<Document> saveConsumer;

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

	//region interface Document
	/**
	 * Create a new document. The base method change only the name for the document
	 */
	@Override
	public void create()
	{
		DocumentInfo annotation = this.getClass().getAnnotation(DocumentInfo.class);
		if (annotation != null)
		{
			this.nameProperty.accept(annotation.newName());
		}
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
	}

	/**
	 * Save the document to a file with passed name.
	 * After saving, a save consumer will calling ( if the consumer is present)
	 *
	 * @param fileName a fileName, which will use for save the document
	 *
	 * @throws Exception if something went wrong (e.g. file for saving not found)
	 *
	 * @see Document#onSave(Consumer)
	 */
	@Override
	public void save(String fileName) throws Exception
	{
		this.nameProperty.accept(fileName);
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
		this.afterRedoUndo();
		this.changedProperty.accept(true);
	}

	@Override
	public void undo()
	{
		if (this.provider.undo())
		{
			this.changedProperty.accept(true);
			this.afterRedoUndo();
		}
	}

	@Override
	public void redo()
	{
		if (this.provider.redo())
		{
			this.changedProperty.accept(true);
			this.afterRedoUndo();
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
	public DocumentFactory getFactory()
	{
		return this.factory;
	}
	//endregion

	//region interface Mutable
	@Override
	public void saved()
	{
		this.nameProperty.saved();
		this.changedProperty.saved();
		this.provider.clear();
	}

	@Override
	public boolean isChanged()
	{
		return this.nameProperty.isChanged() || this.changedProperty.isChanged();
	}

	//endregion

	/**
	 * This method called after executing any undo or redo commands.
	 * Override this methods for so something after executing a undo or redo commands
	 *
	 * @see AbstractDocument#addCommand(Command, Command)
	 */
	protected void afterRedoUndo()
	{
	}
}
