////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import com.exactprosystems.jf.api.app.IExtraInfo;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.report.HTMLhelper;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ExtraInfo implements Mutable, IExtraInfo
{
	protected static final Logger logger = Logger.getLogger(ExtraInfo.class);

	public static final String xpathName     = "xpath";
	public static final String rectangleName = "rectangle";
	public static final String nodeName      = "node";
	public static final String attrName      = "attr";

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

	public static ExtraInfo create()
	{
		return new ExtraInfo();
	}

	//region interface Mutable

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

	//endregion

	public void correctAllXml()
	{
		this.node = this.xmlToText(this.node);
		this.xpath = this.xmlToText(this.xpath);
	}

	public void correctAllText()
	{
		this.node = this.textToXml(this.node);
		this.xpath = this.textToXml(this.xpath);
	}

	public void set(String name, Object value) throws Exception
	{
		Object oldValue = get(ExtraInfo.class, this, name);
		set(ExtraInfo.class, this, name, value);
		Object newValue = get(ExtraInfo.class, this, name);
		this.changed = this.changed || !Objects.equals(oldValue, newValue);
	}

	public Object get(String name) throws Exception
	{
		return get(ExtraInfo.class, this, name);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + hashCode();
	}

	//region private methods
	private String xmlToText(String source)
	{
		return HTMLhelper.htmlunescape(source);
	}

	private String textToXml(String source)
	{
		return HTMLhelper.htmlescape(source);
	}

	private static void set(Class<?> clazz, Object object, String name, Object value) throws Exception
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

	private static Object get(Class<?> clazz, Object object, String name) throws Exception
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
	//endregion
}
