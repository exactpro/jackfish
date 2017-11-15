////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.HashMap;
import java.util.Map;

public class LocatorsHolder
{
	public LocatorsHolder()
	{
		this.holder = new HashMap<LocatorKind, Locator>();
	}
	
	public void put(LocatorKind kind, Locator locator)
	{
		this.holder.put(kind, locator);
	}
	
	public Locator get(LocatorKind kind)
	{
		return this.holder.get(kind);
	}
	
	private Map<LocatorKind, Locator> holder;
}
