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
		this.list.add(new Piece(PieceKind.VISIBLE));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// count
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec count(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.COUNT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec count(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.COUNT).setName(another));
		return this;
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// contains
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec contains(String another)
	{
		this.list.add(new Piece(PieceKind.CONTAINS).setName(another));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// left
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec left(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec left(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT).setName(another));
		return this;
	}

	public Spec left(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec left(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// right
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec right(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec right(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT).setName(another));
		return this;
	}

	public Spec right(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec right(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// top
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec top(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec top(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP).setName(another));
		return this;
	}

	public Spec top(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec top(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// bottom
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec bottom(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec bottom(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM).setName(another));
		return this;
	}

	public Spec bottom(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec bottom(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM).setLocator(null, locator));
		return this;
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// inLeft
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec inLeft(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec inLeft(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_LEFT).setName(another));
		return this;
	}

	public Spec inLeft(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec inLeft(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_LEFT).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inRight
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec inRight(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec inRight(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_RIGHT).setName(another));
		return this;
	}

	public Spec inRight(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec inRight(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_RIGHT).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inTop
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec inTop(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec inTop(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_TOP).setName(another));
		return this;
	}

	public Spec inTop(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec inTop(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_TOP).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inBottom
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec inBottom(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec inBottom(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_BOTTOM).setName(another));
		return this;
	}

	public Spec inBottom(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec inBottom(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_BOTTOM).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onLeft
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec onLeft(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec onLeft(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_LEFT).setName(another));
		return this;
	}

	public Spec onLeft(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec onLeft(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_LEFT).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onRight
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec onRight(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec onRight(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_RIGHT).setName(another));
		return this;
	}

	public Spec onRight(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec onRight(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_RIGHT).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onTop
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec onTop(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec onTop(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_TOP).setName(another));
		return this;
	}

	public Spec onTop(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec onTop(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_TOP).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onBottom
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec onBottom(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec onBottom(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_BOTTOM).setName(another));
		return this;
	}

	public Spec onBottom(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec onBottom(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_BOTTOM).setLocator(null, locator));
		return this;
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// lAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec lAlign(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec lAlign(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT_ALIGNED).setName(another));
		return this;
	}

	public Spec lAlign(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec lAlign(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT_ALIGNED).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// rAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec rAlign(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec rAlign(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT_ALIGNED).setName(another));
		return this;
	}

	public Spec rAlign(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec rAlign(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT_ALIGNED).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// tAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec tAlign(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec tAlign(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP_ALIGNED).setName(another));
		return this;
	}

	public Spec tAlign(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec tAlign(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP_ALIGNED).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// bAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec bAlign(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec bAlign(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM_ALIGNED).setName(another));
		return this;
	}

	public Spec bAlign(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec bAlign(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM_ALIGNED).setLocator(null, locator));
		return this;
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// hCenter
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec hCenter(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.HORIZONTAL_CENTERED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec hCenter(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.HORIZONTAL_CENTERED).setName(another));
		return this;
	}

	public Spec hCenter(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.HORIZONTAL_CENTERED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec hCenter(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.HORIZONTAL_CENTERED).setLocator(null, locator));
		return this;
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// vCenter
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec vCenter(String another, Number dist)
	{
		this.list.add(new Piece(PieceKind.VERTICAL_CENTERED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec vCenter(String another, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.VERTICAL_CENTERED).setName(another));
		return this;
	}

	public Spec vCenter(Locator locator, Number dist)
	{
		this.list.add(new Piece(PieceKind.VERTICAL_CENTERED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec vCenter(Locator locator, CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.VERTICAL_CENTERED).setLocator(null, locator));
		return this;
	}
	
	
	
	
	
	
	//------------------------------------------------------------------------------------------------------------------------------
	protected List<Piece> list;

	private static final long	serialVersionUID	= -9155953771178401088L;
}