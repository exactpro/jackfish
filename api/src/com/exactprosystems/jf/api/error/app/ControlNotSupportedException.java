////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error.app;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class ControlNotSupportedException extends JFRemoteException
{
    private static final long	serialVersionUID	= -3425794123466364307L;

    public ControlNotSupportedException(String message)
    {
        super(message, null);
    }

    @Override
    public ErrorKind getErrorKind()
    {
        return ErrorKind.CONTROL_NOT_SUPPORTED;
    }
}


