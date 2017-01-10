////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.functions.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DisplayDriver
{
	Object 		createLayout		(MatrixItem item, int lines);

	void		showTitle			(MatrixItem item, Object layout, int row, int column, String name, Settings settings);
	void 		showLabel			(MatrixItem item, Object layout, int row, int column, String name, int fontSize);
	void 		showCheckBox		(MatrixItem item, Object layout, int row, int column, String name, Setter<Boolean> set, Getter<Boolean> get);
	void 		showComboBox		(MatrixItem item, Object layout, int row, int column, Setter<String> set, Getter<String> get, Function<Void, List<String>> handler);
	void 		showTextBox			(MatrixItem item, Object layout, int row, int column, Setter<String> set, Getter<String> get, FormulaGenerator generator);
	void 		showExpressionField	(MatrixItem item, Object layout, int row, int column, String name, Setter<String> set, Getter<String> get,
										Function<String, String> firstHandler, Function<String, String> secondHandler, Character first, Character second);
	void 		showTextArea		(MatrixItem item, Object layout, int row, int column, Text text, Consumer<List<String>> consumer);
	void		showAutoCompleteBox	(MatrixItem item, Object layout, int row, int column, List<String> words, Consumer<String> supplier);
	void 		showComment			(MatrixItem item, Object layout, int row, int column, List<CommentString> lines);
	void 		showButton			(MatrixItem item, Object layout, int row, int column, String name, Function<MatrixItem, Void> action);
	void 		showToggleButton	(MatrixItem item, Object layout, int row, int column, String name, Function<Boolean, Void> action, Function<Boolean, String> changeName, boolean initialValue);
	void 		showParameters		(MatrixItem item, Object layout, int row, int column, Parameters parameters, FormulaGenerator generator, boolean oneLine);
	void 		showGrid			(MatrixItem item, Object layout, int row, int column, Table table);
	void 		hide				(MatrixItem item, Object layout, int row, boolean hide);
	void		setupCall			(MatrixItem item, String reference, Parameters parameters);
	void 		setCurrentItem		(MatrixItem item);
	void		deleteItem			(MatrixItem item);
	void		layoutWizard		(MatrixItem item, Table table, Context context);
}
