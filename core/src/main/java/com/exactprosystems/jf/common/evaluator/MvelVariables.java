/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
