/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public abstract class SpreadsheetCellEditor
{
	private static final double MAX_EDITOR_HEIGHT = 50.0;

	SpreadsheetView view;

	public SpreadsheetCellEditor(SpreadsheetView view)
	{
		this.view = view;
	}

	public final void endEdit(boolean b)
	{
		view.getCellsViewSkin().getSpreadsheetCellEditorImpl().endEdit(b);
	}

	public abstract void startEdit(Object item);

	public abstract Control getEditor();

	public abstract String getControlValue();

	public abstract void end();

	public double getMaxHeight()
	{
		return MAX_EDITOR_HEIGHT;
	}


	public static class StringEditor extends SpreadsheetCellEditor
	{

		private final TextField tf;

		public StringEditor(SpreadsheetView view)
		{
			super(view);
			tf = new TextField();
		}

		@Override
		public void startEdit(Object value)
		{

			if (value instanceof String || value == null)
			{
				tf.setText((String) value);
			}
			attachEnterEscapeEventHandler();

			tf.requestFocus();
			tf.end();
			//			tf.positionCaret(tf.sel);
			tf.selectAll();
		}

		@Override
		public String getControlValue()
		{
			return tf.getText();
		}

		@Override
		public void end()
		{
			tf.setOnKeyPressed(null);
		}

		@Override
		public TextField getEditor()
		{
			return tf;
		}

		private void attachEnterEscapeEventHandler()
		{
			tf.setOnKeyPressed(t -> {
				if (t.getCode() == KeyCode.ENTER)
				{
					endEdit(true);
				}
				else if (t.getCode() == KeyCode.ESCAPE)
				{
					endEdit(false);
				}
			});
		}
	}
}
