////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////


package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

/**
 * The interface, used for generating the string representation of the object.
 *
 * @see DisplayDriver#showTextBox(MatrixItem, Object, int, int, java.util.function.Consumer, java.util.function.Supplier, FormulaGenerator)
 */
@FunctionalInterface
public interface FormulaGenerator
{
	String generate();
}
