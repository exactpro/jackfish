////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.related.refactor;

import java.util.Collections;
import java.util.List;

import com.exactprosystems.jf.api.wizard.WizardCommand;

public class RefactorEmpty  extends Refactor
{
    private String message;
    
	public RefactorEmpty(String message)
	{
		this.message = message;
	}
	
	public List<WizardCommand> getCommands()
	{
		return Collections.emptyList();
	}
	
	@Override
	public String toString()
	{
		return this.message;
	}
}