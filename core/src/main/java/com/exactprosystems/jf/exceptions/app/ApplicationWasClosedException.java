/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.exceptions.app;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;

public class ApplicationWasClosedException extends JFException
{
	public ApplicationWasClosedException(String appId)
	{
		super(String.format(R.APP_WAS_CLOSED_EXCEPTION.get(), appId));
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.APPLICATION_CLOSED;
	}
}
