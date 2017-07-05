////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

// TODO move this class to common exception package
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
