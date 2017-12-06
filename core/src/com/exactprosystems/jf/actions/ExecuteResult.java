////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions;

public class ExecuteResult
{
	public String Text;
	public int ExitCode;
	public int PID;

	public ExecuteResult(String text, int exitCode, int pid)
	{
		this.Text = text;
		this.ExitCode = exitCode;
		this.PID = pid;
	}
	
	@Override
	public String toString()
	{
		return "PID:      " + this.PID + "\n"
			+  "ExitCode: " + this.ExitCode + "\n"
			+  "Text:     " + this.Text;
	}
}
