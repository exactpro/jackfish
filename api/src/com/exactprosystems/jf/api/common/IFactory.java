/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.common;

public interface IFactory
{
	String[]		wellKnownParameters(ParametersKind kind);
	boolean			canFillParameter(String parameterToFill);
	String[]		listForParameter(String parameterToFill);
}
