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

import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.i18n.R;

import java.awt.Color;
import java.io.Serializable;


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
		return this.kind.toFormula(this.name, this.range, "" + this.a, "" + this.b, "" + this.text, this.text2, "" + this.color);
	}
	
	public void tune(IWindow window, ITemplateEvaluator templateEvaluator) throws Exception
	{
		if (this.name != null && !this.name.isEmpty())
		{
			IControl control = window.getControlForName(SectionKind.Run, this.name);
			if (control == null)
			{
				throw new Exception(String.format(R.PIECE_TUNE_EXCEPTION.get(), window,this.name));
			}
			
			this.locator = IControl.evaluateTemplate(control.locator(), templateEvaluator);
			IControl ownerControl = window.getOwnerControl(control);
			if (ownerControl != null)
			{
				this.owner = IControl.evaluateTemplate(ownerControl.locator(), templateEvaluator);
			}
		}
	}

	public PieceKind getKind()
	{
		return kind;
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

	public Piece setText(String text)
	{
		this.text = text;
		return this;
	}

	public Piece setText2(String text)
	{
		this.text2 = text;
		return this;
	}
	
	public Piece setColor(Color color)
	{
		this.color = color;
		return this;
	}

	protected PieceKind kind;
	protected String    name;
	protected Locator   owner;
	protected Locator   locator;
	protected long      a;
	protected long      b;
	protected Range     range;
	protected String    text;
	protected String    text2;
	protected Color     color;

}
