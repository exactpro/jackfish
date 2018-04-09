/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.exceptions;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;

public class DialogNotFoundException extends JFException
{
	public DialogNotFoundException(String windowName)
	{
		super(String.format(R.DIALOG_NOT_FOUND_EXCEPTION.get(), windowName));
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.DIALOG_NOT_FOUND;
	}
}
