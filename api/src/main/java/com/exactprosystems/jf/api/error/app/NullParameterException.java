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

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class NullParameterException extends JFRemoteException
{
	private static final long	serialVersionUID	= 2729558079619073423L;

	public NullParameterException(String message)
	{
		super(String.format(R.NULL_PARAMETER_EXCEPTION_MESSAGE.get(), message), null);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.EMPTY_PARAMETER;
	}
}
