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

package com.exactprosystems.jf.tool.git.branch;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;

public class GitBranch
{
	private final CredentialBean credentialBean;
	private GitBranchController controller;

	public GitBranch(CredentialBean credentialBean) throws Exception
	{
		this.credentialBean = credentialBean;
		this.controller = Common.loadController(this.getClass());
		this.controller.init(this);
		this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
	}

	public void display()
	{
		this.controller.show();
	}

	void newBranch(String newName)
	{
		DialogsHelper.showInfo(R.GIT_BRANCH_START.get());
		this.controller.setDisable(true);
		Task<Void> task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				GitUtil.createNewBranch(credentialBean, newName);
				return null;
			}
		};
		task.setOnFailed(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showError(R.GIT_BRANCH_ERROR_CREATE.get());
		});
		task.setOnSucceeded(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showSuccess(R.GIT_BRANCH_CREATED.get());
			Common.tryCatch(() -> this.controller.updateBranches(GitUtil.getBranches(this.credentialBean)), R.GIT_BRANCH_ERROR_CREATE.get());
		});
		new Thread(task).start();
	}

	void renameBranch(String oldName, String newName) throws Exception
	{
		GitUtil.rename(this.credentialBean, oldName, newName);
		this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
	}

	void checkout(String branchName, String newBranchName) throws Exception
	{
		GitUtil.checkout(this.credentialBean, branchName, newBranchName);
		this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
	}

	void deleteBranch(GitUtil.Branch branch) throws Exception
	{
		if (DialogsHelper.showYesNoDialog(R.GIT_BRANCH_DELETE_MESSAGE.get(), R.GIT_BRANCH_DELETE_QUESTION.get()))
		{
			GitUtil.deleteBranch(this.credentialBean, branch);
			this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
		}
	}
}
