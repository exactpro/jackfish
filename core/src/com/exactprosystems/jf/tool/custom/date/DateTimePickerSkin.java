////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.date;

import com.exactprosystems.jf.tool.custom.number.NumberSpinner;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.sun.javafx.scene.control.skin.DatePickerContent;
import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.time.LocalDateTime;

public class DateTimePickerSkin extends DatePickerSkin
{
	private final DateTimePicker datePicker;
	private DatePickerContent    content;

	public DateTimePickerSkin(DateTimePicker datePicker)
	{
		super(datePicker);
		this.datePicker = datePicker;
	}

	@Override
	public Node getPopupContent()
	{
		if (this.content == null)
		{
			this.content = (DatePickerContent) super.getPopupContent();
			LocalDateTime dateTime = datePicker.getDateTime();

			Slider hours = new Slider(0, 23, (dateTime.getHour()));
			Label hoursValue = new Label("Hours: ");
			NumberSpinner hoursSpinner = new NumberSpinner(new NumberTextField(dateTime.getHour(), 0, 23));
			hoursSpinner.setPrefWidth(55);
			hoursSpinner.setMaxWidth(55);

			Slider minutes = new Slider(0, 59, dateTime.getMinute());
			Label minutesValue = new Label("Minutes: ");
			NumberTextField numberField = new NumberTextField(dateTime.getMinute(), 0, 59);
			NumberSpinner minutesSpinner = new NumberSpinner(numberField);
			minutesSpinner.setPrefWidth(55);
			minutesSpinner.setMaxWidth(55);

			Slider seconds = new Slider(0, 59, dateTime.getSecond());
			Label secondsValue = new Label("Seconds: ");
			NumberSpinner secondsTextField = new NumberSpinner(new NumberTextField(dateTime.getSecond(), 0, 59));
			secondsTextField.setPrefWidth(55);
			secondsTextField.setMaxWidth(55);

			GridPane grid = new GridPane();
			ColumnConstraints hoursColumn = new ColumnConstraints();
			hoursColumn.setHalignment(HPos.LEFT);
			ColumnConstraints minutesColumn = new ColumnConstraints();
			minutesColumn.setHalignment(HPos.CENTER);
			ColumnConstraints secondsColumn = new ColumnConstraints();
			secondsColumn.setHalignment(HPos.RIGHT);

			grid.add(hoursValue, 0, 0);
			grid.add(hoursSpinner, 1, 0);
			grid.add(hours, 2, 0);
			grid.add(minutesValue, 0, 1);
			grid.add(minutesSpinner, 1, 1);
			grid.add(minutes, 2, 1);
			grid.add(secondsValue, 0, 2);
			grid.add(secondsTextField, 1, 2);
			grid.add(seconds, 2, 2);

			this.content.getChildren().add(grid);

			hours.valueProperty().addListener((observable, oldValue, newValue) ->
			{
				this.datePicker.setDateTime(this.datePicker.getDateTime().withHour(newValue.intValue()));
				hoursSpinner.getNumberField().setText(String.valueOf(newValue.intValue()));
			});
			minutes.valueProperty().addListener((observable, oldValue, newValue) ->
			{
				this.datePicker.setDateTime(datePicker.getDateTime().withMinute(newValue.intValue()));
				minutesSpinner.getNumberField().setText(String.valueOf(newValue.intValue()));
			});
			seconds.valueProperty().addListener((observable, oldValue, newValue) ->
			{
				this.datePicker.setDateTime(datePicker.getDateTime().withSecond(newValue.intValue()));
				secondsTextField.getNumberField().setText(String.valueOf(newValue.intValue()));
			});

			hoursSpinner.getNumberField().textProperty().addListener((observable, oldValue, newValue) -> hours.setValue(hoursSpinner.getValue()));
			minutesSpinner.getNumberField().textProperty().addListener((observable, oldValue, newValue) -> minutes.setValue(minutesSpinner.getValue()));
			secondsTextField.getNumberField().textProperty().addListener((observable, oldValue, newValue) -> seconds.setValue(secondsTextField.getValue()));
		}
		return this.content;
	}
}