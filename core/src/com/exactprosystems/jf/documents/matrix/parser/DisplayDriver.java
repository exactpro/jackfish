/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.client.IMessageDictionary;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.functions.Text;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The interface for displaying items from a matrix.
 * All methods contains row and column numbers , because every layout is a simple Grid ( with columns and rows)
 *
 * @see com.exactprosystems.jf.tool.custom.treetable.DisplayDriverFx
 */
public interface DisplayDriver
{
	/**
	 * Create layout for display a simple controls.
	 *
	 * @param item the item for creating a layout
	 * @param lines number of lines that are necessary for the item. This number is -1 only for {@link com.exactprosystems.jf.documents.matrix.parser.items.End} item
	 *
	 * @return a layout, which used for all methods for displaying
	 */
	Object createLayout(MatrixItem item, int lines);

	/**
	 * Display a simple title for the item on the passed layout.
	 * <p>
	 * Difference between {@code showTitle} and {@code showLabel} is that
	 * <ul>
	 * <li>the title is clickable : on click on title will show comments for the item</li>
	 * <li>the title can change a color ( based on settings)</li>
	 * </ul>
	 *
	 * @param item the item, for which display title
	 * @param layout the layout for the item for display the title
	 * @param row the number of row, where need display the title
	 * @param column the number of columns, where need display the title
	 * @param name the displaying title text
	 * @param settings the settings. It's used for change color of the title
	 *
	 * @see Settings#MATRIX_COLORS
	 */
	void showTitle(MatrixItem item, Object layout, int row, int column, String name, Settings settings);

	/**
	 * Display a simple label for the item on the passed layout
	 *
	 * @param item the item, for which display the label
	 * @param layout the layout for the item for display the label
	 * @param row the number of row, where need display the label
	 * @param column the number of columns, where need display the label
	 * @param name the displaying label text
	 */
	void showLabel(MatrixItem item, Object layout, int row, int column, String name);

	/**
	 * Display a simple checkbox for the item on the passed layout.
	 *
	 * @param item the item, for which display the checkbox
	 * @param layout the layout for display the checkbox
	 * @param row the number of row, where need display the checkbox
	 * @param column the number of columns, where need display the checkbox
	 * @param name the displaying checkbox text
	 * @param set the listener, which notify, that checkbox change value
	 * @param get the listener, which used for get value from the item and set in the checkbox
	 */
	void showCheckBox(MatrixItem item, Object layout, int row, int column, String name, Consumer<Boolean> set, Supplier<Boolean> get);

	/**
	 * Display a simple combobox for the item on the current layout
	 *
	 * @param item the item, for which display the combobox
	 * @param layout the layout for display the combobox
	 * @param row the number of row, where need display the combobox
	 * @param column the number of columns, where need display the combobox
	 * @param set the listener, which used for notify, that combobox change a value
	 * @param get the listener, which used for get value from the item
	 * @param handler the supplier for get all available values for the combobox
	 * @param needUpdate the function which used for understand, need update a value of the combobox or not
	 */
	void showComboBox(MatrixItem item, Object layout, int row, int column, Consumer<String> set, Supplier<String> get, Supplier<List<String>> handler, Function<String, Boolean> needUpdate);

	/**
	 * Display a simple textfield for the item on the current layout
	 *  @param item the item, for which display the textfield
	 * @param layout the layout for display the textfield
	 * @param row the number of row, where need display the textfield
	 * @param column the number of columns, where need display the textfield
	 * @param set the listener, which used for notify, that a text from the textfield is changed
	 * @param get the listener, which used for get a text from item and set into the textfield
	 * @param generator a formulaGenerator functional interface. Used for generate text via dragNdrop from the textfield to another place
	 * @param placeholder the description which can help to fill textfield
	 */
	void showTextBox(MatrixItem item, Object layout, int row, int column, Consumer<String> set, Supplier<String> get, FormulaGenerator generator, String placeholder);

