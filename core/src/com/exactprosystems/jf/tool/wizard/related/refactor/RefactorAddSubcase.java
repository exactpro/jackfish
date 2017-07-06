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
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

public class RefactorAddSubcase
{
	private List<WizardCommand> command;

	public RefactorAddSubcase(MatrixFx matrix, MatrixItem where, SubCase subcase)
	{
		this.command = CommandBuilder.start().addMatrixItem(matrix, where, subcase, 0).build();
	}
	
	public List<WizardCommand> getCommands()
	{
		return this.command;
	}
}