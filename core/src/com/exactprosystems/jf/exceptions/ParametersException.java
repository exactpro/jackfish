////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.exceptions;

import com.exactprosystems.jf.documents.matrix.parser.Parameters;

public class ParametersException extends Exception
{
	private Parameters params;

	public ParametersException(String message, Parameters params)
	{
		this(message, null, params);
	}

	public ParametersException(String message, Throwable cause, Parameters params)
	{
		super(message, cause);
		this.params = params;
	}
}
