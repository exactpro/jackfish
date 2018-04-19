/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
