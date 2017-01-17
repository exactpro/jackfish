package com.exactprosystems.jf.tool.git.branch;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Task;

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
		DialogsHelper.showInfo("Start creating branch");
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
			DialogsHelper.showError("Error on create new branch");
		});
		task.setOnSucceeded(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showSuccess("New branch was created");
			Common.tryCatch(() -> this.controller.updateBranches(GitUtil.getBranches(this.credentialBean)), "Error on create new branch");
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
		String newBranch = GitUtil.checkout(this.credentialBean, branchName, newBranchName);
		System.out.println("Checkout to branch : " + newBranch);
		this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
	}

	void deleteBranch(GitUtil.Branch branch) throws Exception
	{
		boolean flag = DialogsHelper.showYesNoDialog("Are you sure, that you want to delete the branch?", "Delete branch");
		if (flag)
		{
			GitUtil.deleteBranch(this.credentialBean, branch);
			this.controller.updateBranches(GitUtil.getBranches(this.credentialBean));
		}
	}
}
