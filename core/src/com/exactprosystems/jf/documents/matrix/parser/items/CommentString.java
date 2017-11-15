////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
