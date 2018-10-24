/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.documents.msgdic;

import com.exactprosystems.jf.api.app.Mutable;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

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
