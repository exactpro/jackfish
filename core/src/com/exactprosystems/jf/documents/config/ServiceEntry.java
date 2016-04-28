////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ServiceEntry extends Entry
{
	@XmlElement(name = Configuration.serviceDescription)
	protected String descriptionValue;
	
	@XmlElement(name = Configuration.serviceJar)
	protected String serviceJarNameValue;

	@Override
	public String get(String name) throws Exception
	{
		switch (name)
		{
			case Configuration.serviceDescription: 	return this.descriptionValue;
			case Configuration.serviceJar:			return this.serviceJarNameValue;
		}
		return null;
	}

	@Override
	public void set(String name, Object value) throws Exception
	{
		switch (name)
		{
			case Configuration.serviceDescription: 	this.descriptionValue		= "" + value;	return;
			case Configuration.serviceJar:			this.serviceJarNameValue	= "" + value;	return;
		}
	}
}