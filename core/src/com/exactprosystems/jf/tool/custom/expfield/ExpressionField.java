////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.expfield;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.listeners.ListProvider;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Deprecated
public class ExpressionField extends BorderPane
{
	private AbstractEvaluator			evaluator;

	private TextField					editor;
	private Label						firstLabel;
	private Label						secondLabel;
	private Label						shadowLabel;
	private HBox						hBox;

	private ErrorHandler				errorHandler;
	private Function<String, String>	firstAction;
	private Function<String, String>	secondAction;

	private ChangeListener<Boolean> valueListener;
	private ChangeListener<Boolean> focusListener;

	public ExpressionField(AbstractEvaluator evaluator)
	{
		this(evaluator, null);
	}

	public ExpressionField(AbstractEvaluator evaluator, String text)
	{
		this.evaluator = evaluator;

		this.editor = new TextField(text == null ? "" : text);
		stretchIfCan(this.editor.getText());
		this.editor.getStyleClass().add(CssVariables.EXPRESSION_EDITOR);
		ChangeListener<Boolean> globalListener = (observable, oldValue, newValue) -> 
		{
			Optional.ofNullable(valueListener).ifPresent(listener -> listener.changed(observable, oldValue, newValue));
			Optional.ofNullable(focusListener).ifPresent(listener -> listener.changed(observable, oldValue, newValue));
			stretchIfCan(this.editor.getText());
		};
		this.editor.focusedProperty().addListener(globalListener);
		this.firstLabel = new Label("≡");
		this.firstLabel.setAlignment(Pos.CENTER);
		this.firstLabel.getStyleClass().setAll(CssVariables.EXPRESSION_FIELD_FIRST);

		this.secondLabel = new Label("ﬁ");
		this.secondLabel.setAlignment(Pos.CENTER);
		this.secondLabel.getStyleClass().setAll(CssVariables.EXPRESSION_FIELD_SECOND);

		this.firstLabel.prefHeightProperty().bind(this.editor.heightProperty().multiply(1));
		this.secondLabel.prefHeightProperty().bind(this.editor.heightProperty().multiply(1));
		sizeTextField();

		setWidthLabels();

		this.setPrefHeight(this.editor.getPrefHeight());

		this.setCenter(this.editor);

		this.hBox = new HBox();
		this.hBox.setAlignment(Pos.CENTER);
		setAlignment(this.hBox, Pos.CENTER);
		setAlignment(this.editor, Pos.CENTER);
		this.setRight(this.hBox);

		this.shadowLabel = new Label();
		this.shadowLabel.prefHeightProperty().bind(this.editor.heightProperty().multiply(1));

		disableDefaultContextMenu();
		listeners();
		showButtons();
	}

	public void setOnAction(EventHandler<ActionEvent> event)
	{
		this.editor.setOnAction(event);
	}

	public void setOnContextMenuRequest(EventHandler<ContextMenuEvent> event)
	{
		this.editor.setOnContextMenuRequested(event);
		this.editor.setContextMenu(new ContextMenu());
	}

	public void setNotifierForErrorHandler()
	{
		setErrorHandler(e ->
		{
			logger.error(e.getMessage(), e);
			DialogsHelper.showError(String.format("Error on configuration [%s]", e.getMessage()));
		});

	}

	public void setChooserForExpressionField(String title, ListProvider provider)
	{
		setFirstActionListener(str ->
		{
			String currentText = getText();
			try
			{
				List<ReadableValue> list = provider.getList();
				ReadableValue value = new ReadableValue(str);
				value = DialogsHelper.selectFromList(title, value, list);
				return value.getValue();
			}
			catch (Exception e)
			{
				if (errorHandler != null)
				{
					errorHandler.error(e);
				}
				else
				{
					logger.error(e.getMessage(), e);
				}
			}
			return currentText;
		});
	}

	public void setHelperForExpressionField(String title, Matrix matrix)
	{
		setSecondActionListener(str -> DialogsHelper.showHelperDialog(title, evaluator, str, matrix) );
	}

	public void setErrorHandler(ErrorHandler handler)
	{
		this.errorHandler = handler;
	}

	public void setFirstActionListener(Function<String, String> handler)
	{
		this.firstAction = handler;
		showButtons();
	}

	public void setSecondActionListener(Function<String, String> handler)
	{
		this.secondAction = handler;
		showButtons();
	}

	public void setChangingValueListener(ChangeListener<Boolean> changeListener)
	{
		this.valueListener = changeListener;
	}

	public void setChangingFocusListener(ChangeListener<Boolean> changeListener)
	{
		this.focusListener = changeListener;
	}

	public void setText(String text)
	{
		this.editor.setText(text);
		if (text != null)
		{
			Tooltip toolTip = this.editor.getTooltip();
			if (toolTip == null)
			{
				this.editor.setTooltip(new Tooltip(text));
			}
			else
			{
				toolTip.setText(text);
			}
		}
		stretchIfCan(text);
	}

