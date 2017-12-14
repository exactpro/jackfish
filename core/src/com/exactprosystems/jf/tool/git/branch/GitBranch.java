////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
