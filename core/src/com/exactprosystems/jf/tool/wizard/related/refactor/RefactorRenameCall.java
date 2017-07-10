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
import com.exactprosystems.jf.documents.matrix.parser.items.Call;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

public class RefactorRenameCall  extends Refactor
{
	private List<WizardCommand> command;
	private String file;
	private int size;

	public RefactorRenameCall(Matrix matrix, String newName, List<Integer> calls)
	{
	    this.file = matrix.getName();
	    this.size = calls.size();
	    CommandBuilder builder = CommandBuilder.start();
        builder.loadDocument(matrix);
	    calls.forEach(c -> 
	    {
	        builder.findAndHandleMatrixItem(matrix, c, i -> ((Call)i).setName(newName));
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
		return this.file + " : " + this.size;
	}
}