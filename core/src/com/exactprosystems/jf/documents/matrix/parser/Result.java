/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser;

public enum Result
{
	Break      ("Break"),
	Continue   ("Continue"),
	Return     ("Return"),
	Passed     ("Passed"),
	Failed     ("Failed"),
    StepFailed ("Failed"),
	Ignored    ("Ignored"),
	NotExecuted("NotExecuted"),
	Off        ("Off"),
	Stopped    ("Stopped"),
	;
	private String name;

	Result(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	public boolean isFail()
	{
		return this == Failed || this == StepFailed;
	}

	public String getStyle()
	{
		return this.name;
	}

}
