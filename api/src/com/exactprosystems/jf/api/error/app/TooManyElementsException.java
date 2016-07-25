////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2016, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error.app;

import com.exactprosystems.jf.api.app.Locator;
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
		super(msg + locator, null);
	}

	public TooManyElementsException(Locator locator)
	{
		super("Too many elements were found. Element: " + locator, null);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.ELEMENT_NOT_FOUND;
	}
}
