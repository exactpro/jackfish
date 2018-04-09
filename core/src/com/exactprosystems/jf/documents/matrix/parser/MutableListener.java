/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.documents.matrix.parser;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface MutableListener<T> extends Supplier<T>
{
	/**
	 * Set the listener for observing a value
	 */
	void setOnChangeListener(BiConsumer<T, T> listener);
}
