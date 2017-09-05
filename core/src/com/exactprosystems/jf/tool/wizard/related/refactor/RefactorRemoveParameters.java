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
		this.message = String.format("Remove parameter with indexes %s from item %s from a matrix %s"
				, Arrays.toString(parameterIndexes)
				, item
				, Common.getRelativePath(item.getMatrix().getNameProperty().get())
		);
		commands = CommandBuilder.start()
				.removeParameters(item, parameterIndexes)
				.saveDocument(item.getMatrix())
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
