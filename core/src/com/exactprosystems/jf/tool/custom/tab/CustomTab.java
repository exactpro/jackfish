////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.tab;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.FileReader;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.exactprosystems.jf.tool.custom.tab.CustomTabPane.TAB_DRAG_KEY;

public class CustomTab extends Tab implements AutoCloseable
{
	private static final String	CHANGED_MARKER	= " *";
	private Hyperlink			crossButton;
	private Text				text;
	private Document			document;
	private FileWatcher			watcher;
	private Settings			settings;
	private final AtomicBoolean	warningIsShow;

	private CustomTabPane tabPane;
	private HBox view;

	public CustomTab(Document document, Settings settings, CustomTabPane tabPane)
	{
		super();
		this.tabPane = tabPane;
		this.warningIsShow = new AtomicBoolean(false);
		this.settings = settings;
		this.document = document;

		init();
	}

	protected void init()
	{
		this.setClosable(false);

		this.view = new HBox();
		this.view.setAlignment(Pos.CENTER);

		this.crossButton = new Hyperlink();
		Image image = new Image(CssVariables.Icons.CLOSE_BUTTON_ICON);
		this.crossButton.setGraphic(new ImageView(image));
		this.crossButton.setFocusTraversable(false);

		this.text = new Text();
		this.text.getStyleClass().add(CssVariables.TAB_LABEL);
		this.text.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.MIDDLE)
			{
				Common.tryCatch(this::onClose, "");
				Common.tryCatch(this::close, "");
			}
		});
		this.view.getChildren().addAll(this.text, this.crossButton);
		this.view.setOnDragDetected(e -> {
			tabPane.draggingTabProperty().set(this);

			Dragboard dragboard = this.view.startDragAndDrop(TransferMode.MOVE);
			ClipboardContent clipboardContent = new ClipboardContent();
			clipboardContent.putString(TAB_DRAG_KEY);
			dragboard.setContent(clipboardContent);
			WritableImage snapshot = this.view.snapshot(new SnapshotParameters(), null);
			dragboard.setDragView(snapshot);

			e.consume();
		});
		this.view.setOnDragOver(e -> {
			tabPane.droppedTabProperty().set(null);
		});
		this.setGraphic(this.view);

		this.watcher = new FileWatcher()
		{
			@Override
			public void onChanged()
			{
				if (Common.appIsFocused() && isSelected())
				{
					synchronized (warningIsShow)
					{
						if(!warningIsShow.get())
						{
							Common.tryCatch(CustomTab.this::reload, "Error on reload");
							warningIsShow.set(true);
						}
					}
				}
			}
		};
		this.watcher.saved(this.document.getName());
		this.crossButton.setDisable(true);
		this.crossButton.setVisible(false);
		this.setOnSelectionChanged(arg0 ->
		{
			crossButton.setDisable(!isSelected());
			crossButton.setVisible(isSelected());
			if (isSelected() && watcher.isChanged())
			{
				Common.tryCatch(CustomTab.this::reload, "Error on reload");
			}
		});
		this.document.setOnChange(flag ->
		{
			String text = this.text.getText();
			if (flag)
			{
				if (!text.endsWith(CHANGED_MARKER))
				{
					this.text.setText(text + CHANGED_MARKER);
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
		this.crossButton.setOnAction(actionEvent ->
		{
			Common.tryCatch(this::onClose, "");
			Common.tryCatch(this::close, "");
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

	public void reload() throws Exception
	{
		Platform.runLater(() ->
		{
			ButtonType desision = DialogsHelper.showFileChangedDialog(this.document.getName());
			if (desision == ButtonType.OK)
			{
				Common.tryCatch(() ->
				{
					try (Reader reader = new FileReader(this.document.getName()))
					{
						this.document.load(reader);
					}
					this.document.display();
					this.document.saved();
				}, "Error on reload");
			}
			saved(this.document.getName());
			synchronized (warningIsShow)
			{
				warningIsShow.set(false);
			}
		});
	}

	public void onClose() throws Exception
	{
		if (this.document.canClose())
		{
			this.document.close(settings);
		}
	}

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
	}

	HBox getView()
	{
		return view;
	}

	public static class TempCustomTab extends CustomTab
	{
		public TempCustomTab(Document document, Settings settings,CustomTabPane pane)
		{
			super(document, settings, pane);
			this.getStyleClass().addAll(CssVariables.TEMP_CUSTOM_TAB);
		}

		@Override
		protected void init()
		{
			super.view = new HBox();
			super.view.getStyleClass().addAll(CssVariables.TEMP_VIEW_CUSTOM_TAB);
			Label lbl = new Label();
			lbl.setPrefWidth(15);
			lbl.setMinWidth(15);
			lbl.setMaxWidth(15);
			super.view.getChildren().add(lbl);
			super.view.setOnDragOver(e -> {
				super.tabPane.droppedTabProperty().set(this);
				super.view.getStyleClass().removeAll(CssVariables.TEMP_VIEW_OVER_CUSTOM_TAB);
				super.view.getStyleClass().add(CssVariables.TEMP_VIEW_OVER_CUSTOM_TAB);
			});
			super.view.setOnDragExited(e -> {
				super.view.getStyleClass().removeAll(CssVariables.TEMP_VIEW_OVER_CUSTOM_TAB);
			});
			setGraphic(super.view);
		}

		@Override
		public void close() throws Exception
		{

		}

		@Override
		public void saved(String fileName)
		{

		}

		@Override
		public void reload() throws Exception
		{

		}

		@Override
		public void onClose() throws Exception
		{

		}

		@Override
		public String getTitle()
		{
			return super.getTitle();
		}

		@Override
		public Document getDocument()
		{
			return null;
		}

		@Override
		public void setTitle(String text)
		{

		}
	}

}
