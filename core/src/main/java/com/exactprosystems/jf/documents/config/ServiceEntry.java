/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A xml bean for any ServiceEntry from a configuration
 *
 * @see Configuration
 * @see Configuration#getServiceEntries()
 * @see Entry
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceEntry extends Entry
{
	@XmlElement(name = Configuration.serviceDescription)
	protected String descriptionValue;
	
	@XmlElement(name = Configuration.serviceJar)
	protected String serviceJarNameValue;

	@Override
	protected String getDerived(String name)
	{
		switch (name)
		{
			case Configuration.serviceDescription: 	return this.descriptionValue;
			case Configuration.serviceJar:			return this.serviceJarNameValue;
		}
		return null;
	}

	@Override
	protected void setDerived(String name, Object value)
	{
		switch (name)
		{
			case Configuration.serviceDescription: 	this.descriptionValue		= "" + value;	return;
			case Configuration.serviceJar:			this.serviceJarNameValue	= "" + value;	return;
		}
	}
}