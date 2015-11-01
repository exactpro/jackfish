package com.exactprosystems.jf.tool.custom.date;

import com.sun.javafx.scene.control.skin.DatePickerContent;
import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

public class DateTimePickerSkin extends DatePickerSkin {

	private DateTimePicker datePicker;
	private DatePickerContent ret;

	public DateTimePickerSkin(DateTimePicker datePicker){
		super(datePicker);
		this.datePicker = datePicker;
	}

	@Override
	public Node getPopupContent() {
		if (ret == null)
		{
			ret = (DatePickerContent) super.getPopupContent();

			Slider hours = new Slider(0, 23, (datePicker.getDateTime() != null ? datePicker.getDateTime().getHour() : 0));
			Label hoursValue = new Label("Hours: " + (datePicker.getDateTime() != null ? datePicker.getDateTime().getHour() : "") + " ");

			Slider minutes = new Slider(0, 59, (datePicker.getDateTime() != null ? datePicker.getDateTime().getMinute() : 0));
			Label minutesValue = new Label("Minutes: " + (datePicker.getDateTime() != null ? datePicker.getDateTime().getMinute() : "") + " ");

			Slider seconds = new Slider(0, 59, (datePicker.getDateTime() != null ? datePicker.getDateTime().getSecond() : 0));
			Label secondsValue = new Label("Seconds: " + (datePicker.getDateTime() != null ? datePicker.getDateTime().getSecond() : "") + " ");

			ret.getChildren().addAll(new HBox(hoursValue, hours), new HBox(minutesValue, minutes), new HBox(secondsValue, seconds));

			hours.valueProperty().addListener((observable, oldValue, newValue) -> {
				datePicker.setDateTime(datePicker.getDateTime().withHour(newValue.intValue()));
				hoursValue.setText("Hours: " + String.format("%02d", datePicker.getDateTime().getHour()) + " ");
			});

			minutes.valueProperty().addListener((observable, oldValue, newValue) -> {
				datePicker.setDateTime(datePicker.getDateTime().withMinute(newValue.intValue()));
				minutesValue.setText("Minutes: " + String.format("%02d", datePicker.getDateTime().getMinute()) + " ");
			});

			seconds.valueProperty().addListener((observable, oldValue, newValue) -> {
				datePicker.setDateTime(datePicker.getDateTime().withSecond(newValue.intValue()));
				secondsValue.setText("Seconds: " + String.format("%02d", datePicker.getDateTime().getSecond()) + " ");
			});

		}
		return ret;
	}
}