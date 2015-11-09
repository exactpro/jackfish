////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public abstract class SpreadsheetCellEditor {
    private static final double MAX_EDITOR_HEIGHT = 50.0;

    SpreadsheetView view;

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Construct the SpreadsheetCellEditor.
     *
     * @param view
     */
    public SpreadsheetCellEditor(SpreadsheetView view) {
        this.view = view;
    }

    /***************************************************************************
     * * Public Final Methods * *
     **************************************************************************/
    /**
     * Whenever you want to stop the edition, you call that method.<br>
     * True means you're trying to commit the value, then
     * {@link SpreadsheetCellType#convertValue(Object)} will be called in order
     * to verify that the value is correct.<br>
     * False means you're trying to cancel the value and it will be follow by
     * {@link #end()}.<br>
     * See SpreadsheetCellEditor description
     *
     * @param b
     *            true means commit, false means cancel
     */
    public final void endEdit(boolean b) {
        view.getCellsViewSkin().getSpreadsheetCellEditorImpl().endEdit(b);
    }

    /***************************************************************************
     * * Public Abstract Methods * *
     **************************************************************************/
    /**
     * This method will be called when edition start.<br>
     * You will then do all the configuration of your editor.
     *
     * @param item
     */
    public abstract void startEdit(Object item);

    /**
     * Return the control used for controlling the input. This is called at the
     * beginning in order to display your control in the cell.
     *
     * @return the control used.
     */
    public abstract Control getEditor();

    /**
     * Return the value within your editor as a string. This will be used by the
     * {@link SpreadsheetCellType#convertValue(Object)} in order to compute
     * whether the value is valid regarding the {@link SpreadsheetCellType}
     * policy.
     *
     * @return the value within your editor as a string.
     */
    public abstract String getControlValue();

    /**
     * This method will be called at the end of edition.<br>
     * You will be offered the possibility to do the configuration post editing.
     */
    public abstract void end();

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    /**
     * Return the maximum height of the editor.
     * @return 50 by default.
     */
    public double getMaxHeight(){
        return MAX_EDITOR_HEIGHT;
    }

    /**
     * A {@link SpreadsheetCellEditor} for
     * {@link StringCellType} typed cells. It displays a
     * {@link TextField} where the user can type different values.
     */
    public static class StringEditor extends SpreadsheetCellEditor
	{
        /***************************************************************************
         * * Private Fields * *
         **************************************************************************/
        private final TextField tf;

        /***************************************************************************
         * * Constructor * *
         **************************************************************************/
        /**
         * Constructor for the StringEditor.
         * @param view The SpreadsheetView
         */
        public StringEditor(SpreadsheetView view) {
            super(view);
            tf = new TextField();
        }

        /***************************************************************************
         * * Public Methods * *
         **************************************************************************/
        @Override
        public void startEdit(Object value) {

            if (value instanceof String || value == null) {
                tf.setText((String) value);
            }
            attachEnterEscapeEventHandler();

            tf.requestFocus();
            tf.end();
        }

        @Override
        public String getControlValue() {
            return tf.getText();
        }

        @Override
        public void end() {
            tf.setOnKeyPressed(null);
        }

        @Override
        public TextField getEditor() {
            return tf;
        }

        /***************************************************************************
         * * Private Methods * *
         **************************************************************************/

        private void attachEnterEscapeEventHandler() {
            tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        endEdit(true);
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        endEdit(false);
                    }
                }
            });
        }
    }
}
