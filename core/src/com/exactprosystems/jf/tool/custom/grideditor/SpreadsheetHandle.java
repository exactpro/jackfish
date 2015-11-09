////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.grideditor;

/**
 * Implementation vs public bridge.
 */
public abstract class SpreadsheetHandle {
	/** Access the main control. */
	protected abstract SpreadsheetView getView();
	/** Accesses the grid (ie cell table) in the spreadsheet. */
	protected abstract SpreadsheetGridView getGridView();
	/** Accesses the grid view (ie cell table view). */
	protected abstract GridViewSkin getCellsViewSkin();
	/** Whether that column width has been set by the user. */
	protected abstract boolean isColumnWidthSet(int indexColumn);
}
