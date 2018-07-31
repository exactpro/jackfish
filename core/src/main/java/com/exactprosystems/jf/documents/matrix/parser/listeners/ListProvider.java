/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.actions.ReadableValue;

import java.util.List;

@FunctionalInterface
public interface ListProvider
{
	List<ReadableValue> getList() throws Exception;
}
