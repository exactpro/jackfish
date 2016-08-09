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
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MergeEditorController implements Initializable, ContainingParent
{
	public Parent parent;
	public GridPane gridPane;
//	public TextArea taYour;
//	public TextArea taResult;
//	public TextArea taTheir;

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
	public void init(MergeEditor model, String fileName)
	{
		this.model = model;
		initDialog(fileName);
	}

	public void show()
	{
		this.dialog.setOnShown(e -> Common.tryCatch(() -> this.model.dialogShown(), "Error on dialog shown"));
		Optional<ButtonType> buttonType = this.dialog.showAndWait();
		System.out.println(buttonType);
	}

	private void initDialog(String fileName)
	{
		this.dialog = DialogsHelper.createGitDialog("Merge editor for file \'"+fileName+"\'", this.parent);
	}

	public void save(ActionEvent actionEvent)
	{
//		Common.tryCatch(() -> this.model.saveResult(this.lvResult.getItems().stream().map(MergeCell::getText).collect(Collectors.toList())), "Error on save result");
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

	@Deprecated
	public void displayYours(List<String> yours)
	{
//		this.lvYour.getItems().setAll(yours.stream().map(st -> new MergeCell(st, MergeCell.MergeType.WITHOUT_MOVE)).collect(Collectors.toList()));
	}

	@Deprecated
	public void displayTheirs(List<String> theirs)
	{
//		this.lvTheir.getItems().setAll(theirs.stream().map(st -> new MergeCell(st, MergeCell.MergeType.WITHOUT_MOVE)).collect(Collectors.toList()));
	}

	@Deprecated
	public void displayResult(List<String> result)
	{
//		this.lvResult.getItems().setAll(result.stream().map(st -> new MergeCell(st, MergeCell.MergeType.WITHOUT_MOVE)).collect(Collectors.toList()));
	}

	public void addLines(String yourText, String theirText, String resultText, boolean isConflict, int count)
	{
		TextArea taYour = new TextArea();
		taYour.setEditable(false);

		TextArea taResult = new TextArea();

		TextArea taTheir = new TextArea();
		taTheir.setEditable(false);

		RowConstraints rowConstraints = new RowConstraints();
		rowConstraints.setFillHeight(true);

		this.gridPane.getRowConstraints().add(rowConstraints);

		this.gridPane.add(taYour, 0, count);
		this.gridPane.add(taResult, 2, count);
		this.gridPane.add(taTheir, 4, count);

		if (isConflict)
		{
			ImageView acceptYour = new ImageView(new Image(CssVariables.Icons.GIT_ACCEPT_YOUR));
			acceptYour.setOnMouseClicked(e -> {
				//TODO implement
			});
			this.gridPane.add(acceptYour, 1, count);

			ImageView acceptTheir = new ImageView(new Image(CssVariables.Icons.GIT_ACCEPT_THEIR));
			this.gridPane.add(acceptTheir, 3, count);

			taYour.getStyleClass().addAll(CssVariables.GIT_MERGE_YOUR_CONFLICT);
			taTheir.getStyleClass().addAll(CssVariables.GIT_MERGE_THEIR_CONFLICT);
		}
		else
		{
			taYour.getStyleClass().addAll(CssVariables.GIT_MERGE_YOUR_CONFLICT);
			taResult.getStyleClass().addAll(CssVariables.GIT_MERGE_YOUR_CONFLICT);
			taTheir.getStyleClass().addAll(CssVariables.GIT_MERGE_THEIR_CONFLICT);
		}
		taYour.setText(yourText);
		taResult.setText(resultText);
		taTheir.setText(theirText);

		Arrays.asList(taYour, taTheir).forEach(ta -> {
			double value = ta.getParagraphs().size() * (ta.getFont().getSize() + 3) + 13;
			ta.setPrefHeight(value);
			ta.setMinHeight(value);
		});

		taResult.setPrefHeight(Math.max(taYour.getPrefHeight(), taTheir.getPrefHeight()));
		taResult.setMaxHeight(Math.max(taYour.getMaxHeight(), taTheir.getMaxHeight()));
		taResult.setMinHeight(Math.max(taYour.getMinHeight(), taTheir.getMinHeight()));
	}

	public void closeDialog()
	{
		this.dialog.close();
	}

	public void displayLines(List<Chunk> conflicts)
	{
//		Platform.runLater(() -> {
//			VirtualFlow vf1 = (VirtualFlow) this.lvYour.lookup(".virtual-flow");
//			VirtualFlow vf2 = (VirtualFlow) this.lvTheir.lookup(".virtual-flow");
//			for (Chunk conflict : conflicts)
//			{
//				int firstStart = conflict.getFirstStart();
//				int firstLength = conflict.getFirstLength();
//				IntStream.range(firstStart, firstStart + firstLength).forEach(i -> {
//					IndexedCell cell = vf1.getCell(i);
//					cell.getStyleClass().add(CssVariables.CHANGES_LINE);
//				});
//
//				int secondStart = conflict.getSecondStart();
//				int secondLength = conflict.getSecondLength();
//				IntStream.range(secondStart, secondStart + secondLength).forEach(i -> {
//					IndexedCell cell = vf2.getCell(i);
//					cell.getStyleClass().add(CssVariables.CHANGES_LINE);
//				});
//			}
//		});
	}
}