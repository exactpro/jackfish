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
