////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge.editor;

import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.List;
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
		this.dialog.setResult(ButtonType.YES);
		this.dialog.close();
	}

	public void close(ActionEvent actionEvent)
	{
		this.dialog.setResult(ButtonType.CANCEL);
		this.dialog.close();
	}

	public void displayYours(List<String> yours)
	{
		this.taYour.setText(yours.stream().reduce((s, s2) -> s + System.lineSeparator() + s2).orElse(""));
	}

	public void displayTheirs(List<String> theirs)
	{
		this.taTheir.setText(theirs.stream().reduce((s, s2) -> s + System.lineSeparator() + s2).orElse(""));
	}
}