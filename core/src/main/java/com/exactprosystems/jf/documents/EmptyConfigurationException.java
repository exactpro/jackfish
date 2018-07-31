/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
