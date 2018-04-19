/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
