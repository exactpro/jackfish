////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.app;

import java.rmi.RemoteException;

public class ElementNotFoundException extends RemoteException
{
	private static final long serialVersionUID = -4722988704327432417L;

	public ElementNotFoundException(int x, int y)
	{
		super(String.format("Element not found by location (%d,%d)", x, y));
	}

	public ElementNotFoundException(String msg, Locator locator)
	{
		super(msg + locator);
	}

	public ElementNotFoundException(Locator locator)
	{
		super("No one element was found. Element: " + locator);
	}
}
