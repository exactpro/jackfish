////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error;

import java.rmi.RemoteException;

public abstract class JFRemoteException extends RemoteException
{
	private static final long	serialVersionUID	= 7205429619716291799L;

	public JFRemoteException(String message, Throwable cause)
	{
		super (message, cause);
	}
	
	public abstract ErrorKind getErrorKind();
}
