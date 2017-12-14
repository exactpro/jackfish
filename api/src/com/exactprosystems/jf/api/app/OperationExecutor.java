////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.client.ICondition;
import org.w3c.dom.Document;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface OperationExecutor<T>
{
	void                        setPluginInfo   (PluginInfo info);
	/**
	 * @return true, if the passed operation allowed for passed kind
	 *
	 * @see OperationKind
	 * @see ControlKind
	 */
	boolean                     isAllowed       (ControlKind kind, OperationKind operation);
	/**
	 * @return true, if current plugin supported a kind
	 *
	 * @see ControlKind
	 */
	boolean						isSupported		(ControlKind kind);
	/**
	 * Available kinds : All
	 * @return java.awt.Rectangle for current component.
	 *
	 * @see Rectangle
	 * @see ControlKind
	 */
	Rectangle					getRectangle	(T component) throws Exception;
	/**
	 * Available kinds : All
	 * @return color for the component. If isForeground true, will return foreground color. Otherwise will return background color
	 *
	 * @see ControlKind
	 */
	Color						getColor		(T component, boolean isForeground																						) throws Exception;
	/**
	 * Find all components from the window, which matches by locator
	 * @param controlKind found kind
	 * @param window owner for find
	 * @param locator elements, which matches
	 * @return list of all matches components
	 */
	List<T> 					findAll			(ControlKind controlKind, T window, Locator locator																		) throws Exception;
	/**
	 * @param owner locator, which use as owner for finding components
	 * @param element locator for finding components
	 * @return list of all founded components
	 */
	List<T> 					findAll			(Locator owner, Locator element																							) throws Exception;
	/**
	 * @param owner locator, which use as owner for found component
	 * @param element locator for find component
	 * @return found component. If components count is 0 or more than 1, will throw exception
	 */
	T 							find			(Locator owner, Locator element																							) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Tree}
	 * @param element element, which use for finding as owner
	 * @param path xpath for finding elements
	 * @return list of all components, which matches by passed xpath
	 *
	 * @see ControlKind
	 */
	List<T> 					findByXpath		(T element, String path																									) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table}
	 * @param table is table, which use for found cell as owner
	 * @param additional locator, for found rows of the table
	 * @param header locator, for found headers of the table
	 * @param x is column index. If column index is {@link Integer#MIN_VALUE}, will returned row of table by index row index
	 * @param y is row index.
	 * @return cell from the table by passed parameters
	 *
	 * @see ControlKind
	 */
	T							lookAtTable		(T table, Locator additional, Locator header, int x, int y																) throws Exception;
	/**
	 * Available kinds : All
	 * @return true, if component is enabled and false otherwise
	 * @see ControlKind
	 */
	boolean						elementIsEnabled(T component                                                                                                            ) throws Exception;
	/**
	 * Available kinds : All
	 * @return true, if component is visible and false otherwise
	 *
	 * @see ControlKind
	 */
	boolean                     elementIsVisible(T component                                                                                                            ) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table}
	 * @return attribute, which mean, that table is container for other elements
	 */
	boolean						tableIsContainer();
	/**
	 * Available kinds : All, except {@link ControlKind#Label} <br>
	 * Execute a mouse operation for component
	 * @param component component for operation
	 * @param x x coordinates for mouse operation
	 * @param y y coordinates for mouse operation
	 * @param action kind of mouse action
	 * @return true, if operation was execute successful
	 *
	 * @see MouseAction
	 * @see ControlKind
	 */
	boolean 					mouse			(T component, int x, int y, MouseAction action																			) throws Exception;
	/**
	 * Available kinds : All, except {@link ControlKind#ProgressBar}, {@link ControlKind#MenuItem},{@link ControlKind#Tooltip} <br>
	 * Execute a press operation for component
	 * @param component component for operation
	 * @param key key, which will pressed
	 * @return true, if operation was execute successful
	 *
	 * @see Keyboard
	 * @see ControlKind
	 */
	boolean						press			(T component, Keyboard key																								) throws Exception;
	/**
	 * Available kinds : see available kinds for {@link OperationExecutor#press(T, Keyboard)} <br>
	 * Press or release a key for component
	 * @param component component for operation
	 * @param key key, which will pressed or released
	 * @param b if parameter true, this mean, that key will pressed. Otherwise key will released
	 * @return true, if operation was execute successful
	 *
	 * @see Keyboard
	 */
	boolean						upAndDown		(T component, Keyboard key, boolean b																					) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Button} <br>
	 * Execute a push for the button
	 * @param component which will use for pushing
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						push			(T component																											) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#CheckBox}, {@link ControlKind#RadioButton} and {@link ControlKind#ToggleButton} <br>
	 * Set a state of the current component to @value
	 * @param component which will use for changing the state
	 * @param value new state of the component
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						toggle			(T component, boolean value																								) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#ComboBox}, {@link ControlKind#TabPanel}, {@link ControlKind#ListView} and {@link ControlKind#Tree} <br>
	 * Select the child from the component by text
	 * @param component which use as owner for selecting
	 * @param selectedText text of child, which need set selected
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						select			(T component, String selectedText																						) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#ComboBox}, {@link ControlKind#TabPanel} and {@link ControlKind#ListView} <br>
	 * Select the child from the component by index
	 * @param component which use as owner for selecting
	 * @param index index of child, which need set selected
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						selectByIndex	(T component, int index																									) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Menu}, {@link ControlKind#MenuItem} and {@link ControlKind#Tree} <br>
	 * Expand or collapse (depends of boolean parameter) the component
	 * @param component which will use for expanding/collapsing
	 * @param path path, which use for expanding/collapsing
	 * @param expandOrCollapse if the parameter is true, the component will expanded. Otherwise the component will collapsed
	 *
	 * @see ControlKind
	 */
	boolean						expand			(T component, String path, boolean expandOrCollapse																		) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#TextBox} <br>
	 * Insert the text for the component.
	 * @param component which used for inserting a text
	 * @param text a text, which will insert to a component
	 * @param clear if this parameter is true, then a component before inserting a text will cleaned. Otherwise the text will added to the end
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						text			(T component, String text, boolean clear																				) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Wait} <br>
	 * Used for waiting component on the screen
	 * @param locator locator, which used for waiting
	 * @param ms a time for waiting ( in milliseconds)
	 * @param toAppear if this parameter is true, then wait while component is appear on the application. Otherwise wait while component will disappear from application
	 * @param atomicLong parameter which saved time for waiting
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						wait			(Locator locator, int ms, boolean toAppear, AtomicLong atomicLong														) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#ScrollBar}, {@link ControlKind#Splitter} and {@link ControlKind#Slider} <br>
	 * Set a progress for the component
	 * @param component which used for setting the progress
	 * @param value a value, which will setting
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean 					setValue		(T component, double value																								) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Button}, {@link ControlKind#CheckBox}, {@link ControlKind#ComboBox}, {@link ControlKind#Label}, {@link ControlKind#ListView}, {@link ControlKind#Panel}, {@link ControlKind#ProgressBar}, {@link ControlKind#RadioButton}, {@link ControlKind#ScrollBar}, {@link ControlKind#Slider}, {@link ControlKind#Splitter}, {@link ControlKind#Spinner}, {@link ControlKind#TabPanel}, {@link ControlKind#TextBox}, {@link ControlKind#ToggleButton}, {@link ControlKind#Tooltip}, {@link ControlKind#Tree}, {@link ControlKind#TreeItem} <br>
	 * Return the value from the component
	 * @param component which used for getting a value
	 * @return string representation of the value of the component
	 *
	 * @see ControlKind
	 */
	String						getValue		(T component																											) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#ComboBox}, {@link ControlKind#ListView}, {@link ControlKind#TabPanel} <br>
	 * Return list of items from the current component
	 * @param component which used for setting the progress
	 * @param onlyVisible mean, return only visible (on the screen) items or all items.
	 * @return List of items
	 *
	 * @see ControlKind
	 */
	List<String>				getList			(T component, boolean onlyVisible																						) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Tree} <br>
	 * Convert current component to the XML
	 * @return XML document of the current component
	 *
	 * @see ControlKind
	 * @see Document
	 */
	Document					getTree			(T component																											) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Button}, {@link ControlKind#CheckBox}, {@link ControlKind#ComboBox}, {@link ControlKind#Label}, {@link ControlKind#Menu}, {@link ControlKind#MenuItem}, {@link ControlKind#RadioButton}, {@link ControlKind#Row}, {@link ControlKind#TextBox}, {@link ControlKind#ToggleButton}, {@link ControlKind#Tooltip} <br>
	 * @return the visible text of the component
	 *
	 * @see ControlKind
	 */
	String						get				(T component																											) throws Exception;
	/**
	 * Available kinds : All, expect {@link ControlKind#Tooltip}
	 * Return value of parameter with name @name
	 * @param name of parameter
	 * @return value of the parameter with passed name
	 *
	 * @see ControlKind
	 */
	String						getAttr			(T component, String name																								) throws Exception;
	/**
	 * Available kinds : All <br>
	 * Execute a javascript for the element. Supported only for web plugin
	 * @param component which used for executing a javascript
	 * @param script a javascript, which will execute
	 * @return result of the script
	 *
	 * @see ControlKind
	 */
	String						script			(T component, String script																								) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Any}, {@link ControlKind#Button}, {@link ControlKind#Dialog}, {@link ControlKind#Frame}, {@link ControlKind#Image}, {@link ControlKind#Label}, {@link ControlKind#Panel}, {@link ControlKind#Slider}, {@link ControlKind#TextBox}, {@link ControlKind#ToggleButton}, {@link ControlKind#TreeItem} <br>
	 * Execute the drag and drop operation
	 * @param drag component, which will dragged
	 * @param x1 start x coordinate of the drag component
	 * @param y1 start y coordinate of the drag component
	 * @param drop drop component. If drop component is null, will use absolute ( of the screen) coordinates
	 * @param x2 end x coordinate of the drop component. If drop component is null, will use absolute ( of the screen) coordinates
	 * @param y2 end y coordinate of the drop component. If drop component is null, will use absolute ( of the screen) coordinates
	 * @param moveCursor if true, mouse cursor will moved. Otherwise for drag and drop will used events
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						dragNdrop		(T drag, int x1, int y1, T drop, int x2, int y2, boolean moveCursor														) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#ComboBox}, {@link ControlKind#ListView}, {@link ControlKind#Tree} <br>
	 * Execute scroll operation
	 * @param component which will used for scroll operation
	 * @param index index of the item, to which will scrolled
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						scrollTo		(T component, int index																									) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * Execute a mouse operation for the cell with coordinates x and y from a table
	 * @param component table, which will used for mouse operation
	 * @param column index of column for operation
	 * @param row index of row for operation
	 * @param action kind of mouse action
	 * @return true, if operation was execute successful
	 *
	 * @see MouseAction
	 * @see ControlKind
	 */
	boolean 					mouseTable		(T component, int column, int row, MouseAction action																	) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * Insert the text to cell with coordinates x and y from a table
	 * @param component table, which will used for inserting a text
	 * @param column index of column for operation
	 * @param row index of row for operation
	 * @param text a text, which will insert to a component
	 * @return true, if operation was execute successful
	 *
	 * @see ControlKind
	 */
	boolean						textTableCell	(T component, int column, int row, String text																			) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * @param component table, which will used for getting value
	 * @param column index of column for operation
	 * @param row index of row for operation
	 * @return visible text of the cell
	 *
	 * @see ControlKind
	 */
	String						getValueTableCell(T component, int column, int row																						) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * @param component table, which will used for getting row
	 * @param additional locator, which will used for found rows
	 * @param header locator, which will used for found header
	 * @param useNumericHeader if this parameter is true, then will used numeric values to the header
	 * @param columns if this parameter is not null or empty, then this parameter will use instead of header
	 * @param valueCondition the value condition for found row
	 * @param colorCondition the color condition for found row
	 * @return map representation of the found row, where key - name of header, value - value of the cell
	 *
	 * @see ControlKind
	 * @see com.exactprosystems.jf.api.conditions.ColorCondition
	 * @see ICondition
	 */
	Map<String, String> 		getRow			(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * @param component table, which will used for searching row indexes
	 * @param additional locator, which will used for found rows
	 * @param header locator, which will used for found header
	 * @param useNumericHeader if this parameter is true, then will used numeric values to the header
	 * @param columns if this parameter is not null or empty, then this parameter will use instead of header
	 * @param valueCondition the value condition for found row
	 * @param colorCondition the color condition for found row
	 * @return list of rows, which are matches to the passed condition
	 *
	 * @see ControlKind
	 * @see com.exactprosystems.jf.api.conditions.ColorCondition
	 * @see ICondition
	 */
	List<String> 				getRowIndexes	(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * @param component table, which will used for getting row
	 * @param additional locator, which will used for found rows
	 * @param header locator, which will used for found header
	 * @param useNumericHeader if this parameter is true, then will used numeric values to the header
	 * @param columns if this parameter is not null or empty, then this parameter will use instead of header
	 * @param i index of the row
	 * @return map representation of the found row, where key - name of header, value - value of the cell
	 *
	 * @see ControlKind
	 */
	Map<String,String> 			getRowByIndex	(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i						) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * @param component table, which will used for getting row
	 * @param additional locator, which will used for found rows
	 * @param header locator, which will used for found header
	 * @param useNumericHeader if this parameter is true, then will used numeric values to the header
	 * @param columns if this parameter is not null or empty, then this parameter will use instead of header
	 * @param i index of the row
	 * @return map representation of the found row, where key - name of header, value - instance of the class {@link ValueAndColor}
	 *
	 * @see ControlKind
	 * @see ValueAndColor
	 */
	Map<String, ValueAndColor>	getRowWithColor	(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i						) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * @param component table, which will used for getting row
	 * @param additional locator, which will used for found rows
	 * @param header locator, which will used for found header
	 * @param useNumericHeader if this parameter is true, then will used numeric values to the header
	 * @param columns if this parameter is not null or empty, then this parameter will use instead of header
	 * @return double array of the table, where all elements are value of cells.
	 *
	 * @see ControlKind
	 */
	String[][]					getTable		(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns							) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * @param component table, which will used for getting row
	 * @param additional locator, which will used for found rows
	 * @param header locator, which will used for found header
	 * @param useNumericHeader if this parameter is true, then will used numeric values to the header
	 * @return size of a table ( count of rows in the table)
	 *
	 * @see ControlKind
	 */
	int							getTableSize	(T component, Locator additional, Locator header, boolean useNumericHeader												) throws Exception;
	/**
	 * Available kinds : {@link ControlKind#Table} <br>
	 * @param component table, which will used for getting row
	 * @param x column index
	 * @param y row index
	 * @return color of the cell, which found by passed parameters
	 *
	 * @see ControlKind
	 */
    Color                       getColorXY      (T component, int x, int y                                                                                              ) throws Exception;
	
}
