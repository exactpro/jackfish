////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error.app;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class ProxyException extends JFRemoteException
{
	private static final long	serialVersionUID	= -7998700941400982238L;

	public ProxyException(String message, String shortMessage, Throwable cause)
	{
        super(message, cause);
        this.message = message;
        this.shortMessage = shortMessage;
        this.stackTrace = cause.getStackTrace();
	}
	
	public String getFullMessage()
	{
		return this.message;
	}
	
	@Override
	public StackTraceElement[] getStackTrace()
	{
		return this.stackTrace;
	}
	
	@Override
	public String getMessage()
	{
		return this.shortMessage;
	}	
	
	private StackTraceElement[] stackTrace;
	
	private String message = null;

	private String shortMessage = null;

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.EXCEPTION;
	}
}
