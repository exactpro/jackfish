/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.documents.text;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.common.highlighter.StyleWithRange;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.documents.ControllerInfo;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@ControllerInfo(resourceName = "PlainTextFx.fxml")
public class PlainTextFxController extends AbstractDocumentController<PlainTextFx>
{
	public  BorderPane            mainPane;
	public  ToolBar               toolbar;
	public  ToggleButton          tbFindAndReplace;
	public  ComboBox<Highlighter> cbHighlighting;
	public  CheckBox              cbShowLineNumbers;
	public  GridPane              findPane;
	public  CheckBox              cbRegexp;
	public  CheckBox              cbMatchCase;
	public  TextField             tfFind;
	public  TextField             tfReplace;
	public  Label                 lblFindCount;
	private StyleClassedTextArea  textArea;
	public Button btnReplace;

	private Subscription lastSubscription;

	private BooleanProperty canReplace = new SimpleBooleanProperty(true);

	private Supplier<StyleSpans<Collection<String>>> lastFindSupplier = () -> null;
	private int                                      lastDifference   = 0;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		super.initialize(url, resourceBundle);

		this.textArea = new StyleClassedTextArea();
		this.textArea.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.F && event.isControlDown())
			{
				this.tbFindAndReplace.selectedProperty().setValue(!this.tbFindAndReplace.isSelected());
			}
		});
		this.textArea.getUndoManager().forgetHistory();
		this.textArea.getUndoManager().mark();

		// position the caret at the beginning
		this.textArea.selectRange(0, 0);
		this.cbHighlighting.getItems().addAll(Highlighter.values());
		this.cbHighlighting.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> subscribeAndSet());
		this.cbHighlighting.getSelectionModel().selectFirst();
		this.cbShowLineNumbers.selectedProperty().addListener((observable, oldValue, newValue) -> this.textArea.setParagraphGraphicFactory(newValue ? LineNumberFactory.get(this.textArea) : null));

		this.tbFindAndReplace.selectedProperty().addListener((observable, oldValue, newValue) -> {
			this.findPane.setVisible(newValue);
			this.findPane.setPrefHeight(newValue ? 60 : 0);
			this.findPane.setMinHeight(newValue ? 60 : 0);
			this.findPane.setMaxHeight(newValue ? 60 : 0);
			subscribeAndSet();
			Common.setFocusedFast(this.tfFind);

			if (!newValue)
			{
				this.lastFindSupplier = () -> null;
			}
		});

		this.mainPane.setCenter(this.textArea);
		GridPane.setColumnSpan(this.textArea, 2);

		this.cbRegexp.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.resetMatcher(this.tfFind.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected()));
		this.cbMatchCase.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.resetMatcher(this.tfFind.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected()));
		this.tfFind.textProperty().addListener((observable, oldValue, newValue) -> this.model.resetMatcher(this.tfFind.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected()));

		this.btnReplace.disableProperty().bind(canReplace);
	}

	public void init(Document model, CustomTab customTab)
	{
		super.init(model, customTab);

		this.model.getFindCountProperty().setOnChangeListener((oldValue, newValue) -> {
			if (newValue == null || newValue < 0) {
				this.lblFindCount.setText("");
			} else {
				this.lblFindCount.setText("Found " + newValue);
			}
		});
		this.textArea.replaceText(this.model.getProperty().get());

		this.textArea.textProperty().addListener((observable, oldValue, newValue) -> {
			if (Objects.equals(newValue, oldValue)) {
				return;
			}
			this.model.getProperty().accept(newValue);
		});
		this.model.getProperty().setOnChangeListener((oldValue, newValue) -> {
			this.model.getChangedProperty().accept(true);
			this.textArea.replaceText(newValue);
		});
		this.model.getHighlighter().setOnChangeListener((o, n) -> this.cbHighlighting.getSelectionModel().select(n));

		Settings.SettingsValue value = model.getFactory().getSettings().getValueOrDefault(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.FONT);
		Font font = Common.fontFromString(value.getValue());
		this.textArea.setStyle("-fx-font-size: " + font.getSize() + "; -fx-font-family: \"" + font.getFamily() + "\";");
	}

	public void findAll(ActionEvent actionEvent)
	{
		this.lastFindSupplier = () -> {
			List<StyleWithRange> styles = this.model.findAll();
			return Common.convertFromList(styles);
		};
		subscribeAndSet();
	}

	public void replaceAll(ActionEvent actionEvent)
	{
		if (!this.tfFind.getText().isEmpty())
		{
			this.model.replaceAll(this.tfReplace.getText());
		}
	}

	public void findNext(ActionEvent actionEvent)
	{
		List<StyleWithRange> styles = this.model.findNext();
		this.lastFindSupplier = () -> Common.convertFromList(this.model.getCurrentStyles(lastDifference));
		if (styles.size() != 1)
		{
			styles.stream()
					.filter(s -> s.getStyle() == null)
					.findFirst()
					.ifPresent(swr -> {
						this.textArea.moveTo(swr.getRange());
						this.textArea.selectWord();
					});
			this.canReplace.setValue(false);
		}
		this.subscribeAndSet();
	}

	public void replaceCurrent(ActionEvent actionEvent)
	{
		this.model.replaceCurrent(this.tfReplace.getText());
		this.canReplace.setValue(true);
	}

	public void replaceAndFind(ActionEvent actionEvent)
	{
		this.replaceCurrent(null);
		this.findNext(null);
	}

	//region private methods
	private void subscribeAndSet()
	{
		Highlighter highlighter = this.cbHighlighting.getSelectionModel().getSelectedItem();

		Optional.ofNullable(this.lastSubscription).ifPresent(Subscription::unsubscribe);
		this.textArea.setStyleSpans(0, union(convert(highlighter), this.lastFindSupplier.get()));

		this.lastSubscription = this.textArea.richChanges()
				.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
				.subscribe(change -> {
					this.lastDifference = change.getInserted().length() - change.getRemoved().length();
					this.textArea.setStyleSpans(0, union(convert(highlighter), this.lastFindSupplier.get()));
				});
	}

	private StyleSpans<Collection<String>> convert(Highlighter highlighter)
	{
		return Common.convertFromList(highlighter.getStyles(this.textArea.getText()));
	}

	private static StyleSpans<Collection<String>> union(StyleSpans<Collection<String>> initList, StyleSpans<Collection<String>> findList)
	{
		if (findList == null)
		{
			return initList;
		}
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		for (int i = 0; i < initList.length(); i++)
		{
			StyleSpan<Collection<String>> find = get(i, findList);
			StyleSpan<Collection<String>> init = get(i, initList);
			boolean b = find == null || (find.getStyle().size() == 1 && find.getStyle().iterator().next().equals("default"));
			Collection<String> strings = b ? init.getStyle() : find.getStyle();
			StyleSpan<Collection<String>> newSpan = new StyleSpan<>(strings, 1);
			spansBuilder.add(newSpan);
		}

		return spansBuilder.create();
	}

	private static StyleSpan<Collection<String>> get(int position, StyleSpans<Collection<String>> list)
	{
		int curPos = 0;
		for (int i = 0; i < list.getSpanCount(); i++)
		{
			StyleSpan<Collection<String>> styleSpan = list.getStyleSpan(i);
			if (curPos <= position && (curPos + styleSpan.getLength() - 1) >= position)
			{
				return styleSpan;
			}
			curPos += styleSpan.getLength();
		}
		//never happens
		return new StyleSpan<>(Collections.singleton("default"), 1);
	}

	//endregion
}
