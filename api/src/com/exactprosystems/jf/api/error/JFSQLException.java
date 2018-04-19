/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.error;

import java.sql.SQLException;

public class JFSQLException extends JFException
{
    private static final long serialVersionUID = -368356122323820884L;

    public JFSQLException(String message)
    {
        super(message, null);
    }

	public JFSQLException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public JFSQLException(SQLException e)
	{
		super(e.getMessage(), e);
	}

	@Override
    public ErrorKind getErrorKind()
    {
        return ErrorKind.SQL_ERROR;
    }
}