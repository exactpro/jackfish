/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.msgdic;

import com.exactprosystems.jf.api.client.IAttribute;
import com.exactprosystems.jf.api.client.IType;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Attribute", propOrder = {"value"})
public class Attribute implements IAttribute
{
	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NMTOKEN")
	protected String name;

	@XmlValue
	protected String value;

	@XmlAttribute(name = "type")
	protected JavaType type;

	public Attribute()
	{
	}

	@Override
	public String toString()
	{
		return this.name + "=" + this.value;
	}

	//region interface IAttribute
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

	//endregion

}
