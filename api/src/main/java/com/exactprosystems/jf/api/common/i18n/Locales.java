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

package com.exactprosystems.jf.api.common.i18n;
import java.util.*;

public enum Locales
{
    ENGLISH(Locale.ENGLISH),
    RUSSIAN(new Locale("ru"));

    private final Locale locale;

	Locales(Locale locale)
	{
		this.locale = locale;
	}

	public static void setDefault(String localeName)
	{
		Locale.setDefault(Locales.valueOf(localeName.toUpperCase()).locale);
	}

    @Override
    public String toString()
    {
        String name = this.locale.getDisplayLanguage();
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
        return name;
    }
}
