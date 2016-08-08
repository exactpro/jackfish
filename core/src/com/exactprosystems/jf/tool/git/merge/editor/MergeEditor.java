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

import java.io.File;
import java.util.Arrays;

public class MergeEditor
{
	private final String filePath;
	private final Main model;

	private String yoursText;
	private String theirsText;

	private MergeEditorController controller;

	public MergeEditor(Main model, String filePath) throws Exception
	{
		this.filePath = filePath;
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("MergeEditor.fxml"));
		this.controller.init(this);

		this.yoursText = GitUtil.getYours(this.model.getCredential(), filePath).stream().reduce((s, s2) -> s + System.lineSeparator() + s2).orElse("");
		this.theirsText = GitUtil.getTheirs(this.model.getCredential(), filePath).stream().reduce((s, s2) -> s + System.lineSeparator() + s2).orElse("");
		this.controller.displayYours(this.yoursText);
		this.controller.displayTheirs(this.theirsText);
		this.controller.displayResult(""); // TODO eval result
	}

	public void display()
	{
		this.controller.show();
	}

	public void acceptTheirs()
	{
		this.controller.displayResult(this.theirsText);
	}

	public void acceptYours()
	{
		this.controller.displayResult(this.yoursText);
	}

	public void close()
	{
		check();
		this.controller.closeDialog();
	}

	public void saveResult(String result) throws Exception
	{
		check();
		Common.writeToFile(new File(this.filePath), Arrays.asList(result.split(System.lineSeparator())));
		GitUtil.addFileToIndex(this.model.getCredential(), this.filePath);
	}

	private void check()
	{

	}
}
