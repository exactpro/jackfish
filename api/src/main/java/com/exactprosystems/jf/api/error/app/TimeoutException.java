/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.error.app;

import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class TimeoutException extends JFRemoteException
{
	private static final long serialVersionUID = 6090238826549902713L;

	public TimeoutException(String msg)
	{
		super(msg, null);
	}

	public TimeoutException(String msg, Locator locator)
	{
		super(msg + " " + locator, null);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.TIMEOUT;
	}
}
