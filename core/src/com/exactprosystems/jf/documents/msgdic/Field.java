////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.msgdic;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.exactprosystems.jf.api.client.IAttribute;
import com.exactprosystems.jf.api.client.IField;
import com.exactprosystems.jf.api.client.IType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Field", propOrder = { "description", "attributes", "values" })
@XmlSeeAlso({ Message.class })
public class Field implements IField
{
	public Field()
	{
		this.attributes = new ArrayList<Attribute>();
		this.values = new ArrayList<Attribute>();
	}
	
	@Override
	public String toString()
	{
		return Field.class.getSimpleName() + ":" + this.name + "{" + this.attributes + "}";
	}

	protected String			description;

	@XmlElement(name = "attribute")
	protected List<Attribute>	attributes;

	@XmlElement(name = "value")
	protected List<Attribute>	values;

	@XmlAttribute(name = "name")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NMTOKEN")
	protected String			name;

	@XmlAttribute(name = "reference")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Field		     reference;

	@XmlAttribute(name = "id")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String			id;

	@XmlAttribute(name = "type")
	protected JavaType			type;

	@XmlAttribute(name = "defaultvalue")
	protected String			defaultvalue;

	@XmlAttribute(name = "required")
	protected Boolean			required;

	@XmlAttribute(name = "isCollection")
	protected Boolean			isCollection;

	//----------------------------------------------------------------------------------------------------------------------
	// interface IField
	//----------------------------------------------------------------------------------------------------------------------
	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public IAttribute getAttribute(String name)
	{
		for (IAttribute attr : this.attributes)
		{
			String str = attr.getName();
			if (str != null && str.equals(name))
			{
				return attr;
			}
		}
			
		return null; 
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IAttribute> getAttributes()
	{
		return this.attributes == null ? null : (List<IAttribute>)(List<?>) this.attributes;
	}

	@Override
	public IAttribute getValue(String name)
	{
		for (IAttribute attr : this.values)
		{
			String str = attr.getName();
			if (str != null && str.equals(name))
			{
				return attr;
			}
		}
			
		return null; 
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IAttribute> getValues()
	{
		return this.values == null ? null : (List<IAttribute>)(List<?>) this.values;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public IField getReference()
	{
		return this.reference;
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public String getDefaultvalue()
	{
		return this.defaultvalue;
	}

	@Override
	public boolean isRequired()
	{
		return this.required != null && this.required.booleanValue();
	}

	@Override
	public boolean isCollection()
	{
		return this.isCollection != null && this.isCollection.booleanValue();
	}

	//----------------------------------------------------------------------------------------------------------------------
}
