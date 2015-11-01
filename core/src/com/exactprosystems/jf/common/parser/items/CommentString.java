package com.exactprosystems.jf.common.parser.items;

import com.exactprosystems.jf.api.app.Mutable;

public class CommentString implements Mutable
{
	public CommentString()
	{
		this(null);
	}
	
	public CommentString(String string)
	{
		this.str = string;
	}

	@Override
	public String toString()
	{
		return this.str;
	}
	
	@Override
	public boolean isChanged()
	{
		return false;
	}

	@Override
	public void saved()
	{
	}

	private String str;
}
