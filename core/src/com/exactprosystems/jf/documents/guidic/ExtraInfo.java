////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.common.report.HTMLhelper;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ExtraInfo implements Mutable, IExtraInfo
{
	protected static final Logger logger = Logger.getLogger(ExtraInfo.class);

	public static final String xpathName			= "xpath";
	public static final String rectangleName		= "rectangle";
	public static final String nodeName				= "node";
	public static final String attrName				= "attr";

	@XmlElement(name = xpathName)
	protected String xpath;

	@XmlElement(name = rectangleName)
	protected Rect rectangle;

	@XmlElement(name = nodeName)
	protected String node;

	@XmlElement(name = attrName)
	protected List<Attr> attributes;


    @XmlTransient
	private boolean changed;

	public ExtraInfo() 
	{
		this.changed = false;
	}
	
    @Override
    public String toString() 
    {
        return getClass().getSimpleName()  + ":" + hashCode();
    }

    public static ExtraInfo create() throws Exception
    {
    	ExtraInfo ret = new ExtraInfo(); 
		return ret;
    }

    //------------------------------------------------------------------------------------------------------------------
    // interface Mutable
    //------------------------------------------------------------------------------------------------------------------
	@Override
    public boolean isChanged()
	{
		return this.changed;
	}
	
	@Override
	public void saved()
	{
		this.changed = false;
	}
	
    //------------------------------------------------------------------------------------------------------------------
	public void correctAllXml()
	{
		this.node = xmlToText(this.node); 
		this.xpath= xmlToText(this.xpath);
	}

	public void correctAllText()
	{
		this.node = textToXml(this.node); 
		this.xpath= textToXml(this.xpath);;
	}

	private String xmlToText(String source)
	{
		return HTMLhelper.htmlunescape(source);
	}

	private String textToXml(String source)
	{
		return HTMLhelper.htmlescape(source);
	}

	static void set(Class<?> clazz, Object object, String name, Object value) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields)
		{
		    XmlElement attr = field.getAnnotation(XmlElement.class);
			if (attr == null)
			{
				continue;
			}
			if (attr.name().equals(name))
			{
				field.set(object, value);
			}
		}
	}

	static Object get(Class<?> clazz, Object object, String name) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields)
		{
		    XmlElement attr = field.getAnnotation(XmlElement.class);
			if (attr == null)
			{
				continue;
			}
			if (attr.name().equals(name))
			{
				return field.get(object);
			}
		}
		return null;
	}

	public void set(String name, Object value) throws Exception
	{
		Object oldValue = get(ExtraInfo.class, this, name);
		set(ExtraInfo.class, this, name, value);
		Object newValue = get(ExtraInfo.class, this, name);
	}

	public Object get(String name) throws Exception
	{
		return get(ExtraInfo.class, this, name);
	}
}
