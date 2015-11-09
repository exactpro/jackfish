////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.sun.javafx.event.EventHandlerManager;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.*;
import javafx.scene.Node;

import java.util.Objects;

public class SpreadsheetCellBase implements SpreadsheetCell, EventTarget{

    //The Bit position for the editable Property.
    private static final int EDITABLE_BIT_POSITION = 4;
    private static final int WRAP_BIT_POSITION = 5;
    private final StringCellType type;
    private final int row;
    private final int column;
    private final StringProperty text;
    private final StringProperty styleProperty;
    private final ObjectProperty<Node> graphic;
    private int propertyContainer = 0;
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    private ObservableSet<String> styleClass;

    public SpreadsheetCellBase(final int row, final int column, final int rowSpan, final int columnSpan,final StringCellType type) {
        this.row = row;
        this.column = column;
        this.type = type;
        text = new SimpleStringProperty(""); //$NON-NLS-1$
        graphic = new SimpleObjectProperty<>();
        setEditable(true);
        getStyleClass().add("spreadsheet-cell"); //$NON-NLS-1$
        styleProperty = new SimpleStringProperty();
    }

   /***************************************************************************
     *
     * Public Methods
     *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override
    public boolean match(SpreadsheetCell cell) {
        return type.match(cell);
    }

    // --- item
    private final ObjectProperty<Object> item = new SimpleObjectProperty<Object>(this, "item") { //$NON-NLS-1$
        @Override
        protected void invalidated() {
            updateText();
        }
    };

   /** {@inheritDoc} */
    @Override
    public final void setItem(Object value) {
        if (isEditable())
            item.set(value);
    }

    /** {@inheritDoc} */
    @Override
    public final Object getItem() {
        return item.get();
    }

    /** {@inheritDoc} */
    @Override
    public final ObjectProperty<Object> itemProperty() {
        return item;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEditable() {
        return isSet(EDITABLE_BIT_POSITION);
    }

    /** {@inheritDoc} */
    @Override
    public final void setEditable(boolean editable) {
        if(setMask(editable, EDITABLE_BIT_POSITION)){
            Event.fireEvent(this, new Event(EDITABLE_EVENT_TYPE));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapText(){
        return isSet(WRAP_BIT_POSITION);
    }

    /** {@inheritDoc} */
    @Override
    public void setWrapText(boolean wrapText) {
        if (setMask(wrapText, WRAP_BIT_POSITION)) {
            Event.fireEvent(this, new Event(WRAP_EVENT_TYPE));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final ReadOnlyStringProperty textProperty() {
        return text;
    }

    /** {@inheritDoc} */
    @Override
    public final String getText() {
        return text.get();
    }

   /** {@inheritDoc} */
    @Override
    public final StringCellType getCellType() {
        return type;
    }

   /** {@inheritDoc} */
    @Override
    public final int getRow() {
        return row;
    }

    /** {@inheritDoc} */
    @Override
    public final int getColumn() {
        return column;
    }

    /** {@inheritDoc} */
    @Override
    public final ObservableSet<String> getStyleClass() {
        if (styleClass == null) {
            styleClass = FXCollections.observableSet();
        }
        return styleClass;
    }

    /** {@inheritDoc} */
    @Override
    public void setStyle(String style){
        styleProperty.set(style);
    }

    /** {@inheritDoc} */
    @Override
    public String getStyle(){
        return styleProperty.get();
    }

    /** {@inheritDoc} */
    @Override
    public StringProperty styleProperty(){
        return styleProperty;
    }

    /** {@inheritDoc} */
    @Override
    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    /** {@inheritDoc} */
    @Override
    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    /** {@inheritDoc} */
    @Override
    public Node getGraphic() {
        return graphic.get();
    }

    /** {@inheritDoc} */
    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.append(eventHandlerManager);
    }

    /***************************************************************************
     *
     * Overridden Methods
     *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "cell[" + row + "][" + column + "]" ;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SpreadsheetCell))
            return false;

        final SpreadsheetCell otherCell = (SpreadsheetCell) obj;
        return otherCell.getRow() == row && otherCell.getColumn() == column
                && Objects.equals(otherCell.getText(), getText());
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        result = prime * result + Objects.hashCode(getText());
        return result;
    }
    
    /**
     * Registers an event handler to this SpreadsheetCell. The SpreadsheetCell class allows 
     * registration of listeners which will be notified when a corner state of
     * the editable state of this SpreadsheetCell have changed.
     *
     * @param eventType the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    @Override
    public void addEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler) {
         eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    /**
     * Unregisters a previously registered event handler from this SpreadsheetCell. One
     * handler might have been registered for different event types, so the
     * caller needs to specify the particular event type from which to
     * unregister the handler.
     *
     * @param eventType the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    @Override
    public void removeEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler) {
         eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }
    
    /***************************************************************************
     * 
     * Private Implementation
     * 
     **************************************************************************/

    /**
     * Update the text for the SpreadsheetView.
     */
    @SuppressWarnings("unchecked")
    private void updateText()
	{
		if (getItem() == null)
		{
			text.setValue(""); //$NON-NLS-1$
		}
		else
		{
			text.setValue(type.toString(String.valueOf(getItem())));
		}
	}


    /**
     * Set the specified bit position at the value specified by flag.
     * @param flag
     * @param position
     * @return whether a change has really occured.
     */
    private boolean setMask(boolean flag, int position) {
        int oldCorner = propertyContainer;
        if (flag) {
            propertyContainer |= (1 << position);
        } else {
            propertyContainer &= ~(1 << position);
        }
        return propertyContainer != oldCorner;
    }

    private boolean isSet(int position) {
        return (propertyContainer & (1 << position)) != 0;
    }
}
