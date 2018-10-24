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
