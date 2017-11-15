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
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class CustomDateTimePicker extends HBox
{
	private TextField editor;
	private Date initialDate;
	private SimpleDateFormat formatter = new SimpleDateFormat(Common.DATE_TIME_PATTERN);
	private ChangeDate listener;

	public interface ChangeDate
	{
		void change(Date date);
	}

	public CustomDateTimePicker(ChangeDate listener)
	{
		this(new Date(), listener);
	}

	public CustomDateTimePicker(Date initial, ChangeDate listener)
	{
		super();
		this.setAlignment(Pos.CENTER);
		this.listener = listener;
		this.editor = new TextField(formatter.format(initial));
		this.initialDate = initial;
		Label label = new Label("", new ImageView(new Image(CssVariables.Icons.DATE_ICON)));
		this.getChildren().addAll(this.editor, label);
		this.setSpacing(4.0);
		this.editor.setEditable(false);
		this.editor.setOnKeyPressed(event -> show());
		label.setOnMouseClicked(event -> show());
	}

	private Date show()
	{
		Date date = DialogsHelper.showDateTimePicker(this.initialDate);
		this.editor.setText(formatter.format(date));
		Optional.ofNullable(listener).ifPresent(lis -> lis.change(date));
		this.initialDate = date;
		return this.initialDate;
	}

	public Date getDate()
	{
		return this.initialDate;
	}

}
