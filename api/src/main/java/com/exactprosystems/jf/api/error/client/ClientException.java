/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.error.client;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;

public class ClientException extends JFException
{
	private static final long	serialVersionUID	= 3150355969884595079L;

	public ClientException(String message)
	{
		super(message, null);
	}

	public ClientException(String message, Throwable cause)
	{
		super(message, cause);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.CLIENT_ERROR;
	}
}
