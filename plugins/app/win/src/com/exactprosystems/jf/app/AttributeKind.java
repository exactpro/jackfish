package com.exactprosystems.jf.app;

public enum AttributeKind
{
	ID,
	UID,
	CLASS,
	TEXT,
	NAME,
	TYPE_NAME;

	public static boolean isSupported(String name)
	{
		for (AttributeKind kind : AttributeKind.values())
		{
			if (kind.name().equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
}
