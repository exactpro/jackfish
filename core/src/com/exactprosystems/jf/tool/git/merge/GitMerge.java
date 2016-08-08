////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.git.merge.editor.MergeEditor;
import com.exactprosystems.jf.tool.main.Main;

import java.util.ArrayList;
import java.util.List;

public class GitMerge
{
	private Main model;
	private List<GitMergeBean> conflictFiles;

	private GitMergeController controller;
	private List<String> mergedFiles;

	public GitMerge(Main model, List<GitMergeBean> collect)
	{
		this.model = model;
		this.mergedFiles = new ArrayList<>();
		this.controller = Common.loadController(this.getClass().getResource("GitMerge.fxml"));
		this.controller.init(this, collect);
	}

	public List<String> getMergedFiles()
	{
		return this.mergedFiles;
	}

	public void display()
	{
		this.controller.show();
	}

	public void acceptTheirs(GitMergeBean bean) throws Exception
	{
		String fileName = bean.getFileName();
		GitUtil.mergeTheirs(this.model.getCredential(), fileName);
		this.mergedFiles.add(fileName);
		this.controller.removeBean(bean);
	}

	public void acceptYours(GitMergeBean bean) throws Exception
	{
		String fileName = bean.getFileName();
		GitUtil.mergeYours(this.model.getCredential(), fileName);
		this.mergedFiles.add(fileName);
		this.controller.removeBean(bean);
	}

	public void merge(GitMergeBean bean) throws Exception
	{
		new MergeEditor(this.model, bean.getFileName()).display();
	}
}
