////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.service;

public enum ServicePossibility
{
	Receiving			("Receivingmessages"),
	Sending				("Sending messages")
	
	;
	
	ServicePossibility(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	private String description;
}
