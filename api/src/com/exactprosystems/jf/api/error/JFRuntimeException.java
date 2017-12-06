////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error;

public abstract class JFRuntimeException extends RuntimeException implements IErrorKind
{
    private static final long serialVersionUID = -2429503786251472815L;

    public JFRuntimeException(String message, Throwable cause)
	{
		super (message, cause);
	}

	public JFRuntimeException(String message)
	{
		super(message);
	}
}
