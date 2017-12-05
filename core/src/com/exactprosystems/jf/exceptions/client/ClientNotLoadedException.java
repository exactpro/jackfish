////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.exceptions.client;

import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;

public class ClientNotLoadedException extends JFException
{
	public ClientNotLoadedException(String message)
	{
		super(message);
	}

	public ClientNotLoadedException(ClientConnection connection)
	{
		super(String.format(R.COMMON_CLIENT_IS_NOT_LOADED.get(), String.valueOf(connection)));
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.CLIENT_ERROR;
	}
}
