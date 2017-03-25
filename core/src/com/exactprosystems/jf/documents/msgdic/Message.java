////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.msgdic;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.exactprosystems.jf.api.client.IAttribute;
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
	protected List<Field>	fields = new ArrayList<>();

	//----------------------------------------------------------------------------------------------------------------------
	// interface IMessage
	//----------------------------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
    @Override
	public List<IField> getFields()
	{
	    List<IField> res = new ArrayList<>();
	    addAllField(res, (List<IField>)(List<?>)this.fields);
		return res;
	}

	private static void addAllField(List<IField> list, List<IField> messageList)
	{
        for (IField field : messageList)
        {
            IField ref = field.getReference();
            if (ref != null)
            {
                if (ref instanceof Message)
                {
                    Message refMessage = (Message)ref;
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
                else
                {
                    list.add(ref);
                }
            }
            else
            {
                list.add(field);
            }
        }
	}
	
	@Override
	public IField getField(String name)
	{
		for (IField field : getFields())
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
