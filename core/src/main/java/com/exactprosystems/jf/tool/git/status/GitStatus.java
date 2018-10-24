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
