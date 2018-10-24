/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
		MergeEditor mergeEditor = new MergeEditor(this.model, bean.getFileName());
		mergeEditor.display();
		if (mergeEditor.isSuccessful())
		{
			this.mergedFiles.add(bean.getFileName());
			this.controller.removeBean(bean);
		}
	}
}
