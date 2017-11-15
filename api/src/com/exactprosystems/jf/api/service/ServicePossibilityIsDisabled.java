////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
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
