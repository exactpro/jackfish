////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ScreenshotKind
{
    Never,
    OnStart,
    OnFinish,
    OnError,
    OnStartOrError,
    OnFinishOrError,
    ;

	public static ScreenshotKind valueByName(String name) throws Exception
	{
		if (Str.IsNullOrEmpty(name))
		{
			return Never;
		}

		return Arrays.stream(values())
				.filter(kind -> kind.name().equals(name))
				.findFirst()
				.orElseThrow(() -> new Exception(String.format(R.SCREENSHOTKIND_VALUE_BY_NAME_EXCEPTION.get(), name)));
	}

	public static List<String> names()
	{
		return Arrays.stream(values())
				.map(Enum::name)
				.collect(Collectors.toList());
	}
}
