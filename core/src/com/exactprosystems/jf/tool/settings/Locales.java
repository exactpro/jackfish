package com.exactprosystems.jf.tool.settings;
import java.util.*;

public enum Locales
{
    ENGLISH(Locale.ENGLISH),
    RUSSIAN(new Locale("ru"));

    private final Locale locale;

    public Locale getLocale()
    {
        return locale;
    }

    Locales(Locale locale)
    {
        this.locale = locale;
    }

    @Override
    public String toString()
    {
        String name = this.locale.getDisplayLanguage();
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
        return name;
    }
}
