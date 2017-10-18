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
