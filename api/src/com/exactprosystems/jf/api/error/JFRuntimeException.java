/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
