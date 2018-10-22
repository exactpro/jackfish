/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;
import javafx.event.EventType;

public interface SpreadsheetCell
{
	EventType EDITABLE_EVENT_TYPE = new EventType("EditableEventType"); //$NON-NLS-1$

	EventType WRAP_EVENT_TYPE = new EventType("WrapTextEventType"); //$NON-NLS-1$

	void setItem(Object value);

	Object getItem();

	ObjectProperty<Object> itemProperty();

	boolean isEditable();

	void setEditable(boolean editable);

	boolean isWrapText();

	void setWrapText(boolean wrapText);

	String getStyle();

	StringProperty styleProperty();

	ReadOnlyStringProperty textProperty();

	String getText();

	StringCellType getCellType();

	int getRow();

	int getColumn();

	ObservableSet<String> getStyleClass();
}
