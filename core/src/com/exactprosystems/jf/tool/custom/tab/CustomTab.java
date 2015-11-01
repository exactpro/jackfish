////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.tab;

import com.exactprosystems.jf.common.Document;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

public abstract class CustomTab extends Tab implements AutoCloseable
{
	private static final Logger logger = Logger.getLogger(CustomTab.class);
	private static final String CHANGED_MARKER = " *";
	private Hyperlink crossButton;
	private Text text;
	private Document document;
	private FileWatcher watcher; 

	public CustomTab(Document document) 
	{
		super();
		
		this.setClosable(false);
		this.document = document;

		HBox box = new HBox();
		box.setAlignment(Pos.CENTER);

		this.crossButton = new Hyperlink();
		Image image = new Image(CssVariables.Icons.CLOSE_BUTTON_ICON);
		this.crossButton.setGraphic(new ImageView(image));
		this.crossButton.setFocusTraversable(false);

		this.text = new Text();
		this.text.getStyleClass().add(CssVariables.TAB_LABEL);
		box.getChildren().addAll(this.text, this.crossButton);
		this.setGraphic(box);

		
		this.watcher = new FileWatcher()
		{
			@Override
			public void onChanged()
			{
				if (Common.appIsFocused() && isSelected())
				{
					Common.tryCatch(CustomTab.this::reload, "Error on reload");
				}
			}
		};
		this.watcher.saved(this.document.getName());

		this.setOnSelectionChanged(arg0 -> {
			crossButton.setDisable(!isSelected());
			crossButton.setVisible(isSelected());
			if (isSelected() && watcher.isChanged())
			{
				Common.tryCatch(CustomTab.this::reload, "Error on reload");
			}
		});
		this.document.setOnChange(flag -> {
			String text = this.text.getText();
			if (flag)
			{
				if (!text.endsWith(CHANGED_MARKER))
				{
					this.text.setText(text+CHANGED_MARKER);
				}
			}
			else
			{
				if (text.endsWith(CHANGED_MARKER))
				{
					this.text.setText(text.substring(0, text.length() - CHANGED_MARKER.length()));
				}
			}
		});
		this.crossButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent actionEvent)
			{
				Common.tryCatch(() -> onClose(), "");
				Common.tryCatch(() -> close(), "");
			}
		});
	}

	@Override
	public void close() throws Exception
	{
		this.watcher.close();
	}
	
	public void saved(String fileName)
	{
		this.watcher.saved(fileName);
	}
	
	public abstract void reload() throws Exception;
	
	public abstract void onClose() throws Exception;

	public String getTitle()
	{
		return this.text.getText();
	}

	public Document getDocument()
	{
		return document;
	}

	public void setTitle(String text)
	{
		this.text.setText(Common.getSimpleTitle(text));
		if (this.getTabPane() != null)
		{
			this.getTabPane().getSelectionModel().clearSelection();
			this.getTabPane().getSelectionModel().select(this);
		}
	}
}
