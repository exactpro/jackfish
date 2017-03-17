package com.exactprosystems.jf.app;

public enum AttributeKind
{
	UID(true),
	CLASS(true),
	TEXT(true),
	NAME(true),

	ID(false),
	TYPE_NAME(false),
	ENABLED(false),
	VISIBLE(false),
	ITEMS(false);

	AttributeKind(boolean addToAttribute)
	{
		this.addToAttribute = addToAttribute;
	}

	private boolean addToAttribute;

	public boolean isAddToAttributes()
	{
		return addToAttribute;
	}

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
