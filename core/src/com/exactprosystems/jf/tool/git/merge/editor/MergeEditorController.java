////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge.editor;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MergeEditorController implements Initializable, ContainingParent
{
	public Parent parent;
	public TextArea taYour;
	public TextArea taResult;
	public TextArea taTheir;

	private MergeEditor model;
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
	public void init(MergeEditor model)
	{
		this.model = model;
		initDialog();
	}

	public void show()
	{
		Optional<ButtonType> buttonType = this.dialog.showAndWait();
		System.out.println(buttonType);
	}

	private void initDialog()
	{
		this.dialog = DialogsHelper.createGitDialog("Merge editor", this.parent);
	}

	public void save(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.saveResult(this.taResult.getText()), "Error on save result");
	}

	public void close(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.close(), "Error on close");
	}

	public void acceptYours(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.acceptYours(), "Error on accept yours");
	}

	public void acceptTheirs(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.acceptTheirs(), "Error on accept theirs");
	}

	public void displayYours(String yours)
	{
		this.taYour.setText(yours);
	}

	public void displayTheirs(String theirs)
	{
		this.taTheir.setText(theirs);
	}

	public void displayResult(String result)
	{
		this.taResult.setText(result);
	}

	public void closeDialog()
	{
		this.dialog.close();
	}
}