////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
