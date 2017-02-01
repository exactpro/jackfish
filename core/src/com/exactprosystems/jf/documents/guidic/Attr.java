////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Attr 
{
	public static final String nameName			= "name";
	
	@XmlAttribute(name = nameName)
	protected String name;
	
	@XmlValue
	protected String value;
	
	public Attr() 
	{
	}

	public Attr(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName() 
	{
		return this.name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}


	public String getValue() 
	{
		return this.value;
	}

	public void setValue(String value) 
	{
		this.value = value;
	}
}