	/**
	 * Display a simple expression field for the item on the current layout.
	 *
	 * @param item the item, for which display the expression field
	 * @param layout the layout for display the expression field
	 * @param row the number of row, where need display the expression field
	 * @param column the number of columns, where need display the expression field
	 * @param name the name, which used on {@link com.exactprosystems.jf.tool.custom.expfield.ExpressionField#setHelperForExpressionField(String, Matrix)}
	 * @param set the listener, which used for notify, that a text from the expression field changed
	 * @param get the listener, which used for get a text from the item and set to the expression field
	 * @param firstHandler the first handler for expression field. See also below.
	 * @param secondHandler the second handler for expression field. See also below.
	 * @param first the first character for display icon for the first handler
	 * @param second the second character for display icon for the second handler
	 *
	 * @see com.exactprosystems.jf.tool.custom.expfield.ExpressionField
	 * @see com.exactprosystems.jf.tool.custom.expfield.ExpressionField#setFirstActionListener(Function)
	 * @see com.exactprosystems.jf.tool.custom.expfield.ExpressionField#setSecondActionListener(Function)
	 * @see com.exactprosystems.jf.tool.custom.expfield.ExpressionField#setNameFirst(String)
	 * @see com.exactprosystems.jf.tool.custom.expfield.ExpressionField#setNameSecond(String)
	 */
	void showExpressionField(MatrixItem item, Object layout, int row, int column, String name, Consumer<String> set, Supplier<String> get, Function<String, String> firstHandler, Function<String, String> secondHandler, Character first, Character second);

	/**
	 * Display a simple text area for the item on the current layout
	 *
	 * @param item the item, for which display the text area
	 * @param layout the layout for display the text area
	 * @param row the number of row, where need display the text area
	 * @param column the number of columns, where need display the text area
	 * @param text the {@link Text} object for displaying on the text area
	 * @param consumer the consumer, which used for notify changes on the text area
	 * @param highlighter the highlighter kind. Used for change text highlighting
	 */
	void showTextArea(MatrixItem item, Object layout, int row, int column, Text text, Consumer<List<String>> consumer, Highlighter highlighter);

	/**
	 * This method used for update the new Text for the text area for the item on the layout
	 *
	 * @param text the new Text
	 */
	void updateTextArea(MatrixItem item, Object layout, Text text);

	/**
	 * Display a simple autocomplete textfield for the item on the current layout
	 *
	 * @param item the item, for which display the autocomplete textfield
	 * @param layout the layout for display the autocomplete textfield
	 * @param row the number of row, where need display the autocomplete textfield
	 * @param column the number of columns, where need display the autocomplete textfield
	 * @param wordsSupplier a supplier, used for get all available strings for autocomplete
	 * @param init the initial value for the autocomplete
	 * @param supplier the listener, which used, when the autocomplete done
	 */
	void showAutoCompleteBox(MatrixItem item, Object layout, int row, int column, Supplier<List<String>> wordsSupplier, Supplier<String> init, Consumer<String> supplier);

	/**
	 * Display comments for the item
	 *
	 * @param item the item, for which display the comments
	 * @param layout the layout for display the comments
	 * @param row the number of row, where need display the comments
	 * @param column the number of columns, where need display the comments
	 * @param lines the list of the comment lines
	 */
	void showComment(MatrixItem item, Object layout, int row, int column, List<MutableValue<String>> lines);

	/**
	 * Display a simple button for the item on the current layout
	 *
	 * @param item the item, for which display the button
	 * @param layout the layout for display the button
	 * @param row the number of row, where need display the button
	 * @param column the number of columns, where need display the button
	 * @param name a text on the button
	 * @param action a listener, which for notify, that button is pushed
	 */
	void showButton(MatrixItem item, Object layout, int row, int column, String name, Consumer<MatrixItem> action);

