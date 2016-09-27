////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import java.util.Map;

public interface ICondition
{
	String serialize();
	String getName();

	boolean isMatched(Map<String, Object> map);

	@Deprecated
	boolean isMatched(String otherName, Object otherValue);
	@Deprecated
	boolean isMatched2(String otherName, Object otherValue1, Object otherValue2);

	String explanation(String name, Object actualValue);

}
