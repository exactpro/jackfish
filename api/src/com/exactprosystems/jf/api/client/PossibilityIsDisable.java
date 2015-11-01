////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

public class PossibilityIsDisable extends Exception
{
	private static final long	serialVersionUID	= -6883081786211266833L;

	public PossibilityIsDisable()
	{
		super();
	}
	
	public PossibilityIsDisable(String message)
	{
		super(message);
	}
}
