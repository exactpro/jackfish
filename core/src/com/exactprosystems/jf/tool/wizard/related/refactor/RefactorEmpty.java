////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
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