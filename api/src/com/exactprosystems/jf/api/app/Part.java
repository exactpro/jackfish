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
		
		this.locatorId = null;
		this.operation = null;
		this.valueCondition = null;
		this.colorCondition = null;
		this.i = 0;
		this.d = Double.NaN;
		this.b = false;
		this.x = Integer.MIN_VALUE;
		this.y = Integer.MIN_VALUE;
		this.text = null;
		this.key = null;
		this.mouse = null;

		this.owner = null;
		this.element = null;
	}
	
	public void tune(IWindow window) throws Exception
	{
		if (this.locatorId != null && !this.locatorId.isEmpty())
		{
			IControl control = window.getControlForName(SectionKind.Run, this.locatorId);
			if (control == null)
			{
				throw new Exception("Cannot find control in dialog='" + window +"' section='" + SectionKind.Run + "' name='" + this.locatorId + "'");
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
				+ (this.locatorId != null ? "locator=" + this.locatorId + " " : "")
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
		this.locatorId = locator;
		return this;
	}

	public Part setOwner(Locator locator)
	{
		this.owner = locator;
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

	protected Operation operation;
	protected String locatorId;
	protected int i;
	protected int x;
	protected int y;
	protected double d;
	protected boolean b;
	protected String text;
	protected OperationKind kind;
	protected ICondition valueCondition;
	protected ICondition colorCondition;
	protected MouseAction mouse;
	protected Keyboard key;

	protected Locator owner;
	protected Locator element;
}