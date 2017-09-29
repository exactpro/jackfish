////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.field.autocomplete;

import com.exactprosystems.jf.documents.matrix.parser.items.TempItem;
import javafx.util.Callback;

import java.util.*;
import java.util.stream.Collectors;

public class SuggestionProvider implements Callback<String, Collection<String>>
{
    private final List<String> possibleSuggestions = new ArrayList<>();
    private final Object possibleSuggestionsLock = new Object();

    public SuggestionProvider(Collection<String> collection)
    {
        synchronized (possibleSuggestionsLock)
        {
            possibleSuggestions.addAll(collection);
        }
    }

    @Override
    public final Collection<String> call(final String request)
    {
        List<String> suggestions = new ArrayList<>();
        if (!request.isEmpty())
        {
            synchronized (possibleSuggestionsLock)
            {
                suggestions = isMatch(new ArrayList<>(possibleSuggestions), request);
            }

            Collections.sort(suggestions);
        }
        return suggestions;
    }

    public static SuggestionProvider create(Collection<String> possibleSuggestions)
    {
        return new SuggestionProvider(possibleSuggestions);
    }

    public static List<String> isMatch(List<String> list, String request)
    {
        return list.stream().filter(s ->
        {
            String userText = request.toLowerCase();
            String suggestionStr = s.toLowerCase();
            String[] split = userText.split(" ", 2);
            String call = TempItem.CALL.toLowerCase();

            if (userText.matches("^c|^ca|^cal|^|call"))
            {
                return suggestionStr.startsWith(call.substring(0,call.length()-1));
            }
            if (!userText.startsWith(call))
            {

                return suggestionStr.contains(userText) && !suggestionStr.startsWith(call);
            }
            else
            {
                return suggestionStr.startsWith(call) && (split.length == 1 || suggestionStr.substring(call.length()).contains(split[1]));
            }

        }).collect(Collectors.toList());
    }
}