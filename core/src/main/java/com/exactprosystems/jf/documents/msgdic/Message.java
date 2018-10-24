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

import com.exactprosystems.jf.api.client.IAttribute;
import com.exactprosystems.jf.api.client.IField;
import com.exactprosystems.jf.api.client.IMessage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Message", propOrder = {"fields"})
public class Message extends Field implements IMessage
{
	@XmlElement(name = "field")
	protected List<Field> fields = new ArrayList<>();

	public Message()
	{
	}

	@Override
	public String toString()
	{
		return Message.class.getSimpleName() + "{" + this.fields + "}";
	}

	//region interface IMessage
	@Override
	public List<IField> getFields()
	{
		List<IField> res = new ArrayList<>();
		addAllField(res, (List<IField>) (List<?>) this.fields);
		return res;
	}

	@Override
	public List<IField> getMessageField()
	{
		return this.fields.stream()
				.map(f -> (IField) f)
				.collect(Collectors.toList());
	}

	@Override
	public IField getField(String name)
	{
		return this.fields.stream()
				.filter(field -> field.getName() != null && field.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	public IField getDeepField(String name)
	{
		return this.getFields().stream()
				.filter(field -> field.getName() != null && field.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	//endregion

	//region private methods
	private static void addAllField(List<IField> list, List<IField> fieldsList)
	{
		//TODO think about this method
		for (IField field : fieldsList)
		{
			list.add(field);
			IField ref = field.getReference();
			if (ref != null)
			{
				if (ref instanceof Message)
				{
					Message refMessage = (Message) ref;
					IAttribute entityType = refMessage.getAttribute("entity_type");
					if (entityType == null)
					{
						continue;
					}
					if ("Group".equals(entityType.getValue()))
					{
						list.add(field);
					}
					else
					{
						addAllField(list, refMessage.getFields());
					}
				}
//				else
//				{
//					list.add(ref);
//				}
			}
//			else
//			{
//				list.add(field);
//			}
		}
	}
	//endregion
}
