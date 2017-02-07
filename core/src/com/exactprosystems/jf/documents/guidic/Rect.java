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

@XmlRootElement(name = "rectangle")
@XmlAccessorType(XmlAccessType.NONE)
public class Rect 
{
	public static final String x1Name = "x1";
	public static final String y1Name = "y1";
	public static final String x2Name = "x2";
	public static final String y2Name = "y2";

	@XmlAttribute(name = x1Name)
	protected Integer x1;

	@XmlAttribute(name = y1Name)
	protected Integer y1;

	@XmlAttribute(name = x2Name)
	protected Integer x2;

	@XmlAttribute(name = y2Name)
	protected Integer y2;

	public Rect() 
	{
	}

	public Rect(int x1, int y1, int x2, int y2) 
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public Integer getX1() 
	{
		return this.x1;
	}

	public void setX1(Integer x1) 
	{
		this.x1 = x1;
	}

	public Integer getY1() 
	{
		return this.y1;
	}

	public void setY1(Integer y1) 
	{
		this.y1 = y1;
	}

	public Integer getX2() 
	{
		return this.x2;
	}

	public void setX2(Integer x2) 
	{
		this.x2 = x2;
	}

	public Integer getY2() 
	{
		return this.y2;
	}

	public void setY2(Integer y2) 
	{
		this.y2 = y2;
	}

}
