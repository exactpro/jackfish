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

import java.util.Map;

public interface ICondition
{
	String serialize();
	String getName();

	boolean isMatched(Map<String, Object> map);

	String explanation(String name, Object actualValue);

}
