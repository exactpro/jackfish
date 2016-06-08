////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

public class EmptyConfigurationException extends Exception
{
	private static final long	serialVersionUID	= 3131082006845739736L;

	public EmptyConfigurationException()
	{
		super();
	}
	
	EmptyConfigurationException(String str)
	{
		super(str);
	}
	
	EmptyConfigurationException(Throwable t)
	{
		super(t);
	}

}
