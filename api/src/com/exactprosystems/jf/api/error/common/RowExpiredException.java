////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error.common;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;
import com.exactprosystems.jf.api.error.JFRuntimeException;

public class RowExpiredException extends JFRuntimeException
{
    private static final long serialVersionUID = -7810216751166242467L;

    public RowExpiredException(String message)
	{
		super(message, null);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.ROW_EXPIRED;
	}
}
