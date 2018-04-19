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

public class EmptyParameterException extends Exception
{
	private static final long	serialVersionUID	= -449426338160659754L;

	public EmptyParameterException(String message)
	{
		super(message);
	}
}
