////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error.app;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class ElementNotEnabled extends JFRemoteException
{
	private static final long	serialVersionUID	= 4045605559537133458L;

	public ElementNotEnabled(String message)
	{
		super(message, null);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.ELEMENT_NOT_ENABLED;
	}
}
