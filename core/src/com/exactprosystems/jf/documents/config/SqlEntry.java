////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class SqlEntry extends Entry
{
	@XmlElement(name = Configuration.sqlJar)
	protected String sqlJarNameValue;

	@XmlElement(name = Configuration.sqlConnection)
	protected String sqlConnectionStringValue;

	@Override
	protected String getDerived(String name)
	{
		switch (name)
		{
			case Configuration.sqlJar: 			return this.sqlJarNameValue;
			case Configuration.sqlConnection:	return this.sqlConnectionStringValue;
		}
		return null;
	}

	@Override
	protected void setDerived(String name, Object value)
	{
		switch (name)
		{
			case Configuration.sqlJar: 			this.sqlJarNameValue			= "" + value;	return;
			case Configuration.sqlConnection:	this.sqlConnectionStringValue	= "" + value;	return;
		}
	}
}