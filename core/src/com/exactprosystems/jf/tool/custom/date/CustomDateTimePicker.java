/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
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
	private final SimpleDateFormat FORMATTER = new SimpleDateFormat(Common.DATE_TIME_PATTERN);

	private       Date       initialDate;
	private final TextField  editor;
	private final ChangeDate listener;

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
		this.listener = listener;
		this.editor = new TextField(FORMATTER.format(initial));
		this.initialDate = initial;
		super.setAlignment(Pos.CENTER);
		Label label = new Label("", new ImageView(new Image(CssVariables.Icons.DATE_ICON)));
		super.getChildren().addAll(this.editor, label);
		super.setSpacing(4.0);
		this.editor.setEditable(false);
		this.editor.setOnKeyPressed(event -> this.show());
		label.setOnMouseClicked(event -> this.show());
	}

	public Date getDate()
	{
		return this.initialDate;
	}

	private Date show()
	{
		Date date = DialogsHelper.showDateTimePicker(this.initialDate);
		this.editor.setText(FORMATTER.format(date));
		Optional.ofNullable(listener).ifPresent(lis -> lis.change(date));
		this.initialDate = date;
		return this.initialDate;
	}
}
