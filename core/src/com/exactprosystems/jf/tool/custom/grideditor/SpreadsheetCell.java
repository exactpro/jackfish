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

/**
 *
 * Interface of the cells used in the {@link SpreadsheetView}.
 * 
 * See {@link SpreadsheetCellBase} for a complete and detailed documentation.
 * @see SpreadsheetCellBase
 */
public interface SpreadsheetCell {
    /**
     * This EventType can be used with an {@link EventHandler} in order to catch
     * when the editable state of a SpreadsheetCell is changed.
     */
    public static final EventType EDITABLE_EVENT_TYPE = new EventType("EditableEventType"); //$NON-NLS-1$
    
    /**
     * This EventType can be used with an {@link EventHandler} in order to catch
     * when the wrap text state of a SpreadsheetCell is changed.
     */
    public static final EventType WRAP_EVENT_TYPE = new EventType("WrapTextEventType"); //$NON-NLS-1$
    
    /**
     * Sets the value of the property Item. This should be used only at
     * initialization. Prefer {@link Grid#setCellValue(int, int, Object)} after
     * because it will compute correctly the modifiedCell. If
     * {@link #isEditable()} return false, nothing is done.
     *
     * @param value
     */
    public void setItem(Object value);

    /**
     * Return the value contained in the cell.
     *
     * @return the value contained in the cell.
     */
    public Object getItem();

    /**
     * The item property represents the currently-set value inside this
     * SpreadsheetCell instance.
     *
     * @return the item property which contains the value.
     */
    public ObjectProperty<Object> itemProperty();

    /**
     * Return if this cell can be edited or not.
     *
     * @return true if this cell is editable.
     */
    public boolean isEditable();

    /**
     * Change the editable state of this cell
     *
     * @param editable
     */
    public void setEditable(boolean editable);
    
    /**
     * If a run of text exceeds the width of the Labeled, then this variable
     * indicates whether the text should wrap onto another line.
     *
     * @return the value of wrapText property.
     */
    public boolean isWrapText();

    /**
     * If a run of text exceeds the width of the Labeled, then this variable
     * indicates whether the text should wrap onto another line.
     * @param wrapText
     */
    public void setWrapText(boolean wrapText);

    /**
     * A string representation of the CSS style associated with this specific
     * Node. This is analogous to the "style" attribute of an HTML element. Note
     * that, like the HTML style attribute, this variable contains style
     * properties and values and not the selector portion of a style rule.
     *
     * @param style
     */
    public void setStyle(String style);
    
    /**
     * A string representation of the CSS style associated with this specific
     * Node. This is analogous to the "style" attribute of an HTML element. Note
     * that, like the HTML style attribute, this variable contains style
     * properties and values and not the selector portion of a style rule.
     *
     * @return The inline CSS style associated with this Node. If this Node does
     * not have an inline style, an empty String is returned.
     */
    public String getStyle();
    
    /**
     * A string representation of the CSS style associated with this specific
     * Node. This is analogous to the "style" attribute of an HTML element. Note
     * that, like the HTML style attribute, this variable contains style
     * properties and values and not the selector portion of a style rule.
     *
     * @return a string representation of the CSS style
     */
    public StringProperty styleProperty();
    
    /**
     * Return the StringProperty of the representation of the value.
     *
     * @return the StringProperty of the representation of the value.
     */
    public ReadOnlyStringProperty textProperty();

    /**
     * Return the String representation currently used for display in the
     * {@link SpreadsheetView}.
     *
     * @return text representation of the value.
     */
    public String getText();

    public StringCellType getCellType();

    /**
     * Return the row of this cell.
     *
     * @return the row of this cell.
     */
    public int getRow();

    /**
     * Return the column of this cell.
     *
     * @return the column of this cell.
     */
    public int getColumn();

    /**
     * Return an ObservableList of String of all the style class associated with
     * this cell. You can easily modify its appearance by adding a style class
     * (previously set in CSS).
     *
     * @return an ObservableList of String of all the style class
     */
    public ObservableSet<String> getStyleClass();

    /**
     * @return an ObjectProperty wrapping a Node for the graphic.
     */
    public ObjectProperty<Node> graphicProperty();

    /**
     * Set a graphic for this cell to display aside with the text.
     *
     * @param graphic
     */
    public void setGraphic(Node graphic);

    /**
     * Return the graphic node associated with this cell. Return null if nothing
     * has been associated.
     *
     * @return the graphic node associated with this cell.
     */
    public Node getGraphic();
    
    /**
     * Registers an event handler to this SpreadsheetCell. 
     * @param eventType the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    public void addEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler);
    
    /**
     * Unregisters a previously registered event handler from this SpreadsheetCell. 
     * @param eventType the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    public void removeEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler);
}
