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
import java.util.List;
import java.util.stream.Collectors;

public class MergeEditor
{
	private final String filePath;
	private final Main model;

	private List<String> yourLines;
	private List<String> theirLines;
	private List<Chunk> conflicts;

	private MergeEditorController controller;

	public MergeEditor(Main model, String filePath) throws Exception
	{
		this.filePath = filePath;
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("MergeEditor.fxml"));
		this.controller.init(this, this.filePath);

		this.yourLines = GitUtil.getYours(this.model.getCredential(), filePath);
		this.theirLines = GitUtil.getTheirs(this.model.getCredential(), filePath);
		this.conflicts = GitUtil.getConflicts(this.model.getCredential(), this.filePath);

		evaluate();
	}

	public void display()
	{
		this.controller.show();
	}

	public void acceptTheirs()
	{
//		this.controller.displayResult(this.theirLines);
	}

	public void acceptYours()
	{
//		this.controller.displayResult(this.yourLines);
	}

	public void close()
	{
		this.controller.closeDialog();
	}

	public void saveResult(List<String> result) throws Exception
	{
		Common.writeToFile(new File(this.filePath), result);
		GitUtil.addFileToIndex(this.model.getCredential(), this.filePath);
	}

	void dialogShown() throws Exception
	{
		this.controller.displayLines(this.conflicts);
	}

	private void evaluate()
	{
		List<Chunk> conflicts = this.conflicts;
		Chunk previousChunk;
		for (int i = 0; i < conflicts.size(); i++)
		{
			Chunk conflict = conflicts.get(i);
			List<String> subYour = this.yourLines.subList(conflict.getFirstStart(), conflict.getFirstEnd());
			List<String> subTheir = this.theirLines.subList(conflict.getSecondStart(), conflict.getSecondEnd());
			String yourText = subYour.stream().collect(Collectors.joining("\n"));
			String theirText = subTheir.stream().collect(Collectors.joining("\n"));
			this.controller.addLines(yourText, theirText, "", true, i);

			previousChunk = conflict;
		}
	}
}
