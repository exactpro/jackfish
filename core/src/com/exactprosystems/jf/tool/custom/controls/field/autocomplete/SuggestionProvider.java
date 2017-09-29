////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.field.autocomplete;

import javafx.util.Callback;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SuggestionProvider<T> implements Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>>
{
	private final List<T> possibleSuggestions = new ArrayList<>();
	private final Object possibleSuggestionsLock = new Object();

	public void addPossibleSuggestions(Collection<T> newPossible)
	{
		synchronized (possibleSuggestionsLock)
		{
			possibleSuggestions.addAll(newPossible);
		}
	}

	@Override
	public final Collection<T> call(final AutoCompletionBinding.ISuggestionRequest request)
	{
		List<T> suggestions = new ArrayList<>();
		if (!request.getUserText().isEmpty())
		{
			synchronized (possibleSuggestionsLock)
			{
				suggestions.addAll(possibleSuggestions.stream()
						.filter(possibleSuggestion -> isMatch(possibleSuggestion, request))
						.collect(Collectors.toList())
				);
			}
			Collections.sort(suggestions, getComparator());
		}
		return suggestions;
	}

	protected abstract Comparator<T> getComparator();

	protected abstract boolean isMatch(T suggestion, AutoCompletionBinding.ISuggestionRequest request);

	public static <T> SuggestionProvider<T> create(Collection<T> possibleSuggestions)
	{
		return create(null, possibleSuggestions);
	}

	public static <T> SuggestionProvider<T> create(Callback<T, String> stringConverter, Collection<T> possibleSuggestions)
	{
		SuggestionProviderString<T> suggestionProvider = new SuggestionProviderString<>(stringConverter);
		suggestionProvider.addPossibleSuggestions(possibleSuggestions);
		return suggestionProvider;
	}

	private static class SuggestionProviderString<T> extends SuggestionProvider<T>
	{
		private Callback<T, String> stringConverter;

		private final Comparator<T> stringComparator = new Comparator<T>()
		{
			@Override
			public int compare(T o1, T o2)
			{
				String o1str = stringConverter.call(o1);
				String o2str = stringConverter.call(o2);
				return o1str.compareTo(o2str);
			}
		};

		public SuggestionProviderString(Callback<T, String> stringConverter)
		{
			this.stringConverter = stringConverter;
			if (this.stringConverter == null)
			{
				this.stringConverter = obj -> obj != null ? obj.toString() : "";
			}
		}

		protected Comparator<T> getComparator()
		{
			return stringComparator;
		}

		@Override
		protected boolean isMatch(T suggestion, AutoCompletionBinding.ISuggestionRequest request)
		{
			String userText = request.getUserText();
			String suggestionStr = String.valueOf(suggestion).toLowerCase();
			String[] split = userText.split("\\s|\\.");

			return Arrays.stream(split).map(String::toLowerCase).allMatch(suggestionStr::contains);
		}
	}
}
