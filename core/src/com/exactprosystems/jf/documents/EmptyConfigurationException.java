////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

public class EmptyConfigurationException extends Exception
{
	private static final long	serialVersionUID	= 3131082006845739736L;

	public EmptyConfigurationException()
	{
		super();
	}
	
	public EmptyConfigurationException(String str)
	{
		super(str);
	}
	
	public EmptyConfigurationException(Throwable t)
	{
		super(t);
	}

}
