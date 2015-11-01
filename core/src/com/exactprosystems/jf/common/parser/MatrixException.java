////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser;

import com.exactprosystems.jf.common.parser.items.MatrixItem;

public class MatrixException extends Exception
{
	private static final long serialVersionUID = -8878908206137039974L;

	public MatrixException(int lineNumber, MatrixItem item, Exception ex)
	{
		super("Matrix error in #" + lineNumber + (item == null ? " " : " '" + item.getItemName() + "' ") + ex.getMessage());
	}
	
	public MatrixException(int lineNumber, MatrixItem item, String msg)
	{
		super("Matrix error in #" + lineNumber + (item == null ? " " : " '" + item.getItemName() + "' ") + msg);
	}
}
