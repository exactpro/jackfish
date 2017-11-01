////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.find;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FindPanel<T> extends BorderPane
{
	private CustomFieldWithButton cfFind;
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
		this.getStyleClass().add(CssVariables.FIND_PANEL);
		this.cfFind = new CustomFieldWithButton();
		this.btnPrevious = new Button();
		this.btnNext = new Button();
		this.checkBoxMatchCase = new CheckBox(R.FIND_PANEL_MATCH_CASE.get());
		this.checkBoxWords = new CheckBox(R.FIND_PANEL_MATCH_CASE_WHOLE_WORD.get());
		this.lblFind = new Label(R.FIND_PANEL_MATCH_CASE_FOUND_ZERO.get());
		this.lblFind.setPrefWidth(100);
		this.lblFind.setMaxWidth(100);
		this.lblFind.setMinWidth(100);
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
		this.cfFind.requestFocus();
	}

	public void setListener(IFind<T> iFind)
	{
		this.iFind = iFind;
	}

	//region private methods
	private void createPane()
	{
		BorderPane.setAlignment(cfFind, Pos.CENTER);
		this.setCenter(cfFind);

		Label find = new Label(R.FIND_PANEL_MATCH_CASE_FIND.get());
		BorderPane.setMargin(find, new Insets(0, 10, 0, 0));
		BorderPane.setAlignment(find, Pos.CENTER);
		this.setLeft(find);

		HBox hBox = new HBox();
		hBox.setAlignment(Pos.CENTER_LEFT);
		hBox.setSpacing(10);
		HBox.setMargin(this.btnPrevious, new Insets(0, 0, 0, 10));
		hBox.getChildren().addAll(this.btnPrevious, this.btnNext, this.checkBoxMatchCase, this.checkBoxWords, this.lblFind, new Label());
		BorderPane.setAlignment(hBox, Pos.CENTER_LEFT);
		this.setRight(hBox);
	}

	private void initialize()
	{
		Common.runLater(() ->
		{
			btnPrevious.setTooltip(new Tooltip(R.FIND_PANEL_MATCH_CASE_FIND_PREVIOUS.get()));
			btnNext.setTooltip(new Tooltip(R.FIND_PANEL_MATCH_CASE_FIND_NEXT.get()));
			Common.customizeLabeled(btnNext, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.FIND_NEXT);
			Common.customizeLabeled(btnPrevious, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.FIND_PREVIOUS);
		});

		this.cfFind.textProperty().addListener((observableValue, s, t1) -> Optional.ofNullable(this.iFind).ifPresent(findPanel -> {
			if (!t1.isEmpty())
			{
				this.findElements(t1);
			}
			else
			{
				this.results.clear();
				this.lblFind.setText(R.FIND_PANEL_MATCH_CASE_FOUND_ZERO.get());
			}
		}));

		cfFind.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.F3 && keyEvent.isShiftDown())
			{
				this.btnPrevious.fire();
			}
			else if (keyEvent.getCode() == KeyCode.F3)
			{
				this.btnNext.fire();
			}
		});

		Stream.of(this.checkBoxWords, this.checkBoxMatchCase).forEach(cb -> cb.setOnAction(event -> {
			if (!this.cfFind.getText().isEmpty())
			{
				findElements(this.cfFind.getText());
			}
		}));

		this.btnPrevious.setOnAction(actionEvent ->
		{
			if (this.iFind != null && !results.isEmpty())
			{
				this.currentElement--;
				if (this.currentElement < 0)
				{
					this.currentElement = this.results.size() - 1;
				}
				this.iFind.find(this.results.get(this.currentElement));
				this.lblFind.setText(MessageFormat.format(R.FIND_PANEL_MATCH_CASE_FIND_FOUND_2.get(), this.currentElement + 1, this.results.size()));
			}
		});

		this.btnNext.setOnAction(actionEvent ->
		{
			if (this.iFind != null && !this.results.isEmpty())
			{
				this.currentElement++;
				if (this.currentElement == this.results.size())
				{
					this.currentElement = 0;
				}
				this.iFind.find(this.results.get(this.currentElement));
				this.lblFind.setText(MessageFormat.format(R.FIND_PANEL_MATCH_CASE_FIND_FOUND_2.get(), this.currentElement + 1, this.results.size()));
			}
		});

		this.results = new ArrayList<>();
	}

	private void findElements(String text)
	{
		this.results = this.iFind.findItem(text, checkBoxMatchCase.isSelected(), checkBoxWords.isSelected());
		this.currentElement = -1;
		if (this.results.isEmpty())
		{
			this.lblFind.setText(R.FIND_PANEL_MATCH_CASE_FOUND_ZERO.get());
			if (!lblFind.getStyleClass().contains(CssVariables.INCORRECT_FIELD))
			{
				this.lblFind.getStyleClass().add(CssVariables.INCORRECT_FIELD);
			}
			this.lblFind.getStyleClass().remove(CssVariables.CORRECT_FIELD);
		}
		else
		{
			this.lblFind.setText(MessageFormat.format(R.FIND_PANEL_MATCH_CASE_FIND_FOUND_1.get(), this.results.size()));
			if (!this.lblFind.getStyleClass().contains(CssVariables.CORRECT_FIELD))
			{
				this.lblFind.getStyleClass().add(CssVariables.CORRECT_FIELD);
			}
			this.lblFind.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
		}
		this.lblFind.setTooltip(new Tooltip(this.lblFind.getText()));
	}

	//endregion
}
