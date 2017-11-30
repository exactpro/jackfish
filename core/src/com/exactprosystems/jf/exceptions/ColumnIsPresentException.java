////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.exceptions;

import com.exactprosystems.jf.api.common.i18n.R;

public class ColumnIsPresentException extends RuntimeException
{
	public ColumnIsPresentException(String columnName)
	{
		super(String.format(R.COLUMN_IS_PRESENT_EXCEPTION.get(), columnName));
	}
}
