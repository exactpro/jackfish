////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.evaluator;

import java.util.List;
import java.util.Map;

public abstract class Variables
{
	public void check(VariableCheckListener listener) {}
	
	public abstract void set(Map<String, Object> predef);
	
	public abstract void set(String name, Object value);

	public abstract void delete(List<String> list);
	
	public abstract void delete(String name);

	public abstract Map<String, Object> getVars();

	public abstract Object getVariable(String string);
	
	public abstract void clear();
}
