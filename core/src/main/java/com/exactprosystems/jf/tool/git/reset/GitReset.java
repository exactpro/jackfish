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
package com.exactprosystems.jf.tool.git.reset;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;

import java.util.List;

public class GitReset
{
	private final Main model;
	private final GitResetController controller;

	public GitReset(Main model, List<GitResetBean> list) throws Exception
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitReset.fxml"));
		this.controller.init(this, list);

	}

	public void select(GitResetBean item) throws Exception
	{
		this.controller.displayMessage(item.getMessage());
		this.controller.displayFiles(item.getFiles());
	}

	public void reset(GitResetBean bean) throws Exception
	{
		String commitId = bean.getCommitId();
		GitUtil.gitReset(this.model.getCredential(), commitId);
		DialogsHelper.showSuccess(String.format(R.GIT_RESET_TO.get(), commitId));
		this.controller.hide();
	}

	public void display()
	{
		this.controller.show();
	}

	public void hide()
	{
		this.controller.hide();
	}
}
