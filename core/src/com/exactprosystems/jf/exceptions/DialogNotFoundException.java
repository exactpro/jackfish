package com.exactprosystems.jf.exceptions;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;

public class DialogNotFoundException extends JFException
{
	public DialogNotFoundException(String windowName)
	{
		super(String.format("Window %s not found in the dictionary", windowName));
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.DIALOG_NOT_FOUND;
	}
}
