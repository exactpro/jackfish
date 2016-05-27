////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
