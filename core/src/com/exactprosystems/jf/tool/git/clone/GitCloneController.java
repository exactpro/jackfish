////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.clone;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class GitCloneController implements Initializable, ContainingParent
{
	public TextField tfURI;
	public TextField tfUserName;
	public CustomFieldWithButton cfLocation;
	public PasswordField pfPassword;

	private GitClone model;
	private Parent parent;
	private BooleanProperty folderExist = new SimpleBooleanProperty(false);

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.cfLocation.setButtonText("...");
		this.cfLocation.setHandler(event -> {
			File file = DialogsHelper.showDirChooseDialog("Choose folder to clone project", this.cfLocation.getText());
			Optional.ofNullable(file).ifPresent(f -> this.cfLocation.setText(f.getAbsolutePath()));
		});
		this.cfLocation.textProperty().addListener((observable, oldValue, newValue) -> {
			this.cfLocation.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
			if (newValue != null && !newValue.isEmpty())
			{
				folderExist.setValue(true);
				if (!new File(newValue).exists())
				{
					this.cfLocation.getStyleClass().add(CssVariables.INCORRECT_FIELD);
					folderExist.setValue(false);
				}
			}
			else
			{
				folderExist.setValue(false);
			}
		});
	}
	//endregion

	public void init(GitClone model)
	{
		this.model = model;
	}

	public boolean display()
	{
		Dialog<Boolean> dialog = new Dialog<>();
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		dialog.setTitle("Clone project");
		dialog.getDialogPane().setHeader(new Label());
		dialog.getDialogPane().setContent(this.parent);
		dialog.setResultConverter(param -> {
			this.model.setPassword(this.pfPassword.getText().toCharArray());
			this.model.setUserName(this.tfUserName.getText());
			this.model.setRemotePath(this.tfURI.getText());
			this.model.setProjectLocation(this.cfLocation.getText());
			return param.getButtonData().equals(ButtonBar.ButtonData.OK_DONE);
		});

		ButtonType buttonCreate = new ButtonType("Clone", ButtonBar.ButtonData.OK_DONE);
		ButtonType buttonClose = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().setAll(buttonCreate, buttonClose);
		dialog.getDialogPane().lookupButton(buttonCreate).disableProperty().bind(folderExist.not());
		return dialog.showAndWait().orElse(false);
	}
}
