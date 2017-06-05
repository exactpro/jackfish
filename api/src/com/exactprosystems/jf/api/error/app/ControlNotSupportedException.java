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


