////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

public enum AttributeKind
{
	ID(false),
	UID(true),
	CLASS(true),
	TEXT(true),
	NAME(true),

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
