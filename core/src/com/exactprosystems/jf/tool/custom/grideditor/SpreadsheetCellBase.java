////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.CssVariables;
import com.sun.javafx.event.EventHandlerManager;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;

import java.util.Objects;

public class SpreadsheetCellBase implements SpreadsheetCell, EventTarget
{

	private static final int EDITABLE_BIT_POSITION = 4;
	private static final int WRAP_BIT_POSITION = 5;
	private final StringCellType type;
	private final int row;
	private final int column;
	private final StringProperty text;
	private final StringProperty styleProperty;
	private int propertyContainer = 0;
	private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

	private ObservableSet<String> styleClass;

	public SpreadsheetCellBase(final int row, final int column, final StringCellType type)
	{
		this.row = row;
		this.column = column;
		this.type = type;
		text = new SimpleStringProperty(""); //$NON-NLS-1$
		setEditable(true);
		getStyleClass().add(CssVariables.SPREADSHEET_CELL); //$NON-NLS-1$
		styleProperty = new SimpleStringProperty();
	}

	private final ObjectProperty<Object> item = new SimpleObjectProperty<Object>(this, "item")
	{ //$NON-NLS-1$
		@Override
		protected void invalidated()
		{
			updateText();
		}
	};

	@Override
	public final void setItem(Object value)
	{
		if (isEditable())
			item.set(value);
	}

	@Override
	public final Object getItem()
	{
		return item.get();
	}

	@Override
	public final ObjectProperty<Object> itemProperty()
	{
		return item;
	}


	@Override
	public final boolean isEditable()
	{
		return isSet(EDITABLE_BIT_POSITION);
	}


	@Override
	public final void setEditable(boolean editable)
	{
		if (setMask(editable, EDITABLE_BIT_POSITION))
		{
			Event.fireEvent(this, new Event(EDITABLE_EVENT_TYPE));
		}
	}

	@Override
	public boolean isWrapText()
	{
		return isSet(WRAP_BIT_POSITION);
	}

	@Override
	public void setWrapText(boolean wrapText)
	{
		if (setMask(wrapText, WRAP_BIT_POSITION))
		{
			Event.fireEvent(this, new Event(WRAP_EVENT_TYPE));
		}
	}

	@Override
	public final ReadOnlyStringProperty textProperty()
	{
		return text;
	}

	@Override
	public final String getText()
	{
		return text.get();
	}

	@Override
	public final StringCellType getCellType()
	{
		return type;
	}

	@Override
	public final int getRow()
	{
		return row;
	}

	@Override
	public final int getColumn()
	{
		return column;
	}

	@Override
	public final ObservableSet<String> getStyleClass()
	{
		if (styleClass == null)
		{
			styleClass = FXCollections.observableSet();
		}
		return styleClass;
	}

	@Override
	public String getStyle()
	{
		return styleProperty.get();
	}

	@Override
	public StringProperty styleProperty()
	{
		return styleProperty;
	}

	@Override
	public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
	{
		return tail.append(eventHandlerManager);
	}

	@Override
	public String toString()
	{
		return String.format(R.SPREADSHEET_CELL_BASE_TO_STRING.get(), row, column);
	}


	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof SpreadsheetCell))
			return false;

		final SpreadsheetCell otherCell = (SpreadsheetCell) obj;
		return otherCell.getRow() == row && otherCell.getColumn() == column && Objects.equals(otherCell.getText(), getText());
	}


	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + row;
		result = prime * result + Objects.hashCode(getText());
		return result;
	}

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

	private boolean setMask(boolean flag, int position)
	{
		int oldCorner = propertyContainer;
		if (flag)
		{
			propertyContainer |= (1 << position);
		}
		else
		{
			propertyContainer &= ~(1 << position);
		}
		return propertyContainer != oldCorner;
	}

	private boolean isSet(int position)
	{
		return (propertyContainer & (1 << position)) != 0;
	}
}
