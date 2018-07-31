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

import java.rmi.RemoteException;

public abstract class JFRemoteException extends RemoteException implements IErrorKind
{
	private static final long	serialVersionUID	= 7205429619716291799L;

	public JFRemoteException(String message, Throwable cause)
	{
		super (message, cause);
	}
}
