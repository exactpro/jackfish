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

package com.exactprosystems.jf.api.app;

import java.io.Serializable;

public class CheckingLayoutResultBean implements Serializable
{
	private static final long serialVersionUID = -524281864386712581L;

	private String relativeField;
	private String relation;
	private String actual;
	private String expected;

	public CheckingLayoutResultBean(String relativeField, String relation, String actual, String expected)
	{
		this.relativeField = relativeField;
		this.relation = relation;
		this.actual = actual;
		this.expected = expected;
	}

	public String getRelativeField()
	{
		return relativeField;
	}

	public String getRelation()
	{
		return relation;
	}

	public String getActual()
	{
		return actual;
	}

	public String getExpected()
	{
		return expected;
	}
}
