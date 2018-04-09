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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;

@DocumentInfo(
        kind = DocumentKind.MESSAGE_DICTIONARY,
		newName = "NewDictionary", 
		extension = "xml",
		description = "Message dictionary"
)
public class MessageDictionary extends AbstractDocument implements IMessageDictionary
{
	private static final Logger logger = Logger.getLogger(MessageDictionary.class);

	protected boolean               changed;
	private   MessageDictionaryBean bean;

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

	//region interface Document

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
			unmarshaller.setEventHandler(event ->
			{
				logger.error(event);
				System.out.println("Error in message dictionary : " + event);
				return false;
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
	public boolean canClose()
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

	//endregion

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.changed || this.bean.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		this.changed = false;
		this.bean.saved();
		super.saved();
	}

	//endregion

	//region interface IMessageDictionary
	@Override
	public String getFilePath()
	{
		return super.getNameProperty().get();
	}

	@Override
	public String getDescription()
	{
		return this.bean.description;
	}

	@Override
	public IField getField(String name)
	{
		return this.bean.fields.fields.stream()
				.filter(field -> field.getName() != null && field.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<IField> getFields()
	{
		return this.bean.fields.fields == null ? null : (List<IField>) (List<? extends IField>) this.bean.fields.fields;
	}

	@Override
	public IMessage getMessageByName(String name)
	{
		return this.bean.messages.messages.stream()
				.filter(message -> message.getName() != null && message.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	@Override
	public IMessage getMessage(String name)
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

	@Override
	public List<IMessage> getMessages()
	{
		return this.bean.messages.messages == null ? null : (List<IMessage>) (List<?>) this.bean.messages.messages;
	}

	//endregion

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + ":" + this.bean.name + " <" + super.getNameProperty() + ">";
	}

	public void set(String name, Object value) throws Exception
	{
		if (set(MessageDictionary.class, this, name, value))
		{
			this.changed = true;
		}
	}

	//region private methods
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
			return o2 == null;
		}

		return o1.equals(o2);
	}
	//endregion
}
