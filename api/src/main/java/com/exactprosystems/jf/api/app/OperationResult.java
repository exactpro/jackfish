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

package com.exactprosystems.jf.api.app;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperationResult implements Serializable
{
	private static final long			serialVersionUID	= -2015741070415094348L;

	private boolean						ok					= false;
    private Boolean                     bool                = null;
    private Color                       color               = null;
	private Integer                     integer             = null;
	private String						text				= null;
    private String                      error               = null;
    private Locator                     locator             = null;
    private List<String>                list                = null;
	private Map<String, String>			map					= null;
	private Map<String, ValueAndColor>	colorMap			= null;
	private String[][]					array				= null;
	private Rectangle 					rectangle 			= null;
	private Document					xml 				= null;

	
	public void setBool(Boolean bool)
	{
	    this.bool = bool;
	}
	
	public void setColor(Color color)
	{
	    this.color = color;
	}
	
	public void setInt(int i)
	{
	    this.integer = i;
	}
	
    public void setError(String text, Locator locator)
    {
        this.ok = false;
        this.error = text;
        this.locator = locator;
    }

	public void setText(String text)
	{
		this.text = text;
	}

	public void setOk(boolean ok)
	{
		this.ok = ok;
	}

	public void setMap(Map<String, String> map)
	{
		this.map = new LinkedHashMap<>();
		this.map.putAll(map);
	}

	public void setColorMap(Map<String, ValueAndColor> colorMap)
	{
		this.colorMap = new LinkedHashMap<>();
		this.colorMap.putAll(colorMap);
	}

	public void setXml(Document xml) {
		this.xml = xml;
	}

	public void setArray(String[][] a)
	{
		this.array = a;
	}

	public void setList(List<String> list)
	{
		this.list = new ArrayList<>(list);
	}

	public void setRectangle(Rectangle rectangle)
	{
		this.rectangle = rectangle;
	}
	

	public boolean isOk()
	{
		return this.ok;
	}

	public Object getValue()
	{
	    if (this.error != null)
	    {
	        return this.error;
	    }
        if (this.bool != null)
        {
            return this.bool;
        }
        if (this.color != null)
        {
            return this.color;
        }
	    if (this.integer != null)
	    {
	        return this.integer;
	    }
		if (this.text != null)
		{
			return this.text;
		}
		if (this.map != null)
		{
			return this.map;
		}
		if (this.colorMap != null)
		{
			return this.colorMap;
		}
        if (this.list != null)
        {
            return this.list;
        }
		if (this.array != null)
		{
			return  this.array;
		}
		if (this.rectangle != null)
		{
			return this.rectangle;
		}
		if (this.xml != null)
		{
			return this.xml;
		}
		return null;
	}
	
	public Locator getLocator()
	{
	    return this.locator;
	}
	
	
	public String humanablePresentation()
	{
	    if (!this.ok)
	    {
	        return "Error: " + this.error;
	    }
	    
		StringBuilder builder = new StringBuilder();
        if (this.locator != null)
        {
            builder.append(" locator [").append(this.locator).append("];");
        }
        if (this.bool != null)
        {
            builder.append(" bool [").append(this.bool).append("];");
        }
        if (this.color != null)
        {
            builder.append(" color [").append(this.color).append("];");
        }
		if (this.integer != null)
        {
            builder.append(" int [").append(this.integer).append("];");
        }
		if (this.rectangle != null)
		{
			builder.append(" rectangle [").append(this.rectangle).append("];");
		}
		if (this.map != null)
		{
			builder.append(" map [").append(this.map).append("];");
		}
		if (this.colorMap != null)
		{
			builder.append(" color map [").append(this.colorMap).append("];");
		}
        if (this.list != null)
        {
            builder.append(" list [").append(this.list).append("];");
        }
		if (this.array != null)
		{
			builder.append(" array [").append(Arrays.deepToString(this.array)).append("];");
		}
		if (this.xml != null)
		{
			builder.append(xmlToString(this.xml));
		}
		if (builder.toString().isEmpty())
		{
			return "" + this.text;
		}
		if (this.text != null)
		{
			builder.append(" text [").append(this.text).append("];");
		}
		return builder.toString();
	}

	private static String xmlToString(Document doc) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");

			Writer stringWriter = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
			return stringWriter.toString();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString()
	{
		return humanablePresentation();
	}
}
