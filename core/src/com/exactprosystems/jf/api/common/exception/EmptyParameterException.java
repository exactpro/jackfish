////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common.exception;

public class EmptyParameterException extends Exception
{
	private static final long	serialVersionUID	= -449426338160659754L;

	public EmptyParameterException(String message)
	{
		super(message);
	}
}
