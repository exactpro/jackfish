////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.client.ICondition;

import java.io.Serializable;

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
		this.i = -1;
		this.d = Double.NaN;
		this.b = false;
		this.toAppear = false;
		this.x = Integer.MIN_VALUE;
		this.y = Integer.MIN_VALUE;
		this.x2 = Integer.MIN_VALUE;
		this.y2 = Integer.MIN_VALUE;
		this.str = null;
		this.text = null;
		this.key = null;
		this.mouse = null;

		this.locatorKind = null;
		this.locator = null;
	}

	public void tune(IWindow window) throws Exception
	{
		if (this.kind == OperationKind.REPEAT && this.operation != null)
		{
			this.operation.tune(window);
		}

		if (this.locatorId != null && !this.locatorId.isEmpty() && this.locatorKind != null)
		{
			IControl control = window.getControlForName(SectionKind.Run, this.locatorId);
			if (control == null)
			{
				throw new Exception("Cannot find control in dialog='" + window +"' section='Run' name='" + this.locatorId + "'");
			}
			//b is true, when we use Do.use(locatorId, locatorKind) and we needn't find another locator
			if (!b)
			{
				switch (this.locatorKind)
				{
					case Element:
						this.locator = control.locator();
						break;

					case Owner:
					case DroppedOwner:
						control = window.getOwnerControl(control);
						break;

					case Header:
						control = window.getHeaderControl(control);
						break;

					case Rows:
						control = window.getRowsControl(control);
						break;
				}
			}

			this.locator = control == null ? null : control.locator();
		}
	}

	@Override
	public String toString()
	{
		return this.kind.toFormula(this);
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

	public Part setX2(int x2) {
		this.x2 = x2;
		return this;
	}

	public Part setY2(int y2) {
		this.y2 = y2;
		return this;
	}

	public Part setStr(String str)
	{
		this.str = str;
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

	public Part setToAppear(boolean toAppear) {
		this.toAppear = toAppear;
		return this;
	}

	public Part setLocatorId(String locator)
	{
		this.locatorId = locator;
		return this;
	}

	public Part setLocator(Locator locator)
	{
		this.locator = locator;
		return this;
	}

	public Part setLocatorKind(LocatorKind locatorKind)
	{
		this.locatorKind = locatorKind;
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

	protected OperationKind kind;

	protected Operation operation;
	protected int i;
	protected int x;
	protected int y;
	protected int x2;
	protected int y2;
	protected double d;
	protected boolean b;
	protected boolean toAppear;
	protected String str;
	protected String text;
	protected ICondition valueCondition;
	protected ICondition colorCondition;
	protected MouseAction mouse;
	protected Keyboard key;

	protected String locatorId;
	protected LocatorKind locatorKind;
	protected Locator locator;
}