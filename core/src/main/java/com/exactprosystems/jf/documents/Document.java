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

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;

import java.io.Reader;
import java.util.function.Consumer;

/**
 * A interface for any document
 */
public interface Document extends Mutable
{
	/**
	 * @return instance of DocumentFactory for the document
	 *
	 * @see DocumentFactory
	 */
    DocumentFactory getFactory();

	/**
	 * Load the current document
	 * @param reader a instance of Reader, from which will load the document
	 * @throws Exception if loading was failed
	 */
	void load(Reader reader) throws Exception;

	/**
	 * Create a new document. The created document has all default values
	 */
	void create();

	/**
	 * Display the current document
	 * @throws Exception if displaying was failed
	 *
	 * @see DocumentFactory#showDocument(Document)
	 */
	void display() throws Exception;

	/**
	 * Check, that the document can be closed.
	 * <p>
	 * If any parameter of the document was changed, this method should return false. It mean, that before closing the document, the document need be saved
	 * This method should return true for any not displayable document.
	 * @return true, if document can be closed and false otherwise
	 * @throws Exception if something went wrong
	 *
	 * @see Mutable#isChanged()
	 */
	boolean canClose() throws Exception;

	/**
	 * Close the document and remove from the configuration
	 *
	 * @throws Exception if closing was failed
	 *
	 * @see com.exactprosystems.jf.documents.config.Configuration#unregister(Document)
	 */
	void close() throws Exception;

	/**
	 * Set a close consumer for the document
	 * <p>
	 * This consumer will invoke <b>after</b> invoke the {@link Document#close()} method
	 */
	void onClose(Consumer<Document> consumer);

	/**
	 * Save the document in the passed file
	 * @param fileName a fileName, which will use for save the document
	 * @throws Exception if something went wrong (e.g. file for saving not found)
	 */
	void save(String fileName) throws Exception;

	/**
	 * Consumer for saving the document
	 * <p>
	 * This consumer will invoke <b>after</b> invoke {@link Document#save(String)} method
	 */
	void onSave(Consumer<Document> consumer);

	/**
	 * Add undo and redo command for UndoRedo actions
	 * @param undo a undo command
	 * @param redo a redo command
	 *
	 * @see com.exactprosystems.jf.common.undoredo.ActionTrackProvider
	 * @see Command
	 */
	void addCommand(Command undo, Command redo);

	/**
	 * Execute the latest added undo command
	 */
	void undo();

	/**
	 * Execute the latest added redo command
	 */
	void redo();

	/**
	 * A mutable value for observing a name property of the document.
	 * <p>
	 * This property may used for subscribe for events. see {@link MutableValue#fire()}
	 * @return a MutableValue for observing a name property of the document
	 *
	 * @see MutableValue
	 */
	MutableValue<String> getNameProperty();

	/**
	 * A mutable value for observing a change property of the document.
	 * <p>
	 * This property may used for subscribe for events. see {@link MutableValue#fire()}
	 * @return a MutableValue for observing a change property of the document
	 *
	 * @see MutableValue
	 */
	MutableValue<Boolean> getChangedProperty();
}
