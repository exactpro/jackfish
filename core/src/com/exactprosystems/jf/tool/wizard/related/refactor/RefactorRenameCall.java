////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.related.refactor;

import java.util.List;

import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

public class RefactorRenameCall  extends Refactor
{
	private List<WizardCommand> command;

	public RefactorRenameCall(String name)
	{
		this.command = CommandBuilder.start().print(name).print(name + " 1").print(name + " 2").build();
	}
	
	public List<WizardCommand> getCommands()
	{
		return this.command;
	}
	
	@Override
	public String toString()
	{
		return "Rename Call";
	}
}