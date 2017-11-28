////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.error.app;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRuntimeException;

public class TableOutOfBoundsException extends JFRuntimeException
{

	public TableOutOfBoundsException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public TableOutOfBoundsException(String message)
	{
		super(message);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.WRONG_PARAMETERS;
	}
}
