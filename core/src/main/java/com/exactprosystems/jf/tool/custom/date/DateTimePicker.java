/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
