////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.service;

public class ServicePossibilityIsDisabled extends Exception
{
	private static final long	serialVersionUID	= -6883081786211266833L;

	public ServicePossibilityIsDisabled()
	{
		super();
	}
	
	public ServicePossibilityIsDisabled(String message)
	{
		super(message);
	}
}
