/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.guidic;

import java.awt.geom.Point2D;

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
	protected Double x1;

	@XmlAttribute(name = y1Name)
	protected Double y1;

	@XmlAttribute(name = x2Name)
	protected Double x2;

	@XmlAttribute(name = y2Name)
	protected Double y2;

	public Rect() 
	{
	}

	public Rect(double x1, double y1, double x2, double y2) 
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

    public Point2D center()
    {
        return new Point2D.Double((this.x1 + this.x2)/2, (this.y1 + this.y2)/2);
    }

    public double square()
    {
        return Math.abs((this.x2 - this.x1) * (this.y2 - this.y1));
    }

	public Double getX1() 
	{
		return this.x1;
	}

	public void setX1(Double x1) 
	{
		this.x1 = x1;
	}

	public Double getY1() 
	{
		return this.y1;
	}

	public void setY1(Double y1) 
	{
		this.y1 = y1;
	}

	public Double getX2() 
	{
		return this.x2;
	}

	public void setX2(Double x2) 
	{
		this.x2 = x2;
	}

	public Double getY2() 
	{
		return this.y2;
	}

	public void setY2(Double y2) 
	{
		this.y2 = y2;
	}
}
