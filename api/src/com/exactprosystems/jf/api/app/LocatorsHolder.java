////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
