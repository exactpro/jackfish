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

package com.exactprosystems.jf.tool.custom.tab;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
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

import java.io.Reader;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.exactprosystems.jf.tool.custom.tab.CustomTabPane.TAB_DRAG_KEY;

public class CustomTab extends Tab
{
	public static final String	CHANGED_MARKER	= " *";
	private Hyperlink			crossButton;
	private Text				text;

	private Document			document;
	private AbstractDocumentController<? extends Document> abstractController;

	private FileWatcher			watcher;
	private final AtomicBoolean	warningIsShow;

	private CustomTabPane tabPane;
	private HBox view;

	public CustomTab(Document document, CustomTabPane tabPane)
	{
		super();
		this.tabPane = tabPane;
		this.warningIsShow = new AtomicBoolean(false);
		this.document = document;
		init();
	}

	public void setController(AbstractDocumentController<? extends Document> abstractController)
	{
		this.abstractController = abstractController;
	}

	public AbstractDocumentController<? extends Document> getController()
	{
		return abstractController;
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
			this.tabPane.draggingTabProperty().set(this);

			Dragboard dragboard = this.view.startDragAndDrop(TransferMode.MOVE);
			ClipboardContent clipboardContent = new ClipboardContent();
			clipboardContent.putString(TAB_DRAG_KEY);
			dragboard.setContent(clipboardContent);
			WritableImage snapshot = this.view.snapshot(new SnapshotParameters(), null);
			dragboard.setDragView(snapshot);

			e.consume();
		});
		this.view.setOnDragOver(e -> this.tabPane.droppedTabProperty().set(null));
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
							Common.tryCatch(CustomTab.this::reload, R.CUSTOM_TAB_ERROR_ON_RELOAD.get());
							warningIsShow.set(true);
						}
					}
				}
			}
		};
		this.watcher.saved(this.document.getNameProperty().get());
		this.crossButton.setDisable(true);
		this.crossButton.setVisible(false);
		this.setOnSelectionChanged(arg0 ->
		{
			crossButton.setDisable(!isSelected());
			crossButton.setVisible(isSelected());
			if (isSelected() && watcher.isChanged())
			{
				Common.tryCatch(CustomTab.this::reload, R.CUSTOM_TAB_ERROR_ON_RELOAD.get());
			}
		});
		this.document.getChangedProperty().setOnChangeListener((oldFlag, flag) ->
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

	public void close()
	{
		this.watcher.close();
	}

	public void saved(String fileName)
	{
		this.watcher.saved(fileName);
	}

	public void reload() throws Exception
	{
		Common.runLater(() ->
		{
			ButtonType desision = DialogsHelper.showFileChangedDialog(this.document.getNameProperty().get());
			if (desision == ButtonType.OK)
			{
				Common.tryCatch(() ->
				{
					try (Reader reader = CommonHelper.readerFromFileName(this.document.getNameProperty().get()))
					{
						this.document.load(reader);
					}
					if (DocumentKind.byDocument(this.document).isUseNewMVP())
					{
						this.document.getFactory().showDocument(this.document);
					}
					else
					{
						this.document.display();
					}
					this.document.saved();
				}, R.CUSTOM_TAB_ERROR_ON_RELOAD.get());
			}
			saved(this.document.getNameProperty().get());
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
			this.document.close();
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

	public static class TempCustomTab extends CustomTab
	{
		public TempCustomTab(Document document, CustomTabPane pane)
		{
			super(document, pane);
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
			super.view.setOnDragExited(e -> super.view.getStyleClass().removeAll(CssVariables.TEMP_VIEW_OVER_CUSTOM_TAB));
			setGraphic(super.view);
		}

		@Override
		public void close()
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
