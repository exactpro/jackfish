////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.related.refactor;

import java.util.List;

import com.exactprosystems.jf.api2.wizard.WizardCommand;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

public class RefactorRemoveItem   extends Refactor
{
	private List<WizardCommand> command;
    private String message;

	public RefactorRemoveItem(Matrix matrix, MatrixItem item)
	{
        this.message = "Remove " + item + " '" + item.getId() +  "' from '" + Common.getRelativePath(matrix.getName()) + "'";
        CommandBuilder builder = CommandBuilder.start();
        builder.removeMatrixItem(matrix, item);
        builder.saveDocument(matrix);
        this.command = builder.build();
	}
	
	public List<WizardCommand> getCommands()
	{
		return this.command;
	}

	@Override
	public String toString()
	{
		return this.message;
	}
}