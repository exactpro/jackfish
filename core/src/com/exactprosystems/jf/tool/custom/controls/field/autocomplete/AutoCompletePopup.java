////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.field.autocomplete;

import com.sun.javafx.event.EventHandlerManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class AutoCompletePopup<T> extends PopupControl
{
	private final static int TITLE_HEIGHT = 28;
	private final ObservableList<T> suggestions = FXCollections.observableArrayList();
	private StringConverter<T> converter;

	private IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 10);

	public static class SuggestionEvent<TE> extends Event
	{
		public static final EventType<SuggestionEvent> SUGGESTION = new EventType<>("SUGGESTION");

		private final TE suggestion;

		public SuggestionEvent(TE suggestion)
		{
			super(SUGGESTION);
			this.suggestion = suggestion;
		}

		public TE getSuggestion()
		{
			return suggestion;
		}
	}

	public AutoCompletePopup()
	{
		this.setAutoFix(true);
		this.setAutoHide(true);
		this.setHideOnEscape(true);

		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}

	public ObservableList<T> getSuggestions()
	{
		return suggestions;
	}

	public void show(Node node)
	{
		if (node.getScene() == null || node.getScene().getWindow() == null)
		{
			return;
		}

		if (isShowing())
		{
			return;
		}

		Window parent = node.getScene().getWindow();
		this.show(parent, parent.getX() + node.localToScene(0, 0).getX() +
				node.getScene().getX(), parent.getY() + node.localToScene(0, 0).getY() +
				node.getScene().getY() + TITLE_HEIGHT);

	}

	public void setConverter(StringConverter<T> converter)
	{
		this.converter = converter;
	}

	public StringConverter<T> getConverter()
	{
		return converter;
	}

	public final IntegerProperty visibleRowCountProperty()
	{
		return visibleRowCount;
	}

	private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

	public final ObjectProperty<EventHandler<SuggestionEvent<T>>> onSuggestionProperty()
	{
		return onSuggestion;
	}

	public final void setOnSuggestion(EventHandler<SuggestionEvent<T>> value)
	{
		onSuggestionProperty().set(value);
	}

	private ObjectProperty<EventHandler<SuggestionEvent<T>>> onSuggestion = new ObjectPropertyBase<EventHandler<SuggestionEvent<T>>>()
	{
		@Override
		protected void invalidated()
		{
			eventHandlerManager.setEventHandler(SuggestionEvent.SUGGESTION, (EventHandler<SuggestionEvent>) (Object) get());
		}

		@Override
		public Object getBean()
		{
			return AutoCompletePopup.this;
		}

		@Override
		public String getName()
		{
			return "onSuggestion"; //$NON-NLS-1$
		}
	};

	@Override
	public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
	{
		return super.buildEventDispatchChain(tail).append(eventHandlerManager);
	}


	public static final String DEFAULT_STYLE_CLASS = "auto-complete-popup"; //$NON-NLS-1$

	@Override
	protected Skin<?> createDefaultSkin()
	{
		return new AutoCompletePopupSkin<>(this);
	}

}
