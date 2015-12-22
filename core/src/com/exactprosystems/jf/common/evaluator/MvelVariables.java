////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.evaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MvelVariables extends Variables
{

	@Override
	public void set(Map<String, Object> predef)
	{
		this.var.putAll(predef);
	}

	@Override
	public void set(String name, Object value)
	{
		this.var.put(name, value);
	}

	@Override
	public void delete(List<String> list)
	{
		for (String name : list)
		{
			this.var.remove(name); 
		}
	}

	@Override
	public void delete(String name)
	{
		this.var.remove(name);
	}

	@Override
	public Map<String, Object> getVars()
	{
		return this.var;
	}

	@Override
	public Object getVariable(String string)
	{
		return this.var.get(string);
	}

	private Map<String, Object> var = new HashMap<String, Object>();

}
