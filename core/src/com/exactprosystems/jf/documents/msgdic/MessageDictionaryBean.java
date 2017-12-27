////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.msgdic;

import com.exactprosystems.jf.api.app.Mutable;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

//TODO think about this MutableInterface in subclasses

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageDictionary", propOrder = {"description", "fields", "messages"})
@XmlRootElement(name = "dictionary")
public class MessageDictionaryBean implements Mutable
{
	public static final Class<?>[] jaxbContextClasses  = 
		{ 
				MessageDictionaryBean.class, 
				Messages.class, 
				Message.class, 
				Fields.class, 
				Field.class, 
				Attribute.class 
		};

	@XmlElement
	protected String description;

	@XmlElement(required = true)
	protected Fields fields;

	@XmlElement(required = true)
	protected Messages messages;

	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NMTOKEN")
	protected String name;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {"fields"})
	public static class Fields implements Mutable
	{
		@XmlElement(name = "field")
		protected List<Field> fields;

		public Fields()
		{
			this.fields = new ArrayList<>();
		}

		@Override
		public boolean isChanged()
		{
			return false;
		}

		@Override
		public void saved()
		{
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {"messages"})
	public static class Messages implements Mutable
	{
		@XmlElement(name = "message")
		protected List<Message> messages;

		public Messages()
		{
			this.messages = new ArrayList<>();
		}

		@Override
		public boolean isChanged()
		{
			return false;
		}

		@Override
		public void saved()
		{
		}
	}

	public MessageDictionaryBean()
	{
		this.fields = new Fields();
		this.messages = new Messages();
	}

	@Override
	public boolean isChanged()
	{
		return this.fields.isChanged() || this.messages.isChanged();
	}

	@Override
	public void saved()
	{
		this.fields.saved();
		this.messages.saved();
	}

}
