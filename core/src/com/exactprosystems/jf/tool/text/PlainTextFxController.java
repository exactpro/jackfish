////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.text;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.common.highlighter.StyleWithRange;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.StyleSpans;
import org.reactfx.Subscription;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PlainTextFxController implements Initializable, ContainingParent
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

	private Parent       pane;
	private PlainTextFx  model;
	private CustomTab    tab;
	private Subscription lastSubscription;

	private boolean findPanelIsOpened = false;

	//region Interface Initializible
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
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
		this.cbHighlighting.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (!this.findPanelIsOpened)
			{
				subscribeAndSet(newValue);
			}
		});
		this.cbHighlighting.getSelectionModel().selectFirst();
		this.cbShowLineNumbers.selectedProperty().addListener((observable, oldValue, newValue) -> this.textArea.setParagraphGraphicFactory(newValue ? LineNumberFactory.get(this.textArea) : null));

		this.tbFindAndReplace.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			this.findPanelIsOpened = newValue;

			this.findPane.setVisible(newValue);
			this.findPane.setPrefHeight(newValue ? 60 : 0);
			this.findPane.setMinHeight(newValue ? 60 : 0);
			this.findPane.setMaxHeight(newValue ? 60 : 0);

			if (!newValue)
			{
				subscribeAndSet(this.cbHighlighting.getSelectionModel().getSelectedItem());
			}
			else
			{
				Optional.ofNullable(this.lastSubscription).ifPresent(Subscription::unsubscribe);
				this.textArea.clearStyle(0, this.textArea.getText().length());
			}

			Common.setFocused(this.tfFind);
		});

		this.mainPane.setCenter(this.textArea);
		GridPane.setColumnSpan(this.textArea, 2);

		this.cbRegexp.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.resetMatcher(this.tfFind.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected()));
		this.cbMatchCase.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.resetMatcher(this.tfFind.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected()));
		this.tfFind.textProperty().addListener((observable, oldValue, newValue) -> this.model.resetMatcher(this.tfFind.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected()));
	}

	//endregion

	//region Interface ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}
	//endregion

	public void init(PlainTextFx model, Settings settings, Highlighter initHighlighter)
	{
		this.model = model;
		SettingsValue value = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.FONT, "Monospaced$16");
		this.textArea.setFont(Common.fontFromString(value.getValue()));
		this.tab = CustomTabPane.getInstance().createTab(model);
		this.tab.setContent(this.pane);
		this.cbHighlighting.getSelectionModel().select(initHighlighter);
		CustomTabPane.getInstance().addTab(this.tab);
		CustomTabPane.getInstance().selectTab(this.tab);
	}

	public void saved(String name)
	{
		this.tab.saved(name);
	}

	public void close() throws Exception
	{
		this.tab.close();
		CustomTabPane.getInstance().removeTab(this.tab);
	}

	public void displayTitle(String title)
	{
		this.tab.setTitle(title);
	}

	public void displayText(String text, Consumer<String> consumer)
	{
		this.textArea.clear();
		this.textArea.appendText(text);
		this.textArea.textProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
	}

	public void findAll(ActionEvent actionEvent)
	{
		if (this.tfFind.getText().isEmpty())
		{
			this.lblFindCount.setText("");
			Optional.ofNullable(this.lastSubscription).ifPresent(Subscription::unsubscribe);
			this.textArea.clearStyle(0, this.textArea.getText().length());
		}
		else
		{
			AtomicInteger atomicInteger = new AtomicInteger(0);
			Optional.ofNullable(this.lastSubscription).ifPresent(Subscription::unsubscribe);
			List<StyleWithRange> styles = this.model.findAll(this.tfFind.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected(), atomicInteger);
			StyleSpans<Collection<String>> styleSpans = Common.convertFromList(styles);
			this.textArea.setStyleSpans(0, styleSpans);
			this.lblFindCount.setText("Found " + atomicInteger.get());
		}
	}

	public void replaceAll(ActionEvent actionEvent)
	{
		if (!this.tfFind.getText().isEmpty())
		{
			this.model.replaceAll(this.tfFind.getText(), this.tfReplace.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected());
		}
	}

	public void findNext(ActionEvent actionEvent)
	{
		Optional.ofNullable(this.lastSubscription).ifPresent(Subscription::unsubscribe);
		List<StyleWithRange> styles = this.model.findNext(this.tfFind.getText(), this.cbMatchCase.isSelected(), this.cbRegexp.isSelected());
		StyleSpans<Collection<String>> styleSpans = Common.convertFromList(styles);
		this.textArea.setStyleSpans(0, styleSpans);
		if (styles.size() != 1)
		{
			styles.stream()
					.filter(s -> s.getStyle() == null)
					.findFirst()
					.ifPresent(s -> this.textArea.moveTo(s.getRange()));
		}
	}

	public void replaceCurrent(ActionEvent actionEvent)
	{
		this.model.replaceCurrent(this.tfReplace.getText());
	}

	public void replaceAndFind(ActionEvent actionEvent)
	{
		this.replaceCurrent(null);
		this.findNext(null);
	}

	private void subscribeAndSet(Highlighter highlighter)
	{
		Optional.ofNullable(this.lastSubscription).ifPresent(Subscription::unsubscribe);
		this.textArea.setStyleSpans(0, Common.convertFromList(highlighter.getStyles(this.textArea.getText())));
		this.lastSubscription = this.textArea.richChanges()
				.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
				.subscribe(change -> this.textArea.setStyleSpans(0, Common.convertFromList(highlighter.getStyles(this.textArea.getText()))));
	}
}
