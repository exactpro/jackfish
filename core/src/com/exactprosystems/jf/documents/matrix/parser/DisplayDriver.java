////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.client.IMessageDictionary;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.functions.Text;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface DisplayDriver
{
	Object 		createLayout		(MatrixItem item, int lines);

	void		showTitle			(MatrixItem item, Object layout, int row, int column, String name, Settings settings);
	void 		showLabel			(MatrixItem item, Object layout, int row, int column, String name);
	void 		showCheckBox		(MatrixItem item, Object layout, int row, int column, String name, Setter<Boolean> set, Getter<Boolean> get);
	void 		showComboBox		(MatrixItem item, Object layout, int row, int column, Setter<String> set, Getter<String> get, Supplier<List<String>> handler, Function<String, Boolean> needUpdate);
	void 		showTextBox			(MatrixItem item, Object layout, int row, int column, Setter<String> set, Getter<String> get, FormulaGenerator generator);
	void 		showExpressionField	(MatrixItem item, Object layout, int row, int column, String name, Setter<String> set, Getter<String> get,
										Function<String, String> firstHandler, Function<String, String> secondHandler, Character first, Character second);
	void 		showTextArea		(MatrixItem item, Object layout, int row, int column, Text text, Consumer<List<String>> consumer, Highlighter highlighter);
	void		updateTextArea		(MatrixItem item, Object layout, Text text);
	void		showAutoCompleteBox	(MatrixItem item, Object layout, int row, int column, List<String> words, Consumer<String> supplier);
	void 		showComment			(MatrixItem item, Object layout, int row, int column, List<CommentString> lines);
	void 		showButton			(MatrixItem item, Object layout, int row, int column, String name, Consumer<MatrixItem> action);

	void		showSpinner(MatrixItem item, Object layout, int row, int column, double prefWidth, Setter<Integer> set, Getter<Integer> get, int minValue, int maxValue);

	void		extendsTable		(Object layout, int prefCols, int prefRows, BooleanSupplier supplier);
	void		displayHighlight	(Object layout, Highlighter highlighter);
	void 		showToggleButton	(MatrixItem item, Object layout, int row, int column, Consumer<Boolean> action, Function<Boolean, String> changeName, boolean initialValue);
	void 		showParameters		(MatrixItem item, Object layout, int row, int column, Parameters parameters, FormulaGenerator generator, boolean oneLine);
	void 		showGrid			(MatrixItem item, Object layout, int row, int column, Table table);
	void		showTree(MatrixItem item, Object layout, int row, int column, MapMessage message, IMessageDictionary dictionary, Context context);
	void 		updateTree			(MatrixItem item, Object layout, MapMessage message, IMessageDictionary dictionary);
	void 		hide				(MatrixItem item, Object layout, int row, boolean hide);
	void		setupCall			(MatrixItem item, String reference, Parameters parameters);
	void 		setCurrentItem		(MatrixItem item, Matrix matrix, boolean needExpand);
	void		deleteItem			(MatrixItem item);
}
