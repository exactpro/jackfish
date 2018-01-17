////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.related.refactor;

import java.util.List;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

public class RefactorSetField  extends Refactor
{
	private List<WizardCommand> command;
	private String message;

	public RefactorSetField(Matrix matrix, Tokens token, String value, List<Integer> itemIds)
	{
        int size = itemIds.size();
        this.message = String.format(R.REFACTOR_SET_FIELD_MESSAGE.get(), token, value, Common.getRelativePath(matrix.getNameProperty().get()), size);
	    CommandBuilder builder = CommandBuilder.start();
        builder.loadDocument(matrix);
	    itemIds.forEach(c -> 
	    {
	        builder.findAndHandleMatrixItem(matrix, c, i -> i.set(token, value));
	    });
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