/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.git.branch;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GitBranchController implements Initializable, ContainingParent
{
	private static final String LOCAL_NAME  = "Local";
	private static final String REMOTE_NAME = "Remote";

	public Parent                   parent;
	public TreeView<GitUtil.Branch> treeView;
	public Button                   btnNewBranch;
	public Button                   btnRenameBranch;
	public Button                   btnCheckoutBranch;
	public Button                   btnMergeBranch;
	public Button                   btnDeleteBranch;
	public VBox                     vBox;
	public Button                   btnClose;

	private GitBranch model;
	private Alert     dialog;

	private TreeItem<GitUtil.Branch> localBranchParent;
	private TreeItem<GitUtil.Branch> remoteBranchParent;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}
	//endregion Initializable

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	void init(GitBranch model)
	{
		this.model = model;
		this.dialog = DialogsHelper.createGitDialog(R.GIT_BRANCH_CONTR_INIT_TITLE.get(), this.parent);
		initTree();
	}

	void show()
	{
		this.dialog.showAndWait();
	}

	void updateBranches(List<GitUtil.Branch> branches)
	{
		this.localBranchParent.getChildren().clear();
		this.remoteBranchParent.getChildren().clear();
		branches.forEach(b -> {
			TreeItem<GitUtil.Branch> item = b.isLocal() ? this.localBranchParent : this.remoteBranchParent;
			item.getChildren().add(new TreeItem<>(b));
		});
	}

	void setDisable(boolean flag)
	{
		this.treeView.setDisable(flag);
		this.vBox.getChildren().forEach(n -> n.setDisable(flag));
		this.btnClose.setDisable(flag);
		if (!flag)
		{
			this.btnNewBranch.setDisable(false);
			this.treeView.getSelectionModel().clearSelection();
			this.treeView.getSelectionModel().select(this.localBranchParent);
		}
	}

	public void hide(ActionEvent event)
	{
		this.dialog.hide();
	}

	public void newBranch(ActionEvent actionEvent)
	{
		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setTitle(R.GIT_BRANCH_CONTR_ENTER_NAME.get());
		inputDialog.getDialogPane().setHeader(new Label());
		Node node = inputDialog.getDialogPane().lookupButton(ButtonType.OK);
		inputDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				node.setDisable(this.anyMatch(this.localBranchParent.getChildren(), newValue));
			}
		});
		inputDialog.showAndWait().ifPresent(this.model::newBranch);
	}

	public void renameBranch(ActionEvent actionEvent)
	{
		TreeItem<GitUtil.Branch> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
		GitUtil.Branch value = selectedItem.getValue();
		String oldName = value.getFullName();

		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setTitle(R.GIT_BRANCH_CONTR_ENTER_NEW_NAME.get());
		inputDialog.getDialogPane().setHeader(new Label());
		Node node = inputDialog.getDialogPane().lookupButton(ButtonType.OK);
		inputDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				node.setDisable(this.anyMatch(this.localBranchParent.getChildren(), newValue));
			}
		});
		Optional<String> newName = inputDialog.showAndWait();
		newName.ifPresent(name -> Common.tryCatch(() -> this.model.renameBranch(oldName, name), R.GIT_BRANCH_CONTR_ERROR_RENAME.get()));
	}

	public void checkoutBranch(ActionEvent actionEvent)
	{
		TreeItem<GitUtil.Branch> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
		GitUtil.Branch branch = selectedItem.getValue();
		String branchName = null;
		if (branch != null && !branch.isLocal())
		{
			TextInputDialog inputDialog = new TextInputDialog();
			inputDialog.setTitle(R.GIT_BRANCH_CONTR_ENTER_NAME.get());
			inputDialog.getEditor().setText(selectedItem.getValue().getSimpleName());
			inputDialog.getDialogPane().setHeader(new Label());
			Node node = inputDialog.getDialogPane().lookupButton(ButtonType.OK);
			inputDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue != null)
				{
					node.setDisable(this.anyMatch(this.localBranchParent.getChildren(), newValue));
				}
			});
			Optional<String> newName = inputDialog.showAndWait();
			if (newName.isPresent())
			{
				branchName = newName.get();
			}
			else
			{
				return;
			}
		}
		String finalBranchName = branchName;
		Common.tryCatch(() -> this.model.checkout(branch.getFullName(), finalBranchName), R.GIT_BRANCH_CONTR_ERROR_CHECKOUT.get());
	}

	public void mergeBranch(ActionEvent actionEvent)
	{

	}

	public void deleteBranch(ActionEvent actionEvent)
	{
		TreeItem<GitUtil.Branch> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
		GitUtil.Branch branch = selectedItem.getValue();
		Common.tryCatch(() -> this.model.deleteBranch(branch), R.GIT_BRANCH_CONTR_ERROR_DELETE.get());
	}

	//region private methods
	private boolean anyMatch(List<TreeItem<GitUtil.Branch>> children, String newValue)
	{
		return children.stream().map(TreeItem::getValue).map(GitUtil.Branch::getSimpleName).anyMatch(newValue::equals);
	}

	private void initTree()
	{
		this.treeView.setCellFactory(p -> new TreeCell<GitUtil.Branch>()
		{
			@Override
			protected void updateItem(GitUtil.Branch item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					String pre = item.isCurrent() ? "* " : "  ";
					setText(pre + item.getSimpleName());
				}
				else
				{
					setText("");
				}
			}
		});
		this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null)
			{
				Arrays.asList(this.btnCheckoutBranch, this.btnRenameBranch, this.btnMergeBranch, this.btnDeleteBranch).forEach(b -> b.setDisable(true));
			}
			else
			{
				GitUtil.Branch branch = newValue.getValue();
				Arrays.asList(this.btnCheckoutBranch, this.btnRenameBranch, this.btnMergeBranch, this.btnDeleteBranch)
						.forEach(b -> b.setDisable(branch.isCurrent() || (branch.getSimpleName().equals(REMOTE_NAME) || branch.getSimpleName().equals(LOCAL_NAME))));
				if (!branch.isLocal())
				{
					this.btnRenameBranch.setDisable(true);
				}
			}
		});

		TreeItem<GitUtil.Branch> root = new TreeItem<>();
		this.treeView.setRoot(root);
		this.treeView.setShowRoot(false);

		this.localBranchParent = new TreeItem<>(new GitUtil.Branch(false, false, LOCAL_NAME));
		this.remoteBranchParent = new TreeItem<>(new GitUtil.Branch(false, false, REMOTE_NAME));

		root.getChildren().addAll(this.localBranchParent, this.remoteBranchParent);
		this.localBranchParent.setExpanded(true);
		this.remoteBranchParent.setExpanded(true);

		this.treeView.getSelectionModel().selectFirst();
	}
	//endregion
}