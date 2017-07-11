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
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

public class RefactorAddItem extends Refactor
{
    private List<WizardCommand> command;
    private String message;

	public RefactorAddItem(Matrix matrix, MatrixItem where, MatrixItem item, int index)
	{
        this.message = "Add '" + item +  "' to '" + Common.getRelativePath(matrix.getName()) + "'";
        CommandBuilder builder = CommandBuilder.start();
        builder.loadDocument(matrix);
        builder.addMatrixItem(matrix, where, item, index);
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