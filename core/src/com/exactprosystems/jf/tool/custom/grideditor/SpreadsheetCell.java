////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;

//TODO remove this interface
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

	void setStyle(String style);

	String getStyle();

	StringProperty styleProperty();

	ReadOnlyStringProperty textProperty();

	String getText();

	StringCellType getCellType();

	int getRow();

	int getColumn();

	ObservableSet<String> getStyleClass();

	ObjectProperty<Node> graphicProperty();

	void setGraphic(Node graphic);

	Node getGraphic();

	void addEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler);

	void removeEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler);
}
