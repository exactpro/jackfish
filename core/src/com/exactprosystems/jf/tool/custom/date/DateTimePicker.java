////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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

public class DateTimePicker extends DatePicker
{
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_PATTERN);
	private final ObjectProperty<LocalDateTime> dateTimeValue;

	public DateTimePicker(Date initialDate)
	{
		super();
		this.dateTimeValue = new SimpleObjectProperty<>();
		super.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
		{
			try
			{
				this.setDateTime(LocalDateTime.parse(newValue, formatter));
			}
			catch (Exception ignored)
			{}
		});
		if (initialDate != null)
		{
			dateTimeValueProperty().setValue(Common.convert(initialDate));
			super.setValue(Common.convert(initialDate).toLocalDate());
		}
		else
		{
			dateTimeValueProperty().setValue(LocalDateTime.now());
			super.setValue(LocalDate.now());
		}
		super.setConverter(new StringConverter<LocalDate>()
		{
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
		super.valueProperty().addListener((observable, oldValue, newValue) ->
				this.dateTimeValue.set(this.dateTimeValue.getValue().withYear(newValue.getYear()).withMonth(newValue.getMonthValue()).withDayOfMonth(newValue.getDayOfMonth())));
	}

	public Date getDate()
	{
		return Common.convert(dateTimeValue.get());
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
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

	public ObjectProperty<LocalDateTime> dateTimeValueProperty()
	{
		return this.dateTimeValue;
	}
}
