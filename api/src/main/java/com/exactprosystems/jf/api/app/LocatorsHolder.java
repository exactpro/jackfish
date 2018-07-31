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
