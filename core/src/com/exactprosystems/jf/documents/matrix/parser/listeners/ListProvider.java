////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.actions.ReadableValue;

import java.util.List;

@FunctionalInterface
public interface ListProvider
{
	List<ReadableValue> getList() throws Exception;
}
