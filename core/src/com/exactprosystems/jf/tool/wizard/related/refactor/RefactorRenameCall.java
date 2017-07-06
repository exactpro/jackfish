////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.related.refactor;

import java.io.File;
import java.util.List;

import com.exactprosystems.jf.api.wizard.WizardCommand;

public abstract class RefactorRenameCall
{
	private File file;

	public RefactorRenameCall(File file)
	{
		this.file = file;
	}
	
	public abstract List<WizardCommand> getCommands();
}