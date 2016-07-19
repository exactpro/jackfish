////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.common.Str;

public class MatrixError
{
	public MatrixError(String message, ErrorKind kind, MatrixItem where)
	{
		this.message = message;
		this.kind = kind;
		this.where = where;
	}
	
	public MatrixError(String message)
	{
		this(message, ErrorKind.OTHER, null);
	}

	@Override
	public String toString()
	{
		return this.message;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		
		if (obj instanceof String)
		{
			return Str.areEqual(this.message, (String)obj);
		}

		if (obj instanceof MatrixError)
		{
			return Str.areEqual(this.message, ((MatrixError)obj).message);
		}
		
		return false;
	}
	
	public MatrixItem where;
	public String message;
	public ErrorKind kind;
}
