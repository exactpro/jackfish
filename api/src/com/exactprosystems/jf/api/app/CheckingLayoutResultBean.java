////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
