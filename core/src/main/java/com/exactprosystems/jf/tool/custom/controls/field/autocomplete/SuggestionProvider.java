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

import com.exactprosystems.jf.documents.matrix.parser.items.TempItem;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SuggestionProvider implements Callback<String, Collection<String>>
{
	private final List<String> possibleSuggestions     = new ArrayList<>();
	private final Object       possibleSuggestionsLock = new Object();

	public SuggestionProvider(Collection<String> collection)
	{
		synchronized (this.possibleSuggestionsLock)
		{
			this.possibleSuggestions.addAll(collection);
		}
	}

	@Override
	public final Collection<String> call(final String request)
	{
		List<String> suggestions = new ArrayList<>();
		if (!request.isEmpty())
		{
			synchronized (this.possibleSuggestionsLock)
			{
				suggestions = isMatch(this.possibleSuggestions, request);
			}

			String prefix = request.toLowerCase();

			suggestions.sort((s1, s2) ->
			{
				if (s1.toLowerCase().startsWith(prefix) && !s2.toLowerCase().startsWith(prefix))
				{
					return -1;
				}
				else if (!s1.toLowerCase().startsWith(prefix) && s2.toLowerCase().startsWith(prefix))
				{
					return 1;
				}
				else
				{
					return Integer.compare(s1.length(), s2.length());
				}
			});
		}
		return suggestions;
	}

	public static SuggestionProvider create(Collection<String> possibleSuggestions)
	{
		return new SuggestionProvider(possibleSuggestions);
	}

	public static List<String> isMatch(List<String> list, String request)
	{
		String userText = request.toLowerCase();
		String[] split = userText.split(" ", 2);
		String call = TempItem.CALL.toLowerCase();
		String subString = call.substring(0, call.length() - 1);
		return list.stream().filter(suggestionStr ->
		{
			suggestionStr = suggestionStr.toLowerCase();
			if (subString.startsWith(userText))
			{
				return suggestionStr.startsWith(subString);
			}
			if (userText.startsWith(subString) || call.indexOf(userText) == 0)
			{
				return suggestionStr.startsWith(subString) && suggestionStr.startsWith(call) && (split.length == 1 || suggestionStr.substring(call.length()).contains(split[1]));
			}
			else
			{
				return suggestionStr.contains(userText) && !suggestionStr.startsWith(call);
			}
		}).collect(Collectors.toList());
	}
}