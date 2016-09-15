////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

public interface ICondition
{
	public String serialize();
	public String getName();
	public boolean isMatched(String otherName, Object otherValue);
	public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2);

	public String explanation(String name, Object actualValue);

}
