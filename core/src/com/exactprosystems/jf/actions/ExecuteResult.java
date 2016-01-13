////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions;

public class ExecuteResult
{
	public ExecuteResult(String text, int exitCode)
	{
		this.Text = text;
		this.ExitCode = exitCode;
	}
	
	@Override
	public String toString()
	{
		return "ExitCode: " + this.ExitCode + "\n"
			+ "Text: " + this.Text;
	}
	
	public String Text;

	public int ExitCode;
}
