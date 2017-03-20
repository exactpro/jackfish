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
import java.util.Iterator;
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

	boolean isSuccessful;

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

	public void close()
	{
		this.isSuccessful = false;
		this.controller.closeDialog();
	}

	public void saveResult(String result) throws Exception
	{
		Common.writeToFile(new File(GitUtil.checkFile(this.model.getCredential(), this.filePath)), Arrays.asList(result.split("\n")));
		GitUtil.addFileToIndex(this.model.getCredential(), this.filePath);
		this.isSuccessful = true;
		this.controller.closeDialog();
	}

	public boolean isSuccessful()
	{
		return isSuccessful;
	}

	private void evaluate() throws Exception
	{
		Iterator<Chunk> iterator = this.conflicts.iterator();
		int i = 0;
		int yourLastPos = 0;
		int theirLastPos = 0;
		while (iterator.hasNext())
		{
			Chunk chunk = iterator.next();
			if (!chunk.isHasConflict())
			{
				int diff = chunk.getEnd() - chunk.getStart();

				String yourText = listToStr(this.yourLines.subList(yourLastPos, yourLastPos + diff));
				yourLastPos += diff;

				String theirText = listToStr(this.theirLines.subList(theirLastPos, theirLastPos + diff));
				theirLastPos += diff;

				this.controller.addLines(yourText, theirText, yourText, false, i);
			}
			else
			{
				Chunk theirChunk = iterator.next();

				int yourDiff = chunk.getEnd() - chunk.getStart();
				String yourText = listToStr(this.yourLines.subList(yourLastPos, yourLastPos + yourDiff));
				yourLastPos += yourDiff;

				int theirDiff = theirChunk.getEnd() - theirChunk.getStart();
				String theirText = listToStr(this.theirLines.subList(theirLastPos, theirLastPos + theirDiff));
				theirLastPos += theirDiff;

				this.controller.addLines(yourText, theirText, "", true, i);
			}
			i++;
		}
	}

	private String listToStr(List<String> list)
	{
		return list.stream().collect(Collectors.joining("\n"));
	}
}
