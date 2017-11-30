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
import com.exactprosystems.jf.api.client.IMessage;
import com.exactprosystems.jf.api.client.IMessageDictionary;
import com.exactprosystems.jf.common.xml.schema.Xsd;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.DocumentKind;
import org.apache.log4j.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.util.List;

@DocumentInfo(
        kind = DocumentKind.MESSAGE_DICTIONARY,
		newName = "NewDictionary", 
		extension = "xml",
		description = "Message dictionary"
)
public class MessageDictionary extends AbstractDocument implements IMessageDictionary, Serializable
{
    private static final long serialVersionUID = 8949804056711432386L;
    private static final Logger logger = Logger.getLogger(MessageDictionary.class);

    protected MessageDictionaryBean bean;
    
    public MessageDictionary()
    {
        this(null, null);
    }

    public MessageDictionary(String fileName, DocumentFactory factory)
    {
        super(fileName, factory);
        this.bean = new MessageDictionaryBean();
        this.changed = false;
    }

	//----------------------------------------------------------------------------------------------------------------------
	// interface Document
	//----------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void load(Reader reader) throws Exception
	{
		try
		{
			super.load(reader);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(MessageDictionaryBean.jaxbContextClasses);
	
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
				    logger.error(event);
					System.out.println("Error in message dictionary : " + event);
					return false;
				}
			});
	
			this.bean = ((MessageDictionaryBean) unmarshaller.unmarshal(reader));
			
			this.changed = true;
		}
		catch (UnmarshalException e)
		{
			throw new Exception(e.getCause().getMessage(), e.getCause());
		}
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
			JAXBContext jaxbContext = JAXBContext.newInstance(MessageDictionaryBean.jaxbContextClasses);

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this.bean, os);
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
		return this.bean.isChanged();
	}

	@Override
	public void saved()
	{
		this.changed = false;
		this.bean.saved();
	}

	//----------------------------------------------------------------------------------------------------------------------
	// interface IMessageDictionary
	//----------------------------------------------------------------------------------------------------------------------
    @Override
    public String getFilePath()
    {
        return getNameProperty().get();
    }

	@Override
	public String getDescription()
	{
		return this.bean.description;
	}
	
	@Override
	public IField 			getField(String name)
	{
		for (Field field : this.bean.fields.fields)
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
		return this.bean.fields.fields == null ? null : (List<IField>)(List<?>) this.bean.fields.fields;
	}

	@Override
	public IMessage 		getMessageByName(String name)
	{
		for (Message mess : this.bean.messages.messages)
		{
			String str = mess.getName();
			if (str != null && str.equals(name))
			{
				return mess;
			}
		}
			
		return null; 
	}

	@Override
	public IMessage 		getMessage(String name)
	{
	    if (name == null)
	    {
	        return null;
	    }
	    
		for (Message mess : this.bean.messages.messages)
		{
		    if (name.equals(mess.getName()))
		    {
		        return mess;
		    }
		    
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
		return this.bean.messages.messages == null ? null : (List<IMessage>)(List<?>) this.bean.messages.messages;
	}

	//----------------------------------------------------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + this.bean.name + " <" + getNameProperty() + ">";
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

	protected boolean 			changed;
}
