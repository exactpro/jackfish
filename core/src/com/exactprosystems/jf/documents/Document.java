////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;

import java.io.Reader;
import java.util.function.Consumer;

public interface Document extends Mutable
{
    DocumentFactory getFactory();

	void load(Reader reader) throws Exception;

	void create() throws Exception;
	
	void display() throws Exception;
	
	boolean canClose() throws Exception;

	void close() throws Exception;

	void onClose(Consumer<Document> consumer);
	
	void save(String fileName) throws Exception;

	void onSave(Consumer<Document> consumer);

    void addCommand(Command undo, Command redo);

    void undo();

	void redo();

	MutableValue<String> getNameProperty();
	
	MutableValue<Boolean> getChangedProperty();
}
