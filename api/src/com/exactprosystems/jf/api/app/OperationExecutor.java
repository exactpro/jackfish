////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.client.ICondition;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface OperationExecutor<T>
{
	Rectangle					getRectangle	(T component) throws Exception;
	
	Color 						getColor		(String color) throws Exception;

	List<T> 					findAll			(ControlKind controlKind, T window, Locator locator																		) throws Exception;
	List<T> 					findAll			(Locator owner, Locator element																							) throws Exception;
	T 							find			(Locator owner, Locator element																							) throws Exception;
	T							lookAtTable		(T table, Locator additional, Locator header, int x, int y																) throws Exception;
	boolean						tableIsContainer();
	
	boolean 					mouse			(T component, int x, int y, MouseAction action																			) throws Exception;
	boolean						press			(T component, Keyboard key																								) throws Exception;
	boolean						upAndDown		(T component, Keyboard key, boolean b																					) throws Exception;
	boolean						push			(T component																											) throws Exception;
	boolean						toggle			(T component, boolean value																								) throws Exception;
	boolean						select			(T component, String selectedText																						) throws Exception;
	boolean						selectByIndex	(T component, int index																									) throws Exception;
	boolean						fold			(T component, String path, boolean collaps																				) throws Exception;
	boolean						text			(T component, String text, boolean clear																				) throws Exception;
	boolean						wait			(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong														) throws Exception;
	boolean 					setValue		(T component, double value																								) throws Exception;
	String						getValue		(T component																											) throws Exception;
	List<String>				getList			(T component																											) throws Exception;
	String						get				(T component																											) throws Exception;
	String						getAttr			(T component, String name																								) throws Exception;
	String						script			(T component, String script																								) throws Exception;

	boolean 					mouseTable		(T component, int column, int row, MouseAction action																	) throws Exception;
	boolean						textTableCell	(T component, int column, int row, String text																			) throws Exception;
	String						getValueTableCell(T component, int column, int row																						) throws Exception;
	Map<String, String> 		getRow			(T component, Locator additional, @Deprecated Locator header, @Deprecated boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception;
	List<String> 				getRowIndexes	(T component, Locator additional, @Deprecated Locator header, @Deprecated boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception;
	Map<String,String> 			getRowByIndex	(T component, Locator additional, @Deprecated Locator header, @Deprecated boolean useNumericHeader, String[] columns, int i						) throws Exception;
	Map<String, ValueAndColor>	getRowWithColor	(T component, Locator additional, @Deprecated Locator header, @Deprecated boolean useNumericHeader, String[] columns, int i						) throws Exception;
	String[][]					getTable		(T component, Locator additional, @Deprecated Locator header, @Deprecated boolean useNumericHeader, String[] columns							) throws Exception;
	int							getTableSize	(T component, Locator additional, @Deprecated Locator header, @Deprecated boolean useNumericHeader												) throws Exception;
	
}
