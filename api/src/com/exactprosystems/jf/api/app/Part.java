////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.io.Serializable;

import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.client.ICondition;

public class Part implements Serializable
{
	private static final long serialVersionUID = -5317750838749524513L;

	public Part(OperationKind kind)
	{
		this.kind = kind;
		this.locator = null;
		this.operation = null;
		this.owner = null;
		this.element = null;
		this.valueCondition = null;
		this.colorCondition = null;
		this.i = 0;
		this.d = Double.NaN;
		this.x = Integer.MIN_VALUE;
		this.y = Integer.MIN_VALUE;
	}
	
	public void tune(IWindow window) throws Exception
	{
		if (this.locator != null && !this.locator.isEmpty())
		{
			IControl control = window.getControlForName(SectionKind.Run, this.locator);
			if (control == null)
			{
				throw new Exception("Cannot find control in dialog='" + window +"' section='" + SectionKind.Run + "' name='" + this.locator + "'");
			}
			IControl owner = window.getOwnerControl(control);
			
			this.owner = owner == null ? null : owner.locator();
			this.element = control.locator();
		}
	}

	@Override
	public String toString()
	{
		return this.kind + "[" 
				+ " b=" + this.b + " " 
				+ (this.i >= 0 ? "i=" + this.i + " " : "") 
				+ (this.d != Double.NaN ? "d=" + this.d + " " : "") 
				+ (this.x != Integer.MIN_VALUE ? "x=" + this.x + " " : "")
				+ (this.y != Integer.MIN_VALUE ? "y=" + this.y + " " : "") 
				+ (this.text != null ? "text=" + this.text + " " : "")
				+ (this.locator != null ? "locator=" + this.locator + " " : "")
				+ (this.valueCondition != null ? "value condition=" + this.valueCondition + " " : "")
				+ (this.colorCondition != null ? "color condition=" + this.colorCondition + " " : "")
				+ (this.mouse != null ? "mouse=" + this.mouse + " " : "") 
				+ (this.operation != null ? "operation=" + this.operation+ " " : "")
				+ (this.key != null ? "key=" + this.key + " " : "") + "]";
	}
	
	public Part setInt(int i)
	{
		this.i = i;
		return this;
	}

	public Part setX(int x)
	{
		this.x = x;
		return this;
	}

	public Part setY(int y)
	{
		this.y = y;
		return this;
	}
	
	public Part setText(String text)
	{
		this.text = text;
		return this;
	}
	
	public Part setBool(boolean bool)
	{
		this.b = bool;
		return this;
	}
	
	public Part setLocatorId(String locator)
	{
		this.locator = locator;
		return this;
	}

	public Part setLocator(Locator locator)
	{
		this.element = locator;
		return this;
	}

	public Part setValueCondition(ICondition valueCondition)
	{
		this.valueCondition = valueCondition;
		return this;
	}

	public Part setColorCondition(ICondition colorCondition)
	{
		this.colorCondition = colorCondition;
		return this;
	}

	public Part setMouseAction(MouseAction mouse)
	{
		this.mouse = mouse;
		return this;
	}

	public Part setKey(Keyboard key)
	{
		this.key = key;
		return this;
	}

	public Part setValue(double d)
	{
		this.d = d;
		return this;
	}

	public Part setOperation(Operation operation)
	{
		this.operation = operation;
		return this;
	}

	Operation operation;
	private String locator;
	Locator owner;
	Locator element;
	int i;
	int x;
	int y;
	double d;
	boolean b;
	String text;
	OperationKind kind;
	ICondition valueCondition;
	ICondition colorCondition;
	MouseAction mouse;
	Keyboard key;
}