	public String getText()
	{
		return this.editor.getText();
	}

	public void sizeTextField()
	{
		String text = getText();
		Common.sizeTextField(this.editor);
		this.editor.setMinWidth(60);
		this.editor.setPrefWidth((Str.IsNullOrEmpty(text) ? 60 : text.length() * 8 + 20));
	}

	public Object getEvaluatedValue() throws Exception
	{
		return this.evaluator != null ? this.evaluator.evaluate(this.editor.getText()) : null;
	}

	public void showShadowText()
	{
		if (this.evaluator != null)
		{
			String shadowText;
			try
			{
				shadowText = String.valueOf(this.evaluator.evaluate(this.editor.getText()));
				this.shadowLabel.getStyleClass().removeAll(this.shadowLabel.getStyleClass());
				this.shadowLabel.getStyleClass().addAll("label", CssVariables.CORRECT_FIELD);
			}
			catch (Exception e)
			{
				shadowText = "Error";
				this.shadowLabel.getStyleClass().removeAll(this.shadowLabel.getStyleClass());
				this.shadowLabel.getStyleClass().addAll("label", CssVariables.INCORRECT_FIELD);
			}
			this.shadowLabel.setText(shadowText);
			this.shadowLabel.setTooltip(new Tooltip(shadowText));
			this.setCenter(shadowLabel);
			shadowLabel.toFront();
			shadowLabel.setPrefWidth(this.editor.getWidth());
			shadowLabel.setVisible(true);
		}
	}

	public void hideShadowText()
	{
		shadowLabel.setVisible(false);
		shadowLabel.toBack();
		this.setCenter(this.editor);
		this.getChildren().remove(shadowLabel);
	}

	public void setNameFirst(String name)
	{
		this.firstLabel.setText(name);
	}

	public void setNameSecond(String name)
	{
		this.secondLabel.setText(name);
	}

	public void clear()
	{
		this.editor.clear();
	}

	// ============================================================
	// private methods
	// ============================================================
	private void listeners()
	{
		this.firstLabel.setOnMouseClicked(mouseEvent ->
		{
			if (firstAction != null)
			{
				setText(firstAction.apply(getText()));
				this.editor.requestFocus();
			}
		});

		this.secondLabel.setOnMouseClicked(mouseEvent ->
		{
			if (secondAction != null)
			{
				setText(secondAction.apply(getText()));
				this.editor.requestFocus();
			}
		});

		this.editor.setOnKeyReleased(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.F4)
			{
				this.firstLabel.getOnMouseClicked().handle(null);
			}
			else if (keyEvent.getCode() == KeyCode.F5)
			{
				this.secondLabel.getOnMouseClicked().handle(null);
			}
		});
		
		this.editor.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean b = false;
			if (dragboard.hasString())
			{
				String str = dragboard.getString();
				this.editor.setText(str);
				stretchIfCan(str);
				Optional.ofNullable(valueListener).ifPresent(listener -> listener.changed(null, true, false));
				b = true;
			}
			event.setDropCompleted(b);
			event.consume();
		});

		this.editor.setOnDragOver(event ->
		{
			if (event.getGestureSource() != this.editor && event.getDragboard().hasString())
			{
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});
		
		
	}

	private void disableDefaultContextMenu()
	{
		final EventDispatcher eventDispatcher = this.editor.getEventDispatcher();
		this.editor.setEventDispatcher((event, eventDispatchChain) ->
		{
			if (event instanceof MouseEvent)
			{
				MouseEvent mouseEvent = (MouseEvent) event;
				if (mouseEvent.getButton() == MouseButton.SECONDARY)
				{
					event.consume();
				}
			}
			return eventDispatcher.dispatchEvent(event, eventDispatchChain);
		});
	}

	//TODO think about it
	private void stretchIfCan(String text)
	{
		int size = text != null ? (text.length() * 8 + 20) : 60;

		if (this.editor.getScene() != null)
		{
			double v = this.editor.getScene().getWindow().getWidth() / 3;
			if (size > v)
			{
				this.editor.setPrefWidth(v);
				return;
			}
		}
		this.editor.setPrefWidth(size);
	}

	private void setWidthLabels()
	{
		int pw = 20;
		int miw = 19;
		int maw = 21;

		this.firstLabel.setPrefWidth(pw);
		this.firstLabel.setMinWidth(miw);
		this.firstLabel.setMaxWidth(maw);

		this.secondLabel.setPrefWidth(pw);
		this.secondLabel.setMinWidth(miw);
		this.secondLabel.setMaxWidth(maw);
	}

	private void showButtons()
	{
		this.hBox.getChildren().clear();
		if (this.firstAction != null)
		{
			this.hBox.getChildren().add(firstLabel);
		}
		if (this.secondAction != null)
		{
			this.hBox.getChildren().add(secondLabel);
		}
	}

	private final static Logger	logger	= Logger.getLogger(ExpressionField.class);
}