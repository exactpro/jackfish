////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.expfield;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
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
	private static final Logger logger = Logger.getLogger(ExpressionField.class);
	private static final int MIN_WIDTH = 80;

	private final AbstractEvaluator evaluator;
	private       StackPane         firstPane;
	private       StackPane         secondPane;
	private       HBox              hBox;
	private       boolean           isStretchable;

	private ErrorHandler             errorHandler;
	private Function<String, String> firstAction;
	private Function<String, String> secondAction;

	private final ChangeListener<Boolean> globalListener;
	private       ChangeListener<Boolean> valueListener;
	private       ChangeListener<Boolean> focusListener;

	public ExpressionField(AbstractEvaluator evaluator)
	{
		this(evaluator, null);
	}

	public ExpressionField(AbstractEvaluator evaluator, String text)
	{
		super(text);
		this.evaluator = evaluator;
		super.setMinWidth(MIN_WIDTH);
		super.setPrefWidth(MIN_WIDTH);
		super.getStyleClass().add(CssVariables.EXPRESSION_EDITOR);
		this.globalListener = (observable, oldValue, newValue) ->
		{
			Optional.ofNullable(this.valueListener).ifPresent(listener -> listener.changed(observable, oldValue, newValue));
			Optional.ofNullable(this.focusListener).ifPresent(listener -> listener.changed(observable, oldValue, newValue));
			this.stretchField(super.getText());
		};
		super.focusedProperty().addListener(this.globalListener);

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

		this.hBox = new HBox();
		this.hBox.setSpacing(5);
		this.hBox.setAlignment(Pos.CENTER);
		super.rightProperty().set(this.hBox);
		this.disableDefaultContextMenu();
		this.listeners();
		this.showButtons();
		this.stretchField(this.getText());
	}

	//region public API
	public void clearListener()
	{
		super.focusedProperty().removeListener(this.globalListener);
	}

	public void setOnContextMenuRequest(EventHandler<ContextMenuEvent> event)
	{
		super.setOnContextMenuRequested(event);
		super.setContextMenu(new ContextMenu());
	}

	public void setNotifierForErrorHandler()
	{
		this.setErrorHandler(e ->
		{
			logger.error(e.getMessage(), e);
			DialogsHelper.showError(String.format(R.ERROR_ON_CONFIGURATION.get(), e.getMessage()));
		});

	}

	public void setChooserForExpressionField(String title, ListProvider provider)
	{
		this.setFirstActionListener(str ->
		{
			String currentText = super.getText();
			try
			{
				List<ReadableValue> list = provider.getList();
				ReadableValue value = new ReadableValue(str);
				value = DialogsHelper.selectFromList(title, value, list);
				return value.getValue();
			}
			catch (Exception e)
			{
				if (this.errorHandler != null)
				{
					this.errorHandler.error(e);
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
		this.setSecondActionListener(str -> DialogsHelper.showHelperDialog(title, evaluator, str, matrix));
	}

	public void setErrorHandler(ErrorHandler handler)
	{
		this.errorHandler = handler;
	}

	public void setFirstActionListener(Function<String, String> handler)
	{
		this.firstAction = handler;
		this.showButtons();
	}

	public void setSecondActionListener(Function<String, String> handler)
	{
		this.secondAction = handler;
		this.showButtons();
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
		String text = super.getText();
		this.stretchField(text);
	}

	public Object getEvaluatedValue() throws Exception
	{
		return this.evaluator != null ? this.evaluator.evaluate(super.getText()) : null;
	}

	private String savedText;
	private boolean isShadowTextIsNotPresented = true;

	public void showShadowText()
	{
		if (this.isShadowTextIsNotPresented)
		{
			this.savedText = super.getText();
			this.isShadowTextIsNotPresented = false;
			if (this.evaluator != null)
			{
				String shadowText;
				try
				{
					shadowText = String.valueOf(this.evaluator.evaluate(super.getText()));
					super.getStyleClass().add(CssVariables.CORRECT_FIELD);
				}
				catch (Exception e)
				{
					shadowText = "Error";
					super.getStyleClass().add(CssVariables.INCORRECT_FIELD);
				}
				super.setEditable(false);
				super.setText(shadowText);
			}
		}
	}

	public void hideShadowText()
	{
		super.getStyleClass().removeAll(CssVariables.INCORRECT_FIELD, CssVariables.CORRECT_FIELD);
		super.setText(this.savedText);
		super.setEditable(true);
		this.isShadowTextIsNotPresented = true;
	}

	public void setNameFirst(String name)
	{
		((Label) this.firstPane.getChildren().get(0)).setText(name);
	}

	public void setNameSecond(String name)
	{
		((Label) this.secondPane.getChildren().get(0)).setText(name);
	}

	public void setStretchable(boolean flag)
	{
		this.isStretchable = flag;
	}
	//endregion

	//region private methods
	private void listeners()
	{
		this.firstPane.setOnMouseClicked(mouseEvent ->
		{
			if (this.firstAction != null)
			{
				super.setText(this.firstAction.apply(super.getText()));
				super.requestFocus();
			}
		});
		this.secondPane.setOnMouseClicked(mouseEvent ->
		{
			if (this.secondAction != null)
			{
				super.setText(this.secondAction.apply(super.getText()));
				super.requestFocus();
			}
		});

		super.setOnKeyReleased(keyEvent ->
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
		super.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean isCompleted = false;
			if (dragboard.hasString())
			{
				String str = dragboard.getString();
				super.setText(str);
				this.stretchField(str);
				Optional.ofNullable(this.valueListener).ifPresent(listener -> listener.changed(null, true, false));
				isCompleted = true;
			}
			event.setDropCompleted(isCompleted);
			event.consume();
		});
		super.setOnDragOver(event ->
		{
			if (event.getGestureSource() != this && event.getDragboard().hasString())
			{
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});
		super.textProperty().addListener((observable, oldValue, newValue) ->
		{
			if (this.isStretchable)
			{
				this.stretchField(newValue);
			}
		});
	}

	private void disableDefaultContextMenu()
	{
		final EventDispatcher eventDispatcher = super.getEventDispatcher();
		super.setEventDispatcher((event, eventDispatchChain) ->
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

	private void stretchField(String text)
	{
		if (this.isShadowTextIsNotPresented)
		{
			Common.runLater(() -> super.setPrefWidth(Common.computeTextWidth(super.getFont(), text, 0.0D) + 40));
		}
	}

	private void showButtons()
	{
		this.hBox.getChildren().clear();
		if (this.firstAction != null)
		{
			this.hBox.getChildren().add(this.firstPane);
		}
		if (this.secondAction != null)
		{
			this.hBox.getChildren().add(this.secondPane);
		}
	}
	//endregion
}