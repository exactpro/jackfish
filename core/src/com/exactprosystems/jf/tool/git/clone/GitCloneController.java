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
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitCloneController implements Initializable, ContainingParent
{
	public TextField tfURI;
	public TextField tfUserName;
	public CustomFieldWithButton cfLocation;
	public PasswordField pfPassword;
	public TextField tfProjectName;
	public CheckBox cbOpenProject;

	private GitClone model;
	private Parent parent;
	private BooleanProperty folderExist = new SimpleBooleanProperty(false);
	private BooleanProperty projectExist = new SimpleBooleanProperty(false);

	private final Pattern pattern = Pattern.compile(".*?[:/](\\w+)\\.git$");

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
			checkLocation(newValue);
			checkProjectName(this.tfProjectName.getText());
		});
		this.tfURI.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue.isEmpty())
			{
				Matcher matcher = pattern.matcher(newValue);
				if (matcher.find())
				{
					this.tfProjectName.setText(matcher.group(1));
				}
			}
			else
			{
				this.tfProjectName.setText("");
			}
		});

		this.tfProjectName.textProperty().addListener((observable, oldValue, newValue) -> {
			checkProjectName(newValue);
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
			boolean ret = param.getButtonData().equals(ButtonBar.ButtonData.OK_DONE);
			if (ret)
			{
				this.model.setPassword(this.pfPassword.getText());
				this.model.setUserName(this.tfUserName.getText());
				this.model.setProjectName(this.tfProjectName.getText());
				this.model.setRemotePath(this.tfURI.getText());
				this.model.setOpenProject(this.cbOpenProject.isSelected());
				this.model.setProjectLocation(this.cfLocation.getText());
			}
			return ret;
		});

		ButtonType buttonCreate = new ButtonType("Clone", ButtonBar.ButtonData.OK_DONE);
		ButtonType buttonClose = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().setAll(buttonCreate, buttonClose);
		dialog.getDialogPane().lookupButton(buttonCreate).disableProperty().bind(folderExist.not().or(projectExist.not()));
		return dialog.showAndWait().orElse(false);
	}

	//region private methods
	private void checkLocation(String newValue)
	{
		this.cfLocation.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
		if (newValue != null && !newValue.isEmpty())
		{
			this.folderExist.setValue(true);
			if (!new File(newValue).exists())
			{
				this.cfLocation.getStyleClass().add(CssVariables.INCORRECT_FIELD);
				this.folderExist.setValue(false);
			}
		}
		else
		{
			this.folderExist.setValue(false);
		}
	}

	private void checkProjectName(String newValue)
	{
		this.tfProjectName.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
		if (newValue != null && !newValue.isEmpty())
		{
			String pathToLocation = this.cfLocation.getText();
			if (pathToLocation != null && !pathToLocation.isEmpty())
			{
				this.projectExist.setValue(true);
				File folderLocation = new File(pathToLocation);
				Optional.ofNullable(folderLocation.list()).ifPresent(list -> Arrays.asList(list).stream().filter(this.tfProjectName.getText()::equals).findFirst().ifPresent(s -> {
					this.tfProjectName.getStyleClass().add(CssVariables.INCORRECT_FIELD);
					this.projectExist.setValue(false);
				}));
			}
		}
		else
		{
			this.projectExist.setValue(false);
		}
	}
	//endregion
}
