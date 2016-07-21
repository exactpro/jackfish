////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2016, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.app.exception;

import java.rmi.RemoteException;

import com.exactprosystems.jf.api.app.Locator;

public class TooManyElementsException extends RemoteException
{
	private static final long	serialVersionUID	= 9087420795591504557L;

	public TooManyElementsException(String msg)
	{
		super(msg);
	}

	public TooManyElementsException(String msg, Locator locator)
	{
		super(msg + locator);
	}

	public TooManyElementsException(Locator locator)
	{
		super("Too many elements were found. Element: " + locator);
	}
}
