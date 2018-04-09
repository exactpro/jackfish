/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.app;

import java.io.Serializable;

public class LocatorAndOperation implements Serializable
{

	private static final long serialVersionUID = 427074351132702320L;

	public LocatorAndOperation(Locator locator, Operation operation)
	{
		this.locator = locator;
		this.operation = operation;
	}
	
	@Override
	public String toString()
	{
		return "[" + this.locator.toString() + ":" + this.operation.toString() + "]";
	}
	
	public Locator getLocator()
	{
		return locator;
	}
	
	public Operation getOperation()
	{
		return operation;
	}

	private Locator locator;
	private Operation operation;
}