	/**
	 * Display a simple spinner
	 *
	 * @param item the item, for which display the spinner
	 * @param layout the layout for display the spinner
	 * @param row the number of row, where need display the spinner
	 * @param column the number of columns, where need display the spinner
	 * @param prefWidth preferred width for the spinner
	 * @param set the listener, which used for notify, that a value from spinner changed
	 * @param get the listener, which used for a get int value from the item and set it to the spinner
	 * @param minValue the minimal value for the spinner
	 * @param maxValue the maximum value for the spinner
	 *
	 * @see com.exactprosystems.jf.tool.custom.number.NumberTextField
	 */
	void showSpinner(MatrixItem item, Object layout, int row, int column, double prefWidth, Consumer<Integer> set, Supplier<Integer> get, int minValue, int maxValue);

	/**
	 * This method used for extends or cut the table to passed number of column and row.
	 * BooleanSupplier used for check, do extends ( cut) or not
	 */
	void extendsTable(Object layout, int prefCols, int prefRows, BooleanSupplier supplier);

	/**
	 * Change highlighting for the text area on the layout
	 *
	 * @see DisplayDriver#showTextArea(MatrixItem, Object, int, int, Text, Consumer, Highlighter)
	 */
	void displayHighlight(Object layout, Highlighter highlighter);

	/**
	 * Display a simple toggle button for the item on the current layout
	 *
	 * @param item the item, for which display the toggle button
	 * @param layout the layout for display the toggle button
	 * @param row the number of row, where need display the toggle button
	 * @param column the number of columns, where need display the toggle button
	 * @param action the listener, which used for notify, that the toggle button change a value ( is pressed or released)
	 * @param changeName the listener, which used for change the name, depends on type of the toggle button ( is pressed or released)
	 * @param initialValue the initial value for the toggle button
	 */
	void showToggleButton(MatrixItem item, Object layout, int row, int column, Consumer<Boolean> action, Function<Boolean, String> changeName, boolean initialValue);

	/**
	 * Display parameters for the item on the current layout
	 *
	 * @param item the item, for which display the parameters pane
	 * @param layout the layout for display the parameters pane
	 * @param row the number of row, where need display the parameters pane
	 * @param column the number of columns, where need display the parameters pane
	 * @param parameters the parameters, which should displayed
	 * @param generator a formulaGenerator functional interface. Used for generate text via dragNdrop from the parameters pane to another place
	 * @param oneLine indicate, how many lines on the layout these parameters should take2
	 */
	void showParameters(MatrixItem item, Object layout, int row, int column, Parameters parameters, FormulaGenerator generator, boolean oneLine);
	/**
	 * Display a table for the item on the current layout
	 *
	 * @param item the item, for which display the table
	 * @param layout the layout for display the table
	 * @param row the number of row, where need display the table
	 * @param column the number of columns, where need display the table
	 * @param table the table object, which should displayed
	 *
	 * @see Table
	 */
	void showGrid(MatrixItem item, Object layout, int row, int column, Table table);

	void showTree(MatrixItem item, Object layout, int row, int column, MapMessage message, IMessageDictionary dictionary, Context context);
	void updateTree(MatrixItem item, Object layout, MapMessage message, IMessageDictionary dictionary);

	/**
	 * Hide or show the passed row in the current layout for the item
	 *
	 * @param item the item, for which display the text area
	 * @param layout the layout for display the text area
	 * @param row the number of row, which should hide or show
	 * @param hide if this parameter is true, a line with passed number will hide. Otherwise the line will show
	 */
	void hide(MatrixItem item, Object layout, int row, boolean hide);

	/**
	 * Select the item on the GUI tree. The parameter needExpand indicate, need expand the selected item or not
	 */
	void setCurrentItem(MatrixItem item, Matrix matrix, boolean needExpand);

	/**
	 * Delete the item on the GUI tree
	 */
	void deleteItem(MatrixItem item);
}
