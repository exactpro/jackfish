/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.wizard.related.refactor;

import java.util.List;

import com.exactprosystems.jf.api.common.i18n.R;
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
        this.message = String.format(R.REFACTOR_ADD_ITEM_MESSAGE.get(), item, item.getId(), Common.getRelativePath(matrix.getNameProperty().get()));
        CommandBuilder builder = CommandBuilder.start();
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