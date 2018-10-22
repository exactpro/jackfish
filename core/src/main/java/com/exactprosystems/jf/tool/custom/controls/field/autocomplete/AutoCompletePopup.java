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
	private static final int               TITLE_HEIGHT        = 28;
	public static final  String            DEFAULT_STYLE_CLASS = "auto-complete-popup";
	private final        ObservableList<T> suggestions         = FXCollections.observableArrayList();
	private              IntegerProperty   visibleRowCount     = new SimpleIntegerProperty(this, "visibleRowCount", 10);
	private StringConverter<T> converter;
	private final EventHandlerManager                              eventHandlerManager = new EventHandlerManager(this);
	private       ObjectProperty<EventHandler<SuggestionEvent<T>>> onSuggestion        = new ObjectPropertyBase<EventHandler<SuggestionEvent<T>>>()
	{
		@Override
		protected void invalidated()
		{
			eventHandlerManager.setEventHandler(SuggestionEvent.SUGGESTION_EVENT_TYPE, (EventHandler<SuggestionEvent>) (Object) super.get());
		}

		@Override
		public Object getBean()
		{
			return AutoCompletePopup.this;
		}

		@Override
		public String getName()
		{
			return "onSuggestion";
		}
	};

	public static class SuggestionEvent<S> extends Event
	{
		public static final EventType<SuggestionEvent> SUGGESTION_EVENT_TYPE = new EventType<>("SUGGESTION_EVENT_TYPE");
		private final S suggestion;

		public SuggestionEvent(S suggestion)
		{
			super(SUGGESTION_EVENT_TYPE);
			this.suggestion = suggestion;
		}

		public S getSuggestion()
		{
			return suggestion;
		}
	}

	public AutoCompletePopup()
	{
		super.setAutoFix(true);
		super.setAutoHide(true);
		super.setHideOnEscape(true);

		super.getStyleClass().add(DEFAULT_STYLE_CLASS);
	}

	public ObservableList<T> getSuggestions()
	{
		return this.suggestions;
	}

	public void show(Node node)
	{
		if (node.getScene() == null || node.getScene().getWindow() == null)
		{
			return;
		}

		if (super.isShowing())
		{
			return;
		}

		Window parent = node.getScene().getWindow();
		this.show(parent, parent.getX() + node.localToScene(0, 0).getX() + node.getScene().getX(), parent.getY() + node.localToScene(0, 0).getY() + node.getScene().getY() + TITLE_HEIGHT);

	}

	public void setConverter(StringConverter<T> converter)
	{
		this.converter = converter;
	}

	public StringConverter<T> getConverter()
	{
		return this.converter;
	}

	public final IntegerProperty visibleRowCountProperty()
	{
		return this.visibleRowCount;
	}

	public final ObjectProperty<EventHandler<SuggestionEvent<T>>> onSuggestionProperty()
	{
		return this.onSuggestion;
	}

	public final void setOnSuggestion(EventHandler<SuggestionEvent<T>> value)
	{
		this.onSuggestion.set(value);
	}

	@Override
	public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
	{
		return super.buildEventDispatchChain(tail).append(this.eventHandlerManager);
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
		return new AutoCompletePopupSkin<>(this);
	}
}
