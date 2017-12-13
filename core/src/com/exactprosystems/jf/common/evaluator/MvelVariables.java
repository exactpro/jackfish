////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.evaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MvelVariables extends Variables
{
	private Map<String, Object> var = new HashMap<>();

	public MvelVariables()
	{
		super();
	}

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
		list.forEach(this.var::remove);
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

	@Override
	public void clear()
	{
		this.var.clear();
	}
}
