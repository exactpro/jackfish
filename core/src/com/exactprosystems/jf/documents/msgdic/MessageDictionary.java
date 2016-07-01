////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.msgdic;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.client.IAttribute;
import com.exactprosystems.jf.api.client.IField;
import com.exactprosystems.jf.api.client.IMessage;
import com.exactprosystems.jf.api.client.IMessageDictionary;
import com.exactprosystems.jf.common.xml.schema.Xsd;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageDictionary", propOrder = { "description", "fields", "messages" })
@XmlRootElement(name = "dictionary")

@DocumentInfo(
		newName = "NewDictionary", 
		extentioin = "xml", 
		description = "Message dictionary"
)
public class MessageDictionary extends AbstractDocument implements IMessageDictionary
{
	public MessageDictionary()
	{
		this(null, null);
	}

	public MessageDictionary(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);

		this.fields = new Fields();
		this.messages = new Messages();
		this.changed = false;
	}

	protected String	description;

	protected Fields	fields;

	protected Messages	messages;

	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NMTOKEN")
	protected String	name;

	@XmlAttribute(name = "version")
	protected String	version;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "fields" })
	public static class Fields implements Mutable
	{

		public Fields()
		{
			this.fields = new ArrayList<Field>();
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

		@XmlElement(name = "field")
		protected List<Field>	fields;

	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "messages" })
	public static class Messages implements Mutable
	{

		public Messages()
		{
			this.messages = new ArrayList<Message>();
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

		@XmlElement(name = "message")
		protected List<Message>	messages;
	}

	//----------------------------------------------------------------------------------------------------------------------
	// interface Document
	//----------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		
		jaxbContextClasses[0] = this.getClass();
		JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);

		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Source schemaFile = new StreamSource(Xsd.class.getResourceAsStream("MessageDictionary.xsd"));
		Schema schema = schemaFactory.newSchema(schemaFile);

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		unmarshaller.setSchema(schema);
		unmarshaller.setEventHandler(new ValidationEventHandler()
		{
			@Override
			public boolean handleEvent(ValidationEvent event)
			{
				System.out.println("Error in message dictionary : " + event);
				return false;
			}
		});

		MessageDictionary messageDictionary = ((MessageDictionary) unmarshaller.unmarshal(reader));
		messageDictionary.factory = getFactory();
		
		this.fields = messageDictionary.fields;
		this.messages = messageDictionary.messages;
		this.description = messageDictionary.description;
		this.name = messageDictionary.name;
		this.version = messageDictionary.version;
		
		this.changed = true;
	}

	@Override
	public boolean canClose() throws Exception
	{
    	return true;
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		
		try (OutputStream os = new FileOutputStream(new File(fileName)))
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, os);
		}
	}

	//----------------------------------------------------------------------------------------------------------------------
	// interface Mutable
	//----------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		if (this.fields.isChanged())
		{
			return true;
		}
		if (this.messages.isChanged())
		{
			return true;
		}
		return false;
	}

	@Override
	public void saved()
	{
		this.changed = false;
		this.fields.saved();
		this.messages.saved();
	}

	//----------------------------------------------------------------------------------------------------------------------
	// interface IMessageDictionary
	//----------------------------------------------------------------------------------------------------------------------
	
	@Override
	public String getDescription()
	{
		return this.description;
	}
	
	@Override
	public IField 			getField(String name)
	{
		for (Field field : this.fields.fields)
		{
			String str = field.getName();
			if (str != null && str.equals(name))
			{
				return field;
			}
		}
			
		return null; 
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IField>	getFields()
	{
		return this.fields.fields == null ? null : (List<IField>)(List<?>) this.fields.fields;
	}

	@Override
	public IMessage 		getMessage(String name)
	{
		for (Message mess : this.messages.messages)
		{
			IAttribute attr = mess.getAttribute("MessageType");
			if (attr == null)
			{
				continue;
			}
			
			String str = attr.getValue();
			if (str != null && str.equals(name))
			{
				return mess;
			}
		}
			
		return null; 
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IMessage>	getMessages()
	{
		return this.messages.messages == null ? null : (List<IMessage>)(List<?>) this.messages.messages;
	}

	@Override
	public String getVersion()
	{
		return this.version;
	}

	//----------------------------------------------------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + this.name + " <" + getName() + ">";
	}
	
	public void set(String name, Object value) throws Exception
	{
		if(set(MessageDictionary.class, this, name, value))
		{
			this.changed = true;
		}
	}

	private static boolean set(Class<?> clazz, Object object, String name, Object value) throws Exception
	{
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for (java.lang.reflect.Field field : fields)
		{
			XmlElement attr = field.getAnnotation(XmlElement.class);
			if (attr == null)
			{
				XmlAttribute attribute = field.getAnnotation(XmlAttribute.class);
				if (attribute == null)
				{
					continue;
				}
				if (attribute.name().equals(name))
				{
					Object oldValue = field.get(object);
					field.set(object, value);
					return !areEqual(oldValue, value);
				}
			}
			else
			{
				if (attr.name().equals(name))
				{
					Object oldValue = field.get(object);
					field.set(object, value);
					return !areEqual(oldValue, value);
				}
			}

		}
		return false;
	}

    private static boolean areEqual(Object o1, Object o2)
    {
    	if (o1 == null)
    	{
    		return o1 == o2;
    	}
    	
    	return o1.equals(o2);
    }

	private static final Class<?>[]	jaxbContextClasses	= 
		{ MessageDictionary.class, Messages.class, Message.class, Fields.class, Field.class, Attribute.class };

	@XmlTransient
	protected boolean 			changed;
}
