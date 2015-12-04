////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Spec implements Iterable<Piece>, Serializable
{
	public static Spec create()
	{
		return new Spec();
	}
	
	public Spec()
	{
		this.list = new ArrayList<Piece>();
	}
	
	@Override
	public Iterator<Piece> iterator()
	{
		return this.list.iterator();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(DoSpec.class.getSimpleName());
		for (Piece piece : this.list)
		{
			sb.append('.').append(piece.toString());
		}
		return sb.toString();
	}
	
	public void tune(IWindow window) throws Exception
	{
		for (Piece piece : this.list)
		{
			piece.tune(window);
		}
	}
	
	public <T> CheckingLayoutResult perform(OperationExecutor<T> executor, Locator owner, Locator locator) throws Exception
	{
		CheckingLayoutResult result = new CheckingLayoutResult();
		List<T> self = executor.findAll(owner, locator);
		
		for (Piece check : this.list)
		{
			check.kind.perform(check, executor, self, result);
		}
		
		return result;
	}
	

	//------------------------------------------------------------------------------------------------------------------------------
	// visible
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec visible()
	{
		return adder(() -> new Piece(PieceKind.VISIBLE));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// count
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec count(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.COUNT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec count(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.COUNT).setName(another));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// left
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec left(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec left(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.LEFT).setName(another));
	}

	public Spec left(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec left(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.LEFT).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// right
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec right(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec right(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.RIGHT).setName(another));
	}

	public Spec right(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec right(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.RIGHT).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// top
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec top(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec top(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.TOP).setName(another));
	}

	public Spec top(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec top(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.TOP).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// bottom
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec bottom(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec bottom(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.BOTTOM).setName(another));
	}

	public Spec bottom(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec bottom(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.BOTTOM).setLocator(null, locator));
	}


	
	
	@FunctionalInterface
	private interface F
	{
		Piece call();
	}
	
	private Spec adder(F func)
	{
		this.list.add(func.call());
		return this;
	}
	
	protected List<Piece> list;

	private static final long	serialVersionUID	= -9155953771178401088L;
}