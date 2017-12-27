////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.msgdic;

import com.exactprosystems.jf.api.client.IAttribute;
import com.exactprosystems.jf.api.client.IField;
import com.exactprosystems.jf.api.client.IType;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Field", propOrder = {"description", "attributes", "values"})
@XmlSeeAlso({Message.class})
public class Field implements IField
{
	protected String description;

	@XmlElement(name = "attribute")
	protected List<Attribute> attributes;

	@XmlElement(name = "value")
	protected List<Attribute> values;

	@XmlAttribute(name = "name")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NMTOKEN")
	protected String name;

	@XmlAttribute(name = "reference")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Field reference;

	@XmlAttribute(name = "id")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String id;

	@XmlAttribute(name = "type")
	protected JavaType type;

	@XmlAttribute(name = "defaultvalue")
	protected String defaultvalue;

	@XmlAttribute(name = "required")
	protected Boolean required;

	@XmlAttribute(name = "isCollection")
	protected Boolean isCollection;

	public Field()
	{
		this.attributes = new ArrayList<>();
		this.values = new ArrayList<>();
	}

	@Override
	public String toString()
	{
		return Field.class.getSimpleName() + ":" + this.name + "{" + this.attributes + "}";
	}

	//region interface IField
	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public IAttribute getAttribute(String name)
	{
		return this.attributes.stream()
				.filter(attr -> attr.getName() != null && attr.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<IAttribute> getAttributes()
	{
		return this.attributes == null ? null : (List<IAttribute>) (List<?>) this.attributes;
	}

	@Override
	public IAttribute getValue(String name)
	{
		return this.values.stream()
				.filter(attr -> attr.getName() != null && attr.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<IAttribute> getValues()
	{
		return this.values == null ? null : (List<IAttribute>) (List<?>) this.values;
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
		return this.required != null && this.required;
	}

	@Override
	public boolean isCollection()
	{
		return this.isCollection != null && this.isCollection;
	}

	//endregion
}
