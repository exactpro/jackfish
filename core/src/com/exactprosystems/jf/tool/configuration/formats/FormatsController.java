////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration.formats;

import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class FormatsController implements Initializable, ContainingParent
{
	public TextField tfFormatsTime;
	public TextField tfFormatsDate;
	public TextField tfFormatDateTime;
	public TextArea taFormatAdditionFormat;

	private ConfigurationFx model;
	private Parent pane;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfFormatDateTime != null : "fx:id=\"tfFormatDateTime\" was not injected: check your FXML file 'Formats.fxml'.";
		assert tfFormatsDate != null : "fx:id=\"tfFormatsDate\" was not injected: check your FXML file 'Formats.fxml'.";
		assert tfFormatsTime != null : "fx:id=\"tfFormatsTime\" was not injected: check your FXML file 'Formats.fxml'.";
		assert taFormatAdditionFormat != null : "fx:id=\"taFormatAdditionFormat\" was not injected: check your FXML file 'Formats.fxml'.";

		Arrays.asList(tfFormatDateTime, tfFormatsDate, tfFormatsTime, taFormatAdditionFormat)
				.forEach(tf -> tf.focusedProperty().addListener((observable, oldValue, newValue) -> tryCatch(() -> {
					if (!newValue && oldValue)
					{
						model.changeFormats(tfFormatDateTime.getText(), tfFormatsDate.getText(), tfFormatsTime.getText(), taFormatAdditionFormat.getText().replace('\n', '|'));
					}
				}, "Error on changing date and time formats")));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	public void init(ConfigurationFx model, TitledPane titledPaneFormats)
	{
		this.model = model;
		titledPaneFormats.setContent(this.pane);
	}

	public void display(String dateTime, String date, String time, String additionalFormat)
	{
		tfFormatDateTime.setText(dateTime);
		tfFormatsDate.setText(date);
		tfFormatsTime.setText(time);
		taFormatAdditionFormat.setText(additionalFormat.replace('|', '\n'));
	}
}
