////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CheckingLayoutResult implements Serializable
{
	private static final long	serialVersionUID	= 3562209509406788900L;

	public boolean isOk()
	{
		return this.ok;
	}
	
	public List<String> getErrors()
	{
		return this.errors;
	}

	public void error(String string)
	{
		this.ok = false;
		this.errors.add(string);
	}

	public void set(boolean b)
	{
		if (!b)
		{
			this.ok = false;
		}
	}
	
	private boolean ok = true;
	private List<String> errors = new ArrayList<String>();
}
