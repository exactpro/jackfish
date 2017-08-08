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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.exactprosystems.jf.api.app.Mutable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "MessageDictionary", 
        propOrder = { "description", "fields", "messages" }
        )

@XmlRootElement(
        name = "dictionary"
        )

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

    @XmlElement(required = false)
    protected String    description;

    @XmlElement(required = true)
    protected Fields    fields;

    @XmlElement(required = true)
    protected Messages  messages;

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String    name;

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
        protected List<Field>   fields;

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
        protected List<Message> messages;
    }

    public MessageDictionaryBean()
    {
        this.fields = new Fields();
        this.messages = new Messages();
    }

    @Override
    public boolean isChanged()
    {
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
        this.fields.saved();
        this.messages.saved();
    }

}
