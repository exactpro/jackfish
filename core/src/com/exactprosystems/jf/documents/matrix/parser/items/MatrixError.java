////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.error.ErrorKind;

/**
 * The class representing the matrix error bean.
 * Has fields:
 * <ul>
 * <li>{@link MatrixError#Where} : the place ( item), where was error</li>
 * <li>{@link MatrixError#Message} : the error message</li>
 * <li>{@link MatrixError#Kind} : the error kind of error</li>
 * </ul>
 *
 * @see ErrorKind
 * @see MatrixItem
 */
public class MatrixError
{
	public MatrixItem Where;
	public String     Message;
	public ErrorKind  Kind;

	public MatrixError(MatrixError error)
	{
		if (error != null)
		{
			this.Where = error.Where.makeCopy();
			this.Message = error.Message;
			this.Kind = error.Kind;
		}
	}

	public MatrixError(String message, ErrorKind kind, MatrixItem where)
	{
		this.Message = message;
		this.Kind = kind;
		this.Where = where;
	}

	@Override
	public String toString()
	{
		return this.Message;
	}
}
