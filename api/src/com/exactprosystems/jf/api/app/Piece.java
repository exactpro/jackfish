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


public class Piece implements Serializable
{
	private static final long	serialVersionUID	= -4982322639565633145L;

	public Piece(PieceKind kind)
	{
		this.kind = kind;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.kind.toString());
		sb.append('(');
		String separator = "";
		
		if (this.kind.useName())
		{
			separator = ", ";
			if (this.locator != null)
			{
				sb.append("'").append(this.locator.getId()).append("'");
			}
			else
			{
				sb.append("'").append(this.name).append("'"); 
			}
		}

		if (this.kind.useRange())
		{
			sb.append(separator).append(this.range.toString("" + this.a, "" + this.b));
		}
		
		sb.append(")");
		
		return sb.toString();
	}
	
	public void tune(IWindow window) throws Exception
	{
		if (this.name != null && !this.name.isEmpty())
		{
			IControl control = window.getControlForName(SectionKind.Run, this.name);
			if (control == null)
			{
				throw new Exception("Cannot find control in dialog='" + window +"' section='Run' name='" + this.name + "'");
			}
			
			this.locator = control.locator();
			IControl ownerControl = window.getOwnerControl(control);
			if (ownerControl != null)
			{
				this.owner = ownerControl.locator();
			}
		}
	}

	

	public Piece setRange(Range range)
	{
		this.range = range;
		return this;
	}

	public Piece setName(String name)
	{
		this.name = name;
		return this;
	}

	public Piece setLocator(Locator owner, Locator locator)
	{
		this.owner = owner;
		this.locator = locator;
		return this;
	}

	public Piece setA(long a)
	{
		this.a = a;
		return this;
	}
	
	public Piece setB(long b)
	{
		this.b = b;
		return this;
	}

	protected PieceKind kind;
	protected String name;
	protected Locator owner;
	protected Locator locator;
	protected long a;
	protected long b;
	protected Range range;
}
