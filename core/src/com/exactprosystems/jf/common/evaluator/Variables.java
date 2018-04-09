/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.common.evaluator;

import java.util.List;
import java.util.Map;

public abstract class Variables
{
	public abstract void set(Map<String, Object> predef);
	
	public abstract void set(String name, Object value);

	public abstract void delete(List<String> list);
	
	public abstract void delete(String name);

	public abstract Map<String, Object> getVars();

	public abstract Object getVariable(String string);
	
	public abstract void clear();
}
