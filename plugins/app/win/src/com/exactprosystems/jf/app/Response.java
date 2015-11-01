////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.google.gson.annotations.SerializedName;

public class Response<T>
{
	@SerializedName(value = "Exception")
	private String	exception;
	
	@SerializedName(value = "ReturnValue")
	private T	returnValue;

	public Response(String exception, T returnValue)
	{
		this.exception = exception;
		this.returnValue = returnValue;
	}

	public Response()
	{
		this.exception = null;
		this.returnValue = null; 
	}
	
	public String getException()
	{
		return exception;
	}

	public void setException(String exception)
	{
		this.exception = exception;
	}

	public T getReturnValue()
	{
		return returnValue;
	}

	public void setReturnValue(T returnValue)
	{
		this.returnValue = returnValue;
	}
}
