////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.status;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.git.GitBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.CompareEnum;
import com.exactprosystems.jf.tool.newconfig.nodes.BuildTree;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GitStatusController implements Initializable, ContainingParent
{
	public Parent parent;
	public BorderPane borderPane;
	public TextField tfPattern;

	private TreeView<GitBean> treeView;
	private GitStatus model;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.tfPattern.textProperty().addListener((observable, oldValue, newValue) -> {
			List<GitBean> list = new ArrayList<>();
			byPass(this.treeView.getRoot(), list, p -> true);
			list.forEach(gb -> gb.setMatch(false));

			Common.tryCatch(() ->
			{
				if (newValue != null && !newValue.isEmpty())
				{
					List<Path> pathList = new ArrayList<>();
					byPassDirectory(GitUtil.gitRootDirectory(GitUtil.EMPTY_BEAN), pathList::add, newValue);
					List<String> filePaths = pathList.stream().map(Path::toFile).map(File::getAbsolutePath).collect(Collectors.toList());

					List<GitBean> beanList = new ArrayList<>();
					byPass(this.treeView.getRoot(), beanList, gb ->
					{
						try
						{
							return filePaths.contains(new File(GitUtil.checkFile(GitUtil.EMPTY_BEAN, gb.getFile().getPath())).getAbsolutePath());
						}
						catch (Exception e)
						{
							//nothing;
						}
						return false;
					});
					beanList.forEach(gb -> gb.setMatch(true));
				}
			}, "Error");
			refreshTree();
		});
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	//region event methods
	public void revertSelected(ActionEvent actionEvent)
	{
		Set<String> set = new HashSet<>();
		byPass(this.treeView.getRoot(), treeItem -> collect(set, treeItem));
		Common.tryCatch(() -> this.model.revertPaths(set), "Error on revert selected items");
	}

	private void byPass(TreeItem<GitBean> item, Consumer<TreeItem<GitBean>> consumer)
	{
		consumer.accept(item);
		item.getChildren().forEach(i -> byPass(i, consumer));
	}

	public void ignoreSelected(ActionEvent actionEvent)
	{
		List<GitBean> list = new ArrayList<>();
		byPass(this.treeView.getRoot(), list, GitBean::isChecked);
		Common.tryCatch(() -> this.model.ignoreFiles(list.stream().map(GitBean::getFile).collect(Collectors.toList())), "Error on revert selected items");
	}

	public void ignoreByPattern(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.ignoreByPattern(this.tfPattern.getText()), "Error on revert selected items");
	}

	public void select(ActionEvent actionEvent)
	{
		List<GitBean> list = new ArrayList<>();
		byPass(this.treeView.getRoot(), list, GitBean::isMatch);
		list.forEach(gb -> gb.setChecked(true));
		refreshTree();
	}
	//endregion


	void init(GitStatus model, File rootDirectory)
	{
		this.model = model;
		this.initTree(rootDirectory);
	}

	void display(List<GitBean> list, String state)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.setTitle("Git status");
		Text headerLabel = new Text(state);
		if (list.isEmpty())
		{
			headerLabel.setText(headerLabel.getText() + " , Already up-to-date");
		}
		BorderPane pane = new BorderPane();
		pane.setCenter(headerLabel);
		dialog.getDialogPane().setHeader(pane);
		dialog.getDialogPane().setContent(this.parent);
		updateFiles(list);
		dialog.showAndWait();
	}

	void updateFiles(List<GitBean> list)
	{
		displayTree(list);
	}

	//region private methods
	private static void byPassDirectory(File path, Consumer<Path> consumer, String pattern) throws Exception
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path.toPath(), pattern))
		{
			stream.forEach(consumer);
		}

		File[] files = path.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isDirectory())
				{
					byPassDirectory(file, consumer, pattern);
				}
			}
		}
	}

	private void refreshTree()
	{
		boolean expanded = this.treeView.getRoot().isExpanded();
		this.treeView.getRoot().setExpanded(!expanded);
		this.treeView.getRoot().setExpanded(expanded);
	}

	private void collect(Set<String> list, TreeItem<GitBean> treeItem)
	{
		boolean isParent = !treeItem.getChildren().isEmpty();
		boolean isChecked = treeItem.getValue().isChecked();

		if (isParent && isChecked)
		{
			collectAllFromDir(list, treeItem);
		}
		else if (isChecked)
		{
			list.add(treeItem.getValue().getFile().getPath());
		}
	}

	private void collectAllFromDir(Set<String> list, TreeItem<GitBean> treeItem)
	{
		for (TreeItem<GitBean> item : treeItem.getChildren())
		{
			boolean isParent = !item.getChildren().isEmpty();
			if (isParent)
			{
				collectAllFromDir(list, item);
			}
			else
			{
				list.add(item.getValue().getFile().getPath());
			}
		}
	}

	private void displayTree(List<GitBean> list)
	{
		this.treeView.getRoot().getChildren().clear();
		for (GitBean gitBean : list)
		{
			List<GitBean> parents = getParents(gitBean);
			TreeItem<GitBean> current = this.treeView.getRoot();
			for (GitBean bean : parents)
			{
				Optional<TreeItem<GitBean>> first = current.getChildren().stream().filter(t -> t.getValue().equals(bean)).findFirst();
				if (first.isPresent())
				{
					current = first.get();
				}
				else
				{
					TreeItem<GitBean> treeItem = new TreeItem<>(new GitBean(bean.getStatus(), bean.getFile()));
					current.getChildren().add(treeItem);
					current = treeItem;
				}
			}
		}
		sort(this.treeView.getRoot());
	}

	private List<GitBean> getParents(GitBean gitBean)
	{
		List<GitBean> res = new ArrayList<>();
		res.add(gitBean);
		GitBean parent = new GitBean(GitBean.Status.EMPTY, gitBean.getFile().getParentFile());
		while (parent.getFile() != null && !parent.equals(this.treeView.getRoot().getValue()))
		{
			res.add(parent);
			parent = new GitBean(GitBean.Status.EMPTY, parent.getFile().getParentFile());
		}
		Collections.reverse(res);
		return res;
	}

	private void sort(TreeItem<GitBean> bean)
	{
		ObservableList<TreeItem<GitBean>> children = FXCollections.observableArrayList(bean.getChildren());
		bean.getChildren().clear();
		children.sort((t1,t2) -> {
			File f1 = t1.getValue().getFile();
			File f2 = t2.getValue().getFile();
			return CompareEnum.ALPHABET_0_1.getComparator().compare(f1, f2);
		});
		bean.getChildren().setAll(children);
		BuildTree.addListenerToExpandChild(bean);
		bean.getChildren().forEach(this::sort);
	}

	private void initTree(File rootDirectory)
	{
		this.treeView = new TreeView<>();
		this.treeView.setCellFactory(p -> new GitStatusTreeCell());
		this.borderPane.setCenter(this.treeView);

		TreeItem<GitBean> root = new TreeItem<>(new GitBean(GitBean.Status.EMPTY, rootDirectory));
		root.setExpanded(true);
		this.treeView.setRoot(root);
	}

	private void byPass(TreeItem<GitBean> root, List<GitBean> list, Predicate<GitBean> predicate)
	{
		GitBean value = root.getValue();
		if (predicate.test(value))
		{
			list.add(value);
		}
		root.getChildren().forEach(t -> byPass(t, list, predicate));
	}
	//endregion

	//region private classes
	private class GitStatusTreeCell extends TreeCell<GitBean>
	{
		@Override
		protected void updateItem(GitBean item, boolean empty)
		{
			super.updateItem(item, empty);
			this.getStyleClass().remove(CssVariables.GIT_MATCHED_FILE);
			if (item != null && !empty)
			{
				BorderPane pane = new BorderPane();

				CheckBox box = new CheckBox();
				box.setSelected(item.isChecked());
				box.selectedProperty().addListener((observable, oldValue, newValue) -> item.setChecked(newValue));

				Label itemFile = new Label(item.getFile().getName());
				itemFile.getStyleClass().addAll(item.getStatus().getStyleClass());
				if (item.isMatch() && !this.getStyleClass().contains(CssVariables.GIT_MATCHED_FILE))
				{
					this.getStyleClass().add(CssVariables.GIT_MATCHED_FILE);
				}
				BorderPane.setAlignment(itemFile, Pos.CENTER_LEFT);
				pane.setLeft(box);
				pane.setCenter(itemFile);
				setGraphic(pane);
			}
			else
			{
				setGraphic(null);
			}
		}
	}
	//endregion
}
