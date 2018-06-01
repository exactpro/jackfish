////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2018, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.wizard.related.refactor;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import java.util.List;

public class RefactorLoadDocument extends Refactor
{
	private List<WizardCommand> command;
	private String message;

	public RefactorLoadDocument(Document document)
	{
		this.message = String.format(R.REFACTOR_LOAD_DOCUMENT.get(), Common.getRelativePath(document.getNameProperty().get()));
		this.command = CommandBuilder.start()
				.loadDocument(document)
				.build();
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
