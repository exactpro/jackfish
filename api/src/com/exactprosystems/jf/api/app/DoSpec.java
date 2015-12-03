////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;


public class DoSpec
{
	public static CheckProvider less(Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.LESS).setA(n.longValue()); 
	}

	public static CheckProvider great(Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.GREAT).setA(n.longValue()); 
	}

	public static CheckProvider about(Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.ABOUT).setA(n.longValue()); 
	}

	public static CheckProvider between(Number n, Number m)
	{
		return  (kind) -> new Piece(kind).setRange(Range.BETWEEN).setA(n.longValue()).setB(m.longValue()); 
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// count
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec visible()
	{
		return new Spec().visible();
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// count
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec count(String another, Number dist)
	{
		return new Spec().count(another, dist.longValue());
	}

	public static Spec count(String another, CheckProvider func)
	{
		return new Spec().count(another, func);
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// left
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec left(String another, Number dist)
	{
		return new Spec().left(another, dist.longValue());
	}

	public static Spec left(String another, CheckProvider func)
	{
		return new Spec().left(another, func);
	}

	public static Spec left(Locator locator, Number dist)
	{
		return new Spec().left(locator, dist.longValue());
	}

	public static Spec left(Locator locator, CheckProvider func)
	{
		return new Spec().left(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// right
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec right(String another, Number dist)
	{
		return new Spec().right(another, dist.longValue());
	}

	public static Spec right(String another, CheckProvider func)
	{
		return new Spec().right(another, func);
	}

	public static Spec right(Locator locator, Number dist)
	{
		return new Spec().right(locator, dist.longValue());
	}

	public static Spec right(Locator locator, CheckProvider func)
	{
		return new Spec().right(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// top
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec top(String another, Number dist)
	{
		return new Spec().top(another, dist.longValue());
	}

	public static Spec top(String another, CheckProvider func)
	{
		return new Spec().top(another, func);
	}

	public static Spec top(Locator locator, Number dist)
	{
		return new Spec().top(locator, dist.longValue());
	}

	public static Spec top(Locator locator, CheckProvider func)
	{
		return new Spec().top(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// bottom
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec bottom(String another, Number dist)
	{
		return new Spec().bottom(another, dist.longValue());
	}

	public static Spec bottom(String another, CheckProvider func)
	{
		return new Spec().bottom(another, func);
	}

	public static Spec bottom(Locator locator, Number dist)
	{
		return new Spec().bottom(locator, dist.longValue());
	}

	public static Spec bottom(Locator locator, CheckProvider func)
	{
		return new Spec().bottom(locator, func);
	}

	
}
