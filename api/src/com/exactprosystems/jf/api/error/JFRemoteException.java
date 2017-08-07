////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
