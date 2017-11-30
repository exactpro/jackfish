////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
