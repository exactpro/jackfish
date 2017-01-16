package com.exactprosystems.jf.tool.git.branch;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;

public class GitBranch
{
	private Main model;
	private final CredentialBean credentialBean;
	private GitBranchController controller;

	public GitBranch(Main model) throws Exception
	{
		this.model = model;
		this.credentialBean = this.model.getCredential();
		this.controller = Common.loadController(this.getClass().getResource("GitBranch.fxml"));
		this.controller.init(this);
		this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
	}

	public void display()
	{
		this.controller.show();
	}

	void newBranch(String newName) throws Exception
	{
		GitUtil.createNewBranch(this.credentialBean, newName);
		this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
	}

	void renameBranch(String oldName, String newName) throws Exception
	{
		GitUtil.rename(this.credentialBean, oldName, newName);
		this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
	}

	void checkout(String branchName, String newBranchName) throws Exception
	{
		String newBranch = GitUtil.checkout(this.credentialBean, branchName, newBranchName);
		System.out.println("Checkout to branch : " + newBranch);
		this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
	}

	void deleteBranch(String branchName) throws Exception
	{
		boolean flag = DialogsHelper.showYesNoDialog("Are you sure, that you want to delete the branch?", "Delete branch");
		if (flag)
		{
			GitUtil.deleteBranch(this.credentialBean, branchName);
			this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
		}
	}
}
