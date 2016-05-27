////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

public class ReturnAndResult implements Cloneable
{
	public ReturnAndResult(Result result, Object out)
	{
		this(result, out, null);
	}
	
	public ReturnAndResult(Result result, Object out, String error)
	{
		this.result = result;
		this.out = out;
		this.error = error;
	}

	public ReturnAndResult (Result result)
	{
		this.result = result;
	}
	
	public Result getResult()
	{
		return this.result;
	}

	public Object getOut()
	{
		return this.out;
	}

	public String getError()
	{
		return this.error;
	}

	private Result result;
	private Object out;
	private String error;
}
