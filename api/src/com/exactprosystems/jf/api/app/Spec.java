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
	// contains
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec contains(String another)
	{
		return adder(() -> new Piece(PieceKind.CONTAINS).setName(another));
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
	
	//------------------------------------------------------------------------------------------------------------------------------
	// inLeft
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec inLeft(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.INSIDE_LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec inLeft(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.INSIDE_LEFT).setName(another));
	}

	public Spec inLeft(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.INSIDE_LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec inLeft(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.INSIDE_LEFT).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inRight
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec inRight(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.INSIDE_RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec inRight(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.INSIDE_RIGHT).setName(another));
	}

	public Spec inRight(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.INSIDE_RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec inRight(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.INSIDE_RIGHT).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inTop
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec inTop(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.INSIDE_TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec inTop(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.INSIDE_TOP).setName(another));
	}

	public Spec inTop(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.INSIDE_TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec inTop(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.INSIDE_TOP).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inBottom
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec inBottom(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.INSIDE_BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec inBottom(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.INSIDE_BOTTOM).setName(another));
	}

	public Spec inBottom(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.INSIDE_BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec inBottom(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.INSIDE_BOTTOM).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onLeft
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec onLeft(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.ON_LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec onLeft(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.ON_LEFT).setName(another));
	}

	public Spec onLeft(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.ON_LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec onLeft(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.ON_LEFT).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onRight
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec onRight(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.ON_RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec onRight(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.ON_RIGHT).setName(another));
	}

	public Spec onRight(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.ON_RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec onRight(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.ON_RIGHT).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onTop
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec onTop(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.ON_TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec onTop(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.ON_TOP).setName(another));
	}

	public Spec onTop(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.ON_TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec onTop(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.ON_TOP).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onBottom
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec onBottom(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.ON_BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec onBottom(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.ON_BOTTOM).setName(another));
	}

	public Spec onBottom(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.ON_BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec onBottom(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.ON_BOTTOM).setLocator(null, locator));
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// lAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec lAlign(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.LEFT_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec lAlign(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.LEFT_ALIGNED).setName(another));
	}

	public Spec lAlign(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.LEFT_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec lAlign(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.LEFT_ALIGNED).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// rAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec rAlign(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.RIGHT_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec rAlign(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.RIGHT_ALIGNED).setName(another));
	}

	public Spec rAlign(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.RIGHT_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec rAlign(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.RIGHT_ALIGNED).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// tAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec tAlign(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.TOP_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec tAlign(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.TOP_ALIGNED).setName(another));
	}

	public Spec tAlign(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.TOP_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec tAlign(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.TOP_ALIGNED).setLocator(null, locator));
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// bAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec bAlign(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.BOTTOM_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec bAlign(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.BOTTOM_ALIGNED).setName(another));
	}

	public Spec bAlign(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.BOTTOM_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec bAlign(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.BOTTOM_ALIGNED).setLocator(null, locator));
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// hCenter
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec hCenter(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.HORIZONTAL_CENTERED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec hCenter(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.HORIZONTAL_CENTERED).setName(another));
	}

	public Spec hCenter(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.HORIZONTAL_CENTERED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec hCenter(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.HORIZONTAL_CENTERED).setLocator(null, locator));
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// vCenter
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec vCenter(String another, Number dist)
	{
		return adder(() -> new Piece(PieceKind.VERTICAL_CENTERED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec vCenter(String another, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.VERTICAL_CENTERED).setName(another));
	}

	public Spec vCenter(Locator locator, Number dist)
	{
		return adder(() -> new Piece(PieceKind.VERTICAL_CENTERED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
	}

	public Spec vCenter(Locator locator, CheckProvider func)
	{
		return adder(() -> func.provide(PieceKind.VERTICAL_CENTERED).setLocator(null, locator));
	}
	
	
	
	
	
	
	//------------------------------------------------------------------------------------------------------------------------------
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