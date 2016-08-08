////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge.editor;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.main.Main;

public class MergeEditor
{
	private final String filePath;
	private final Main model;

	private MergeEditorController controller;

	public MergeEditor(Main model, String filePath) throws Exception
	{
		this.filePath = filePath;
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("MergeEditor.fxml"));
		this.controller.init(this);
		this.controller.displayYours(GitUtil.getYours(this.model.getCredential(), filePath));
		this.controller.displayTheirs(GitUtil.getTheirs(this.model.getCredential(), filePath));
	}

	public void display()
	{
		this.controller.show();
	}
}
