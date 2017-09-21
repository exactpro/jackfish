////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.field.autocomplete;

import com.exactprosystems.jf.tool.Common;
import com.sun.javafx.event.EventHandlerManager;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;

public abstract class AutoCompletionBinding<T> implements EventTarget
{
	private static final long AUTO_COMPLETE_DELAY = 1;
	private final Node completionTarget;
	private final AutoCompletePopup<T> autoCompletionPopup;
	private final Object suggestionsTaskLock = new Object();

	private FetchSuggestionsTask suggestionsTask = null;
	private Callback<ISuggestionRequest, Collection<T>> suggestionProvider = null;
	private boolean ignoreInputChanges = false;

	protected AutoCompletionBinding(Node completionTarget, Callback<ISuggestionRequest, Collection<T>> suggestionProvider, StringConverter<T> converter)
	{
		this.completionTarget = completionTarget;
		this.suggestionProvider = suggestionProvider;
		this.autoCompletionPopup = new AutoCompletePopup<>();
		this.autoCompletionPopup.setConverter(converter);

		autoCompletionPopup.setOnSuggestion(sce -> {
			try
			{
				setIgnoreInputChanges(true);
				completeUserInput(sce.getSuggestion());
				fireAutoCompletion(sce.getSuggestion());
				hidePopup();
			}
			finally
			{
				setIgnoreInputChanges(false);
			}
		});
	}

	public final void setUserInput(String userText)
	{
		if (!isIgnoreInputChanges())
		{
			onUserInputChanged(userText);
		}
	}

	public Node getCompletionTarget()
	{
		return completionTarget;
	}

	public abstract void dispose();

	protected abstract void completeUserInput(T completion);


	protected void showPopup()
	{
		autoCompletionPopup.show(completionTarget);
		selectFirstSuggestion(autoCompletionPopup);
	}

	protected void hidePopup()
	{
		autoCompletionPopup.hide();
	}

	protected void fireAutoCompletion(T completion)
	{
		Event.fireEvent(this, new AutoCompletionEvent<>(completion));
	}

	private void selectFirstSuggestion(AutoCompletePopup<?> autoCompletionPopup)
	{
		Skin<?> skin = autoCompletionPopup.getSkin();
		if (skin instanceof AutoCompletePopupSkin)
		{
			AutoCompletePopupSkin<?> au = (AutoCompletePopupSkin<?>) skin;
			ListView<?> li = (ListView<?>) au.getNode();
			if (li.getItems() != null && !li.getItems().isEmpty())
			{
				li.getSelectionModel().select(0);
			}
		}
	}

	private final void onUserInputChanged(final String userText)
	{
		synchronized (suggestionsTaskLock)
		{
			if (suggestionsTask != null && suggestionsTask.isRunning())
			{
				suggestionsTask.cancel();
			}
			// create a new fetcher task
			suggestionsTask = new FetchSuggestionsTask(userText);
			Thread thread = new Thread(suggestionsTask);
			thread.setName("Show popup autocomplete textField, thread id : " + thread.getId());
			thread.start();
		}
	}

	private boolean isIgnoreInputChanges()
	{
		return ignoreInputChanges;
	}

	private void setIgnoreInputChanges(boolean state)
	{
		ignoreInputChanges = state;
	}

	public static interface ISuggestionRequest
	{
		boolean isCancelled();
		String getUserText();
	}

	private class FetchSuggestionsTask extends Task<Void> implements ISuggestionRequest
	{
		private final String userText;

		public FetchSuggestionsTask(String userText)
		{
			this.userText = userText;
		}

		@Override
		protected Void call() throws Exception
		{
			Callback<ISuggestionRequest, Collection<T>> provider = suggestionProvider;
			if (provider != null)
			{
				long start_time = System.currentTimeMillis();
				final Collection<T> fetchedSuggestions = provider.call(this);
				long sleep_time = start_time + AUTO_COMPLETE_DELAY - System.currentTimeMillis();
				if (sleep_time > 0 && !isCancelled())
				{
					Thread.sleep(sleep_time);
				}
				if (!isCancelled())
				{
					Common.runLater(() -> {
						if (fetchedSuggestions != null && !fetchedSuggestions.isEmpty())
						{
							autoCompletionPopup.getSuggestions().setAll(fetchedSuggestions);
							showPopup();
						}
						else
						{
							hidePopup();
						}
					});
				}
			}
			else
			{
				hidePopup();
			}
			return null;
		}

		@Override
		public String getUserText()
		{
			return userText;
		}
	}

	@SuppressWarnings("serial")
	public static class AutoCompletionEvent<TE> extends Event
	{
		@SuppressWarnings("rawtypes")
		public static final EventType<AutoCompletionEvent> AUTO_COMPLETED = new EventType<>("AUTO_COMPLETED"); //$NON-NLS-1$

		private final TE completion;

		public AutoCompletionEvent(TE completion)
		{
			super(AUTO_COMPLETED);
			this.completion = completion;
		}

		public TE getCompletion()
		{
			return completion;
		}
	}


	private ObjectProperty<EventHandler<AutoCompletionEvent<T>>> onAutoCompleted;

	public final void setOnAutoCompleted(EventHandler<AutoCompletionEvent<T>> value)
	{
		onAutoCompletedProperty().set(value);
	}

	public final EventHandler<AutoCompletionEvent<T>> getOnAutoCompleted()
	{
		return onAutoCompleted == null ? null : onAutoCompleted.get();
	}

	public final ObjectProperty<EventHandler<AutoCompletionEvent<T>>> onAutoCompletedProperty()
	{
		if (onAutoCompleted == null)
		{
			onAutoCompleted = new ObjectPropertyBase<EventHandler<AutoCompletionEvent<T>>>()
			{
				@SuppressWarnings({"rawtypes", "unchecked"})
				@Override
				protected void invalidated()
				{
					eventHandlerManager.setEventHandler(AutoCompletionEvent.AUTO_COMPLETED, (EventHandler<AutoCompletionEvent>) (Object) get());
				}

				@Override
				public Object getBean()
				{
					return AutoCompletionBinding.this;
				}

				@Override
				public String getName()
				{
					return "onAutoCompleted"; //$NON-NLS-1$
				}
			};
		}
		return onAutoCompleted;
	}

	final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

	public <E extends Event> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler)
	{
		eventHandlerManager.addEventHandler(eventType, eventHandler);
	}
	public <E extends Event> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler)
	{
		eventHandlerManager.removeEventHandler(eventType, eventHandler);
	}

	@Override
	public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
	{
		return tail.prepend(eventHandlerManager);
	}


}
