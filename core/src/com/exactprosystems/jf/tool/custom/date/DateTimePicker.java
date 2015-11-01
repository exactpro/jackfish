package com.exactprosystems.jf.tool.custom.date;

import com.exactprosystems.jf.tool.Common;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimePicker extends DatePicker{

	private ObjectProperty<LocalDateTime> dateTimeValue;

	public DateTimePicker(Date initial)
	{
		super();
		this.dateTimeValue = new SimpleObjectProperty<>();
		setValue(LocalDate.now());
		if (initial != null)
		{
			dateTimeValueProperty().setValue(Common.convert(initial));
		}
		else
		{
			dateTimeValueProperty().setValue(LocalDateTime.now());
		}
		setConverter(new StringConverter<LocalDate>()
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_PATTERN);

			@Override
			public String toString(LocalDate object)
			{
				return dateTimeValue.get().format(formatter);
			}

			@Override
			public LocalDate fromString(String string)
			{
				return LocalDate.parse(string, formatter);
			}
		});
		valueProperty().addListener((observable, oldValue, newValue) -> {
			this.dateTimeValue.set(this.dateTimeValue.getValue().withYear(newValue.getYear()).withMonth(newValue.getMonthValue()).withDayOfMonth(newValue.getDayOfMonth()));
		});
	}

	public Date getDate()
	{
		return Common.convert(dateTimeValue.get());
	}

	@Override
	protected Skin<?> createDefaultSkin () {
		return new DateTimePickerSkin(this);
	}

	LocalDateTime getDateTime()
	{
		return dateTimeValue.get();
	}

	void setDateTime(LocalDateTime value)
	{
		this.dateTimeValue.set(value);
	}

	ObjectProperty<LocalDateTime> dateTimeValueProperty(){
		if (dateTimeValue == null){
			dateTimeValue = new SimpleObjectProperty<>();
		}
		return dateTimeValue;
	}
}
