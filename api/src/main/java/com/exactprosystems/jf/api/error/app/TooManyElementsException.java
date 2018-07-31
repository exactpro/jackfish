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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class TooManyElementsException extends JFRemoteException
{
	private static final long	serialVersionUID	= 9087420795591504557L;

	public TooManyElementsException(String msg)
	{
		super(msg, null);
	}

	public TooManyElementsException(String msg, Locator locator)
	{
		super(msg +" " + locator, null);
	}

	public TooManyElementsException(Locator locator)
	{
		super(String.format(R.TOO_MANY_ELEMENTS_EXCEPTION_MESSAGE.get(), locator), null);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.TOO_MANY_ELEMENTS;
	}
}
