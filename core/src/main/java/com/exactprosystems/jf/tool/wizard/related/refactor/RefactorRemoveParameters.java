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

import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import java.util.Arrays;
import java.util.List;

public class RefactorRemoveParameters extends Refactor
{
	private String              message;
	private List<WizardCommand> commands;

	public RefactorRemoveParameters(MatrixItem item, int[] parameterIndexes)
	{
		this.message = String.format("Remove parameters with indexes %s from %s [%s] from a matrix %s"
				, Arrays.toString(parameterIndexes)
				, item
				, item.getNumber()
				, Common.getRelativePath(item.getMatrix().getNameProperty().get())
		);
		commands = CommandBuilder.start()
				.removeParameters(item, parameterIndexes)
				.build();
	}

	@Override
	public String toString()
	{
		return this.message;
	}

	@Override
	public List<WizardCommand> getCommands()
	{
		return commands;
	}
}
