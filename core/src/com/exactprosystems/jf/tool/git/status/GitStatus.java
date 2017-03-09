////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.status;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.main.Main;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitStatus
{
	private Main model;
	private final GitStatusController controller;

	public GitStatus(Main model) throws Exception
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitStatus.fxml"));
		this.controller.init(this, GitUtil.gitRootDirectory(this.model.getCredential()));
	}

	public void display(List<GitBean> list, String state)
	{
		this.controller.display(list, state);
	}

	void revertPaths(Set<String> paths) throws Exception
	{
		CredentialBean credential = this.model.getCredential();
		GitUtil.revertPaths(credential, paths);
		this.controller.updateFiles(GitUtil.gitStatus(credential));
	}

	void ignoreFiles(List<File> collect) throws Exception
	{
		GitUtil.ignorePaths(collect.stream().map(File::getPath).map(path -> path.replaceAll("\\\\","/")).collect(Collectors.toList()));
		this.controller.updateFiles(GitUtil.gitStatus(this.model.getCredential()));
	}

	void ignoreByPattern(String pattern) throws Exception
	{
		GitUtil.ignorePaths(Stream.of(pattern).collect(Collectors.toList()));
		this.controller.updateFiles(GitUtil.gitStatus(this.model.getCredential()));
	}
}
