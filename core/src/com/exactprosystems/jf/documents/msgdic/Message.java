////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.msgdic;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.exactprosystems.jf.api.client.IField;
import com.exactprosystems.jf.api.client.IMessage;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Message", propOrder = { "fields" })
public class Message extends Field implements IMessage
{
	public Message()
	{
	}
	
	@Override
	public String toString()
	{
		return Message.class.getSimpleName() + "{" + this.fields + "}";
	}

	@XmlElement(name = "field")
	protected List<Field>	fields;

	//----------------------------------------------------------------------------------------------------------------------
	// interface IMessage
	//----------------------------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Override
	public List<IField> getFields()
	{
		return this.fields == null ? null : (List<IField>)(List<?>) this.fields;
	}

	@Override
	public IField getField(String name)
	{
		for (Field field : this.fields)
		{
			String str = field.getName();
			if (str != null && str.equals(name))
			{
				return field;
			}
		}
			
		return null; 
	}
}
