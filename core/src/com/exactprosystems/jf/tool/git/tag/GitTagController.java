////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.git.tag;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GitTagController implements Initializable, ContainingParent
{
	public Parent parent;
	public Button btnDeleteTag;
	public ListView<GitUtil.Tag> listView;
	public Button btnPushTags;
	public Button btnNewTag;
	public Button btnClose;
	private GitTag model;
	private Alert dialog;


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

	void init(GitTag model)
	{
		this.model = model;
		this.dialog = DialogsHelper.createGitDialog("Tags", this.parent);
		this.listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				this.btnDeleteTag.setDisable(false);
			}
		});
		this.listView.setCellFactory(p -> new ListCell<GitUtil.Tag>() {
			@Override
			protected void updateItem(GitUtil.Tag item, boolean empty)
			{
				super.updateItem(item, empty);
				if (!empty && item != null)
				{
					setText(item.getName());
				}
				else
				{
					setText(null);
				}
			}
		});
	}

	void show()
	{
		this.dialog.showAndWait();

	}

	public void hide(ActionEvent event)
	{
		this.dialog.hide();
	}

	void updateTags(List<GitUtil.Tag> list)
	{
		this.listView.getItems().setAll(list);
		this.listView.getSelectionModel().selectFirst();
	}

	void setDisable(boolean flag)
	{
		this.listView.setDisable(flag);
		this.btnDeleteTag.setDisable(flag);
		this.btnPushTags.setDisable(flag);
		this.btnNewTag.setDisable(flag);
		this.btnClose.setDisable(flag);
	}

	public void deleteTag(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.deleteTag(this.listView.getSelectionModel().getSelectedItem().getFullname()), "Error on delete tag");
	}

	public void newTag(ActionEvent actionEvent)
	{
		TextInputDialog versionDialog = new TextInputDialog();
		versionDialog.setTitle("Enter the version");
		versionDialog.getDialogPane().setHeader(new Label());
		Node node = versionDialog.getDialogPane().lookupButton(ButtonType.OK);
		versionDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				node.setDisable(this.listView.getItems()
						.stream()
						.map(GitUtil.Tag::getName)
						.anyMatch(newValue::equals)
				);
			}
		});
		Optional<String> version = versionDialog.showAndWait();
		if (!version.isPresent())
		{
			return;
		}
		TextInputDialog messageDialog = new TextInputDialog();
		messageDialog.setTitle("Enter the message");
		messageDialog.getDialogPane().setHeader(new Label());
		Optional<String> msg = messageDialog.showAndWait();
		if (!msg.isPresent())
		{
			return;
		}
		Common.tryCatch(() -> this.model.newTag(version.get(), msg.get()), "Error on delete tag");
	}

	public void pushTag(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.pushTag(), "Error on push tag");
	}
}