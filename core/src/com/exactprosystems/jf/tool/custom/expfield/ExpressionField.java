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
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomField;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import javafx.beans.value.ChangeListener;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ExpressionField extends CustomField
{
	private AbstractEvaluator			evaluator;

	private StackPane					firstPane;
	private StackPane					secondPane;

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
		super(text);
		this.evaluator = evaluator;
		this.getStyleClass().add(CssVariables.EXPRESSION_EDITOR);
		ChangeListener<Boolean> globalListener = (observable, oldValue, newValue) ->
		{
			Optional.ofNullable(valueListener).ifPresent(listener -> listener.changed(observable, oldValue, newValue));
			Optional.ofNullable(focusListener).ifPresent(listener -> listener.changed(observable, oldValue, newValue));
			stretchIfCan(this.getText());
		};
		this.focusedProperty().addListener(globalListener);

		Label firstLabel = new Label("≡");
		firstLabel.getStyleClass().setAll(CssVariables.EXPRESSION_BUTTON);
		this.firstPane = new StackPane(firstLabel);
		this.firstPane.getStyleClass().setAll(CssVariables.EXPRESSION_FIRST_PANE);
		this.firstPane.setCursor(Cursor.DEFAULT);

		Label secondLabel = new Label("ﬁ");
		secondLabel.getStyleClass().setAll(CssVariables.EXPRESSION_BUTTON);
		this.secondPane = new StackPane(secondLabel);
		this.secondPane.getStyleClass().setAll(CssVariables.EXPRESSION_SECOND_PANE);
		this.secondPane.setCursor(Cursor.DEFAULT);

		sizeTextField();

		this.hBox = new HBox();
		this.hBox.setSpacing(5);
		this.hBox.setAlignment(Pos.CENTER);
		this.rightProperty().set(this.hBox);
		disableDefaultContextMenu();
		listeners();
		showButtons();
		stretchIfCan(this.getText());
	}

	public void setOnContextMenuRequest(EventHandler<ContextMenuEvent> event)
	{
		this.setOnContextMenuRequested(event);
		this.setContextMenu(new ContextMenu());
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

	public void sizeTextField()
	{
		String text = getText();
		Common.sizeTextField(this);
		this.setMinWidth(60);
		this.setPrefWidth((Str.IsNullOrEmpty(text) ? 60 : text.length() * 8 + 20));
	}

	public Object getEvaluatedValue() throws Exception
	{
		return this.evaluator != null ? this.evaluator.evaluate(this.getText()) : null;
	}

	private String savedText;
	private boolean isHide = true;

	public void showShadowText()
	{
		if (isHide)
		{
			savedText = this.getText();
			isHide = false;
			if (this.evaluator != null)
			{
				String shadowText;
				try
				{
					shadowText = String.valueOf(this.evaluator.evaluate(this.getText()));
					this.getStyleClass().add(CssVariables.CORRECT_FIELD);
				}
				catch (Exception e)
				{
					shadowText = "Error";
					this.getStyleClass().add(CssVariables.INCORRECT_FIELD);
				}
				this.setEditable(false);
				this.setText(shadowText);
			}
		}
	}

	public void hideShadowText()
	{
		this.getStyleClass().removeAll(CssVariables.INCORRECT_FIELD, CssVariables.CORRECT_FIELD);
		this.setText(savedText);
		this.setEditable(true);
		isHide = true;
	}

	public void setNameFirst(String name)
	{
		((Label) this.firstPane.getChildren().get(0)).setText(name);
	}

	public void setNameSecond(String name)
	{
		((Label) this.secondPane.getChildren().get(0)).setText(name);
	}

	// ============================================================
	// private methods
	// ============================================================
	private void listeners()
	{
		this.firstPane.setOnMouseClicked(mouseEvent ->
		{
			if (firstAction != null)
			{
				setText(firstAction.apply(getText()));
				this.requestFocus();
			}
		});

		this.secondPane.setOnMouseClicked(mouseEvent ->
		{
			if (secondAction != null)
			{
				setText(secondAction.apply(getText()));
				this.requestFocus();
			}
		});

		this.setOnKeyReleased(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.F4)
			{
				this.firstPane.getOnMouseClicked().handle(null);
			}
			else if (keyEvent.getCode() == KeyCode.F5)
			{
				this.secondPane.getOnMouseClicked().handle(null);
			}
		});

		this.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean b = false;
			if (dragboard.hasString())
			{
				String str = dragboard.getString();
				this.setText(str);
				stretchIfCan(str);
				Optional.ofNullable(valueListener).ifPresent(listener -> listener.changed(null, true, false));
				b = true;
			}
			event.setDropCompleted(b);
			event.consume();
		});

		this.setOnDragOver(event ->
		{
			if (event.getGestureSource() != this && event.getDragboard().hasString())
			{
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});
	}

	private void disableDefaultContextMenu()
	{
		final EventDispatcher eventDispatcher = this.getEventDispatcher();
		this.setEventDispatcher((event, eventDispatchChain) ->
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
	public void stretchIfCan(String text)
	{
		int size = text != null ? (text.length() * 8 + 20) : 60;

		if (this.getScene() != null)
		{
			double v = this.getScene().getWindow().getWidth() / 3;
			if (size > v)
			{
				this.setPrefWidth(v);
				return;
			}
		}
		this.setPrefWidth(size+20);
	}

	private void showButtons()
	{
		this.hBox.getChildren().clear();
		if (this.firstAction != null)
		{
			this.hBox.getChildren().add(firstPane);
		}
		if (this.secondAction != null)
		{
			this.hBox.getChildren().add(secondPane);
		}
	}

	private final static Logger	logger	= Logger.getLogger(ExpressionField.class);
}