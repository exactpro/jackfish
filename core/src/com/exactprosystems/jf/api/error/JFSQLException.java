package com.exactprosystems.jf.api.error;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;

public class JFSQLException extends JFException
{
    private static final long serialVersionUID = -368356122323820884L;

    public JFSQLException(String message)
    {
        super(message, null);
    }

    @Override
    public ErrorKind getErrorKind()
    {
        return ErrorKind.SQL_ERROR;
    }
}