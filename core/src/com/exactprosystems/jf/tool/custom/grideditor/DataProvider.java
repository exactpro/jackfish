////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.ObservableList;

public interface DataProvider<T>
{
    int rowCount();
    
    int columnCount();
	
    T getCellValue(int column, int row);

	T defaultValue();

	void setCellValue(int column, int row, T value);
    
    String columnName(int column);
    
    void setColumnName(int column, String name);
    
    void addColumn(int column, String name);
    
    void removeColumn(int column);
    
    void addRow(int row);

    void removeRow(int row);

	ObservableList<ObservableList<SpreadsheetCell>> getRows();

	ObservableList<String> getRowHeaders();

	ObservableList<String> getColumnHeaders();
}
