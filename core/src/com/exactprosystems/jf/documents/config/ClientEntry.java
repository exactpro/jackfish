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
public class ClientEntry extends Entry
{
	@XmlElement(name = Configuration.clientDescription)
	protected String descriptionValue;
	
	@XmlElement(name = Configuration.clientJar)
	protected String clientJarNameValue;

	@XmlElement(name = Configuration.clientLimit)
	protected int clientLimitValue;

	@XmlElement(name = Configuration.clientDictionary)
	protected String clientDictionaryValue;

	@Override
	protected String getDerived(String name) throws Exception
	{
		switch (name)
		{
			case Configuration.clientDescription:	return this.descriptionValue;
			case Configuration.clientJar:			return this.clientJarNameValue;
			case Configuration.clientLimit:			return "" + this.clientLimitValue;
			case Configuration.clientDictionary:	return this.clientDictionaryValue;
		}
		return null;
	}

	@Override
	protected void setDerived(String name, Object value) throws Exception
	{
		switch (name)
		{
			case Configuration.clientDescription:	this.descriptionValue		= "" + value;	return;
			case Configuration.clientJar:			this.clientJarNameValue		= "" + value;	return;
			case Configuration.clientLimit:			this.clientLimitValue		= Integer.parseInt("" +value);	return;
			case Configuration.clientDictionary:	this.clientDictionaryValue	= "" + value;	return;
		}
	}
}