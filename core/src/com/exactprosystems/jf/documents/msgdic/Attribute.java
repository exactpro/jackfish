////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.msgdic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.exactprosystems.jf.api.client.IAttribute;
import com.exactprosystems.jf.api.client.IType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Attribute", propOrder = { "value" })
public class Attribute implements IAttribute
{
	public Attribute()
	{
	}
	
	@Override
	public String toString()
	{
		return this.name + "=" + this.value;
	}

	@XmlValue
	protected String	value;

	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NMTOKEN")
	protected String	name;
	
	@XmlAttribute(name = "type")
	protected JavaType	type;

	//----------------------------------------------------------------------------------------------------------------------
	// interface IAttribute
	//----------------------------------------------------------------------------------------------------------------------
	@Override
	public String getValue()
	{
		return this.value;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

}
