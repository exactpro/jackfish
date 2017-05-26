////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error;

public abstract class JFRuntimeException extends RuntimeException
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
	
	public abstract ErrorKind getErrorKind();
}
