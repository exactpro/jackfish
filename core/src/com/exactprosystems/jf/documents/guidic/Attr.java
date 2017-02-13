////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.exactprosystems.jf.api.common.Str;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Attr 
{
	public static final String nameName			= "name";
	
	@XmlAttribute(name = nameName)
	protected String name;
	
	@XmlValue
	protected String value;
	
	public Attr() 
	{
	}

	@Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Attr other = (Attr) obj;
        if (!Str.areEqual(this.name, other.name))
        {
            return false;
        }
        return Str.areEqual(this.value, other.value);
    }

    public Attr(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName() 
	{
		return this.name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}


	public String getValue() 
	{
		return this.value;
	}

	public void setValue(String value) 
	{
		this.value = value;
	}
}
