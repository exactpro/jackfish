////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

public enum Possibility
{
	Receiving			("Receivingmessages"),
	Sending				("Sending messages"),
	RawSending			("Sending raw messages"),
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
