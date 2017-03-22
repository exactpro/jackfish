////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.number;

import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class NumberSpinner extends HBox
{
	private int step;
	private NumberTextField numberField;

	public NumberSpinner()
	{
		this(new NumberTextField());
	}

	public NumberSpinner(NumberTextField numberField)
	{
		this(numberField, 1);
	}

	public NumberSpinner(NumberTextField numberField, int step)
	{
		super();
		this.getStylesheets().add(Theme.GENERAL.getPath());
		this.numberField = numberField;
		this.step = step;
		numberField.getStyleClass().add(CssVariables.NUMBER_FIELD);

		numberField.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
			if (keyEvent.getCode() == KeyCode.DOWN) {
				decrement();
				keyEvent.consume();
			}
			if (keyEvent.getCode() == KeyCode.UP) {
				increment();
				keyEvent.consume();
			}
		});

		Path arrowUp = new Path();
		arrowUp.getStyleClass().add(CssVariables.ARROW);
		double ARROW_SIZE = 4;
		arrowUp.getElements().addAll(new MoveTo(-ARROW_SIZE, 0), new LineTo(ARROW_SIZE, 0),
				new LineTo(0, -ARROW_SIZE), new LineTo(-ARROW_SIZE, 0));
		// mouse clicks should be forwarded to the underlying button
		arrowUp.setMouseTransparent(true);

		Path arrowDown = new Path();
		arrowDown.getStyleClass().add(CssVariables.ARROW);
		arrowDown.getElements().addAll(new MoveTo(-ARROW_SIZE, 0), new LineTo(ARROW_SIZE, 0),
				new LineTo(0, ARROW_SIZE), new LineTo(-ARROW_SIZE, 0));
		arrowDown.setMouseTransparent(true);

		// the spinner buttons scale with the textfield size
		// the following approach leads to the desired result, but it is
		// not fully understood why and obviously it is not quite elegant
		NumberBinding buttonHeight = numberField.heightProperty().subtract(3).divide(2);
		// give unused space in the buttons VBox to the incrementBUtton
		NumberBinding spacing = numberField.heightProperty().subtract(2).subtract(buttonHeight.multiply(2));

		// inc/dec buttons
		VBox buttons = new VBox();
		String BUTTONS_BOX = "ButtonsBox";
		buttons.setId(BUTTONS_BOX);
		Button incrementButton = new Button();
		incrementButton.getStyleClass().add(CssVariables.SPINNER_BUTTON_UP);
		incrementButton.prefWidthProperty().bind(numberField.heightProperty());
		incrementButton.minWidthProperty().bind(numberField.heightProperty());
		incrementButton.maxHeightProperty().bind(buttonHeight.add(spacing));
		incrementButton.prefHeightProperty().bind(buttonHeight.add(spacing));
		incrementButton.minHeightProperty().bind(buttonHeight.add(spacing));
		incrementButton.setFocusTraversable(false);
		incrementButton.setOnAction(ae -> {
			this.numberField.requestFocus();
			increment();
			ae.consume();
		});

		// Paint arrow path on button using a StackPane
		StackPane incPane = new StackPane();
		incPane.getChildren().addAll(incrementButton, arrowUp);
		incPane.setAlignment(Pos.CENTER);

		Button decrementButton = new Button();
		decrementButton.getStyleClass().add(CssVariables.SPINNER_BUTTON_DOWN);
		decrementButton.prefWidthProperty().bind(numberField.heightProperty());
		decrementButton.minWidthProperty().bind(numberField.heightProperty());
		decrementButton.maxHeightProperty().bind(buttonHeight);
		decrementButton.prefHeightProperty().bind(buttonHeight);
		decrementButton.minHeightProperty().bind(buttonHeight);

		decrementButton.setFocusTraversable(false);
		decrementButton.setOnAction(ae -> {
			this.numberField.requestFocus();
			decrement();
			ae.consume();
		});

		StackPane decPane = new StackPane();
		decPane.getChildren().addAll(decrementButton, arrowDown);
		decPane.setAlignment(Pos.CENTER);

		buttons.getChildren().addAll(incPane, decPane);
		this.getChildren().addAll(numberField, buttons);


	}

	public NumberTextField getNumberField()
	{
		return numberField;
	}

	//============================================================
	// private methods
	//============================================================
	/**
	 * increment number value by stepWidth
	 */
	private void increment() {
		int value = numberField.getValue();
		value+=step;
		numberField.setValue(value);
	}

	/**
	 * decrement number value by stepWidth
	 */
	private void decrement() {
		int value = numberField.getValue();
		value-=step;
		numberField.setValue(value);
	}
}
