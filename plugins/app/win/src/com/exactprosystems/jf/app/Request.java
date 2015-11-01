////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Request
{
	@SerializedName(value = "NameMethod")
	private String				nameMethod;

	@SerializedName(value = "Parameters")
	private Map<String, Object>	parameters;

	public Request(String nameMethod, Map<String, Object> parameters)
	{
		this.nameMethod = nameMethod;
		this.parameters = parameters;
	}

	public String getNameMethod()
	{
		return nameMethod;
	}

	public void setNameMethod(String nameMethod)
	{
		this.nameMethod = nameMethod;
	}

	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters)
	{
		this.parameters = parameters;
	}

	public Request()
	{

	}
}
