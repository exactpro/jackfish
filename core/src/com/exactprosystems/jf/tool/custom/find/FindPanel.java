////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.find;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// TODO it needs code review
public class FindPanel<T> extends BorderPane
{
	private TextField	tfFind;
	private Button		btnPrevious;
	private Button		btnNext;
	private CheckBox	checkBoxMatchCase;
	private CheckBox	checkBoxWords;
	private Label		lblFind;

	private IFind<T>	iFind;
	private List<T>		results;
	private int			currentElement;

	public FindPanel()
	{
		this.tfFind = new TextField();
		this.btnPrevious = new Button();
		this.btnNext = new Button();
		this.checkBoxMatchCase = new CheckBox("Match case");
		this.checkBoxWords = new CheckBox("Whole word");
		this.lblFind = new Label();
		createPane();
		initialize();
	}

	public FindPanel(IFind<T> iFind)
	{
		this();
		this.iFind = iFind;
	}

	@Override
	public void requestFocus()
	{
		super.requestFocus();
		this.tfFind.requestFocus();
	}

	public void setListener(IFind<T> iFind)
	{
		this.iFind = iFind;
	}

	// ============================================================
	// private methods
	// ============================================================
	private void createPane()
	{
		BorderPane.setAlignment(tfFind, Pos.CENTER);
		this.setCenter(tfFind);
		Label find = new Label("Find");
		BorderPane.setMargin(find, new Insets(0, 10, 0, 0));
		BorderPane.setAlignment(find, Pos.CENTER);
		this.setLeft(find);

		HBox hBox = new HBox();
		hBox.setAlignment(Pos.CENTER_LEFT);
		hBox.setSpacing(10);
		HBox.setMargin(btnPrevious, new Insets(0, 0, 0, 10));
		hBox.getChildren().addAll(btnPrevious, btnNext, checkBoxMatchCase, checkBoxWords, lblFind);
		BorderPane.setAlignment(hBox, Pos.CENTER);
		this.setRight(hBox);
	}

	private void initialize()
	{
		Platform.runLater(() ->
		{
			btnPrevious.setTooltip(new Tooltip("Previous"));
			btnNext.setTooltip(new Tooltip("Next"));
			Common.customizeLabeled(btnNext, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.FIND_NEXT);
			Common.customizeLabeled(btnPrevious, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.FIND_PREVIOUS);
		});

		tfFind.textProperty().addListener((observableValue, s, t1) -> Optional.ofNullable(iFind).ifPresent(findPanel -> {
			if (!t1.isEmpty())
			{
				findElements(t1);
			}
		}));

		tfFind.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.F3 && keyEvent.isShiftDown())
			{
				btnPrevious.fire();
			}
			else if (keyEvent.getCode() == KeyCode.F3)
			{
				btnNext.fire();
			}
		});
		Arrays.asList(checkBoxWords, checkBoxMatchCase).stream().forEach(cb -> cb.setOnAction(event -> {
			if (!tfFind.getText().isEmpty())
			{
				findElements(tfFind.getText());
			}
		}));
		btnPrevious.setOnAction(actionEvent ->
		{
			if (iFind != null && !results.isEmpty())
			{
				currentElement--;
				if (currentElement < 0)
				{
					currentElement = results.size() - 1;
				}
				iFind.find(results.get(currentElement));
			}
		});

		btnNext.setOnAction(actionEvent ->
		{
			if (iFind != null && !results.isEmpty())
			{
				currentElement++;
				if (currentElement == results.size())
				{
					currentElement = 0;
				}
				iFind.find(results.get(currentElement));
			}
		});
		results = new ArrayList<>();
	}

	private void findElements(String text)
	{
		results = iFind.findItem(text, checkBoxMatchCase.isSelected(), checkBoxWords.isSelected());
		currentElement = -1;
		if (results.isEmpty())
		{
			lblFind.setText("No matches");
			if (!lblFind.getStyleClass().contains(CssVariables.INCORRECT_FIELD))
			{
				lblFind.getStyleClass().add(CssVariables.INCORRECT_FIELD);
			}
			lblFind.getStyleClass().remove(CssVariables.CORRECT_FIELD);
		}
		else
		{
			lblFind.setText("Found " + results.size());
			if (!lblFind.getStyleClass().contains(CssVariables.CORRECT_FIELD))
			{
				lblFind.getStyleClass().add(CssVariables.CORRECT_FIELD);
			}
			lblFind.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
		}
	}
}
