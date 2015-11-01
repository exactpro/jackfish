////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

public enum Possibility
{
	Receiving			("Receivingmessages"),
	Sending				("Sending messages"),
	Encoding			("Encoding to raw messages"),
	Decoding 			("Decoding from raw messages"),
	
	;
	
	Possibility(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	private String description;
}
