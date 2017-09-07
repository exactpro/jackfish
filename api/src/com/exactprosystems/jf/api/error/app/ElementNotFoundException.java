////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.error.app;

import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class ElementNotFoundException extends JFRemoteException
{
	private static final long serialVersionUID = -4722988704327432417L;

	public ElementNotFoundException(String msg)
	{
		super(msg, null);
	}

	public ElementNotFoundException(int x, int y)
	{
		super(String.format("Element not found by location (%d,%d)", x, y), null);
	}

	public ElementNotFoundException(String msg, Locator locator)
	{
		super(msg + locator, null);
	}

	public ElementNotFoundException(Locator locator)
	{
		super("No one element was found. Element: " + locator, null);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.ELEMENT_NOT_FOUND;
	}
}
