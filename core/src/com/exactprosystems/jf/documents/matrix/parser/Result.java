////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

public enum Result
{
	Break          ("Break"),
	Continue       ("Continue"),
	Return         ("Return"),
	Passed         ("Passed"),
	Failed         ("Failed"),
    StepFailed     ("Failed"),
	Ignored        ("Ignored"),
	NotExecuted    ("NotExecuted"),
	Off            ("Off"),
	Stopped        ("Stopped"),
	;
    
    private Result(String name)
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
	
	private String name;
}
