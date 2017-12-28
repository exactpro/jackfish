////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.field.autocomplete;

import com.exactprosystems.jf.tool.Common;
import com.sun.javafx.event.EventHandlerManager;
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
	private static final long   AUTO_COMPLETE_DELAY = 1;
	private final        Object suggestionsTaskLock = new Object();
	private final Node                 completionTarget;
	private final AutoCompletePopup<T> autoCompletionPopup;

	protected Callback<String, Collection<T>>                      suggestionProvider;
	private   ObjectProperty<EventHandler<AutoCompletionEvent<T>>> onAutoCompleted;
	private       FetchSuggestionsTask suggestionsTask     = null;
	private       boolean              ignoreInputChanges  = false;
	private final EventHandlerManager  eventHandlerManager = new EventHandlerManager(this);

	private class FetchSuggestionsTask extends Task<Void>
	{
		private final String userText;

		private FetchSuggestionsTask(String userText)
		{
			this.userText = userText;
		}

		@Override
		protected Void call() throws Exception
		{
			Callback<String, Collection<T>> provider = suggestionProvider;
			if (provider != null)
			{
				long startTime = System.currentTimeMillis();
				final Collection<T> fetchedSuggestions = provider.call(this.userText);
				long sleepTime = startTime + AUTO_COMPLETE_DELAY - System.currentTimeMillis();
				if (sleepTime > 0 && !super.isCancelled())
				{
					Thread.sleep(sleepTime);
				}
				if (!super.isCancelled())
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
	}

	public static class AutoCompletionEvent<S> extends Event
	{
		static final EventType<AutoCompletionEvent> AUTO_COMPLETED = new EventType<>("AUTO_COMPLETED");
		private final S completion;

		AutoCompletionEvent(S completion)
		{
			super(AUTO_COMPLETED);
			this.completion = completion;
		}

		public S getCompletion()
		{
			return completion;
		}
	}

	AutoCompletionBinding(Node completionTarget, Callback<String, Collection<T>> suggestionProvider, StringConverter<T> converter)
	{
		this.completionTarget = completionTarget;
		this.suggestionProvider = suggestionProvider;
		this.autoCompletionPopup = new AutoCompletePopup<>();
		this.autoCompletionPopup.setConverter(converter);
		this.autoCompletionPopup.setOnSuggestion(sce -> {
			try
			{
				this.ignoreInputChanges = true;
				this.completeUserInput(sce.getSuggestion());
				this.fireAutoCompletion(sce.getSuggestion());
				this.hidePopup();
			}
			finally
			{
				this.ignoreInputChanges = false;
			}
		});
	}

	public Node getCompletionTarget()
	{
		return this.completionTarget;
	}

	public final void setOnAutoCompleted(EventHandler<AutoCompletionEvent<T>> value)
	{
		this.onAutoCompletedProperty().set(value);
	}

	public final EventHandler<AutoCompletionEvent<T>> getOnAutoCompleted()
	{
		return this.onAutoCompleted == null ? null : this.onAutoCompleted.get();
	}

	final void setUserInput(String userText)
	{
		if (!this.ignoreInputChanges)
		{
			this.onUserInputChanged(userText);
		}
	}

	void hidePopup()
	{
		this.autoCompletionPopup.hide();
	}

	@Override
	public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
	{
		return tail.prepend(eventHandlerManager);
	}

	//region abstract methods
	public abstract void dispose();

	protected abstract void completeUserInput(T completion);
	//endregion

	//region private methods
	private void fireAutoCompletion(T completion)
	{
		Event.fireEvent(this, new AutoCompletionEvent<>(completion));
	}

	private void showPopup()
	{
		this.autoCompletionPopup.show(this.completionTarget);
		this.selectFirstSuggestion(this.autoCompletionPopup);
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

	private void onUserInputChanged(final String userText)
	{
		synchronized (this.suggestionsTaskLock)
		{
			if (this.suggestionsTask != null && this.suggestionsTask.isRunning())
			{
				this.suggestionsTask.cancel();
			}
			// create a new fetcher task
			this.suggestionsTask = new FetchSuggestionsTask(userText);
			Thread thread = new Thread(suggestionsTask);
			thread.setName("Show popup autocomplete textField, thread id : " + thread.getId());
			thread.start();
		}
	}

	private ObjectProperty<EventHandler<AutoCompletionEvent<T>>> onAutoCompletedProperty()
	{
		if (this.onAutoCompleted == null)
		{
			this.onAutoCompleted = new ObjectPropertyBase<EventHandler<AutoCompletionEvent<T>>>()
			{
				@Override
				protected void invalidated()
				{
					eventHandlerManager.setEventHandler(AutoCompletionEvent.AUTO_COMPLETED, super.get());
				}

				@Override
				public Object getBean()
				{
					return AutoCompletionBinding.this;
				}

				@Override
				public String getName()
				{
					return "onAutoCompleted";
				}
			};
		}
		return this.onAutoCompleted;
	}
	//endregion

}
