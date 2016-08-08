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
import com.exactprosystems.jf.tool.git.VBoxProgressMonitor;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

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
	public CustomFieldWithButton cfLocation;
	public TextField tfProjectName;
	public CheckBox cbOpenProject;
	public Button btnCancel;
	public Button btnClone;

	public ScrollPane scrollPane;
	public VBox vBox;

	private GitClone model;
	private GridPane parent;
	private BooleanProperty folderExist = new SimpleBooleanProperty(false);
	private BooleanProperty projectExist = new SimpleBooleanProperty(false);

	private final BooleanBinding binding = this.folderExist.not().or(this.projectExist.not());

	private final Pattern pattern = Pattern.compile(".*?[:/](\\w+)\\.git$");

	private VBoxProgressMonitor monitor;

	private Alert dialog;

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = (GridPane) parent;
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

		this.monitor = new VBoxProgressMonitor(this.vBox);
		this.btnClone.disableProperty().bind(this.binding);
	}
	//endregion

	public void init(GitClone model)
	{
		this.model = model;
		initDialog();
	}

	public void cloneProject(ActionEvent actionEvent)
	{
		this.displayStatus(true);
		Common.tryCatch(() -> this.model.cloneProject(this.cfLocation.getText(), this.tfURI.getText(), this.tfProjectName.getText(), this.cbOpenProject.isSelected(), this.monitor), "Error on clone project");
	}

	public void setDisable(boolean flag)
	{
		this.btnCancel.setText(flag ? "Cancel" : "Close");
		if (flag)
		{
			this.btnClone.disableProperty().unbind();
			this.btnClone.setDisable(true);
		}
		else
		{
			this.btnClone.setDisable(this.binding.getValue());
			this.btnClone.disableProperty().bind(this.binding);
			String oldName = this.tfProjectName.getText();
			this.tfProjectName.setText("");
			this.tfProjectName.setText(oldName);
			String oldLoc = this.cfLocation.getText();
			this.cfLocation.setText("");
			this.cfLocation.setText(oldLoc);
		}
		this.tfProjectName.setDisable(flag);
		this.tfURI.setDisable(flag);
		this.cfLocation.setDisable(flag);
		this.cbOpenProject.setDisable(flag);
	}

	public void cancel(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::cancel, "Error on cancel");
	}

	public void show()
	{
		this.dialog.showAndWait();
	}

	public void hide()
	{
		this.dialog.hide();
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

	private void initDialog()
	{
		this.dialog = DialogsHelper.createGitDialog("Clone project", this.parent);
		displayStatus(false);
	}

	private void displayStatus(boolean flag)
	{
		if (flag)
		{
			this.monitor.clear();
		}
		this.scrollPane.setVisible(flag);
		RowConstraints rowStatus = this.parent.getRowConstraints().get(4);

		rowStatus.setMaxHeight(flag ? 100 : 0);
		rowStatus.setMinHeight(flag ? 10 : 0);
		rowStatus.setPrefHeight(flag ? 30 : 0);
		rowStatus.setPercentHeight(flag ? 30 : 0);
	}
	//endregion

}
