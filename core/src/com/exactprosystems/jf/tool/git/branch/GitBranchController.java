package com.exactprosystems.jf.tool.git.branch;

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
	private static final String LOCAL = "Local";
	private static final String REMOTE = "Remote";
	public Parent parent;
	public TreeView<GitUtil.Branch> treeView;
	public Button btnNewBranch;
	public Button btnRenameBranch;
	public Button btnCheckoutBranch;
	public Button btnMergeBranch;
	public Button btnDeleteBranch;
	public VBox vBox;
	public Button btnClose;

	private GitBranch model;
	private Alert dialog;

	private TreeItem<GitUtil.Branch> local;
	private TreeItem<GitUtil.Branch> remote;


	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}
	//endregion

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
		this.dialog = DialogsHelper.createGitDialog("Branches", this.parent);
		initTree();
	}

	void show()
	{
		this.dialog.showAndWait();
	}

	void updateBranches(List<GitUtil.Branch> branches)
	{
		this.local.getChildren().clear();
		this.remote.getChildren().clear();
		branches.forEach(b -> {
			if (b.isLocal())
			{
				local.getChildren().add(new TreeItem<>(b));
			}
			else
			{
				remote.getChildren().add(new TreeItem<>(b));
			}
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
			this.treeView.getSelectionModel().select(local);
		}
	}

	public void hide(ActionEvent event)
	{
		this.dialog.hide();
	}

	public void newBranch(ActionEvent actionEvent)
	{
		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setTitle("Enter branch name");
		inputDialog.getDialogPane().setHeader(new Label());
		Node node = inputDialog.getDialogPane().lookupButton(ButtonType.OK);
		inputDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				node.setDisable(anyMatch(local.getChildren(), newValue) || anyMatch(remote.getChildren(), newValue));
			}
		});
		Optional<String> newName = inputDialog.showAndWait();
		newName.ifPresent(name -> Common.tryCatch(() -> this.model.newBranch(name), "Error on create new branch"));
	}

	private boolean anyMatch(List<TreeItem<GitUtil.Branch>> children, String newValue)
	{
		return children.stream()
				.map(TreeItem::getValue)
				.map(GitUtil.Branch::getName)
				.anyMatch(newValue::equals);
	}

	public void renameBranch(ActionEvent actionEvent)
	{
		TreeItem<GitUtil.Branch> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
		GitUtil.Branch value = selectedItem.getValue();
		String oldName = value.getName();

		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setTitle("Enter new name");
		inputDialog.getDialogPane().setHeader(new Label());
		Node node = inputDialog.getDialogPane().lookupButton(ButtonType.OK);
		inputDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				node.setDisable(anyMatch(local.getChildren(), newValue) || anyMatch(remote.getChildren(), newValue));
			}
		});
		Optional<String> newName = inputDialog.showAndWait();
		newName.ifPresent(name -> Common.tryCatch(() -> this.model.renameBranch(oldName, name), "Error on rename branch"));
	}

	public void checkoutBranch(ActionEvent actionEvent)
	{
		TreeItem<GitUtil.Branch> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
		GitUtil.Branch branch = selectedItem.getValue();
		String branchName = null;
		if (!branch.isLocal())
		{
			TextInputDialog inputDialog = new TextInputDialog();
			inputDialog.setTitle("Enter branch name");
			inputDialog.getDialogPane().setHeader(new Label());
			Node node = inputDialog.getDialogPane().lookupButton(ButtonType.OK);
			inputDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue != null)
				{
					node.setDisable(anyMatch(local.getChildren(), newValue) || anyMatch(remote.getChildren(), newValue));
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
		Common.tryCatch(() -> this.model.checkout(branch.getName(), finalBranchName), "Error on checkout");
	}

	public void mergeBranch(ActionEvent actionEvent)
	{

	}

	public void deleteBranch(ActionEvent actionEvent)
	{
		TreeItem<GitUtil.Branch> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
		GitUtil.Branch branch = selectedItem.getValue();
		Common.tryCatch(() -> this.model.deleteBranch(branch), "Error on delete branch");
	}

	private void initTree()
	{
		this.treeView.setCellFactory(p -> new TreeCell<GitUtil.Branch>(){
			@Override
			protected void updateItem(GitUtil.Branch item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					String pre = item.isCurrent() ? "* " : "  ";
					setText(pre + item.getName());
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
				Arrays.asList(this.btnCheckoutBranch, this.btnRenameBranch, this.btnMergeBranch, this.btnDeleteBranch).forEach(b -> {
					b.setDisable(branch.isCurrent() || (branch.getName().equals(REMOTE) || branch.getName().equals(LOCAL)));
				});
				if (!branch.isLocal())
				{
					this.btnRenameBranch.setDisable(true);
				}
			}
		});

		TreeItem<GitUtil.Branch> root = new TreeItem<>();
		this.treeView.setRoot(root);
		this.treeView.setShowRoot(false);

		this.local = new TreeItem<>(new GitUtil.Branch(false, false, "Local"));
		this.remote = new TreeItem<>(new GitUtil.Branch(false, false, "Remote"));

		root.getChildren().addAll(this.local, this.remote);
		this.local.setExpanded(true);
		this.remote.setExpanded(true);

		this.treeView.getSelectionModel().selectFirst();
	}
}