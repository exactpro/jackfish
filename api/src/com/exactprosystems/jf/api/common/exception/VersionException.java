/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.common.exception;

public class VersionException extends Exception
{
	private static final long	serialVersionUID	= 1355520252954821010L;

	public VersionException(String message)
	{
		super(message);
	}
}
