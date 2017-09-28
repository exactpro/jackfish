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
