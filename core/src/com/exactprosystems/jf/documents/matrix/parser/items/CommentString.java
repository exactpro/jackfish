package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.app.Mutable;

public class CommentString implements Mutable
{
	public CommentString()
	{
		this((String) null);
	}
	
	public CommentString(String string)
	{
		this.str = string;
	}

	/**
	 * copy constructor
	 */
	public CommentString(CommentString cs)
	{
		this.str = cs.str;
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
