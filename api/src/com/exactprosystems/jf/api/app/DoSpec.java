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
	public static  CheckProvider less(Number n)
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
	// visible
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
		return new Spec().count(another, dist);
	}

	public static Spec count(String another, CheckProvider func)
	{
		return new Spec().count(another, func);
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// contains
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec contains(String another)
	{
		return new Spec().contains(another);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// left
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec left(String another, Number dist)
	{
		return new Spec().left(another, dist);
	}

	public static Spec left(String another, CheckProvider func)
	{
		return new Spec().left(another, func);
	}

	public static Spec left(Locator locator, Number dist)
	{
		return new Spec().left(locator, dist);
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
		return new Spec().right(another, dist);
	}

	public static Spec right(String another, CheckProvider func)
	{
		return new Spec().right(another, func);
	}

	public static Spec right(Locator locator, Number dist)
	{
		return new Spec().right(locator, dist);
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
		return new Spec().top(another, dist);
	}

	public static Spec top(String another, CheckProvider func)
	{
		return new Spec().top(another, func);
	}

	public static Spec top(Locator locator, Number dist)
	{
		return new Spec().top(locator, dist);
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
		return new Spec().bottom(another, dist);
	}

	public static Spec bottom(String another, CheckProvider func)
	{
		return new Spec().bottom(another, func);
	}

	public static Spec bottom(Locator locator, Number dist)
	{
		return new Spec().bottom(locator, dist);
	}

	public static Spec bottom(Locator locator, CheckProvider func)
	{
		return new Spec().bottom(locator, func);
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// inLeft
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec inLeft(String another, Number dist)
	{
		return new Spec().inLeft(another, dist);
	}

	public static Spec inLeft(String another, CheckProvider func)
	{
		return new Spec().inLeft(another, func);
	}

	public static Spec inLeft(Locator locator, Number dist)
	{
		return new Spec().inLeft(locator, dist);
	}

	public static Spec inLeft(Locator locator, CheckProvider func)
	{
		return new Spec().inLeft(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inRight
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec inRight(String another, Number dist)
	{
		return new Spec().inRight(another, dist);
	}

	public static Spec inRight(String another, CheckProvider func)
	{
		return new Spec().inRight(another, func);
	}

	public static Spec inRight(Locator locator, Number dist)
	{
		return new Spec().inRight(locator, dist);
	}

	public static Spec inRight(Locator locator, CheckProvider func)
	{
		return new Spec().inRight(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inTop
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec inTop(String another, Number dist)
	{
		return new Spec().inTop(another, dist);
	}

	public static Spec inTop(String another, CheckProvider func)
	{
		return new Spec().inTop(another, func);
	}

	public static Spec inTop(Locator locator, Number dist)
	{
		return new Spec().inTop(locator, dist);
	}

	public static Spec inTop(Locator locator, CheckProvider func)
	{
		return new Spec().inTop(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// inBottom
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec inBottom(String another, Number dist)
	{
		return new Spec().inBottom(another, dist);
	}

	public static Spec inBottom(String another, CheckProvider func)
	{
		return new Spec().inBottom(another, func);
	}

	public static Spec inBottom(Locator locator, Number dist)
	{
		return new Spec().inBottom(locator, dist);
	}

	public static Spec inBottom(Locator locator, CheckProvider func)
	{
		return new Spec().inBottom(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onLeft
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec onLeft(String another, Number dist)
	{
		return new Spec().onLeft(another, dist);
	}

	public static Spec onLeft(String another, CheckProvider func)
	{
		return new Spec().onLeft(another, func);
	}

	public static Spec onLeft(Locator locator, Number dist)
	{
		return new Spec().onLeft(locator, dist);
	}

	public static Spec onLeft(Locator locator, CheckProvider func)
	{
		return new Spec().onLeft(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onRight
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec onRight(String another, Number dist)
	{
		return new Spec().onRight(another, dist);
	}

	public static Spec onRight(String another, CheckProvider func)
	{
		return new Spec().onRight(another, func);
	}

	public static Spec onRight(Locator locator, Number dist)
	{
		return new Spec().onRight(locator, dist);
	}

	public static Spec onRight(Locator locator, CheckProvider func)
	{
		return new Spec().onRight(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onTop
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec onTop(String another, Number dist)
	{
		return new Spec().onTop(another, dist);
	}

	public static Spec onTop(String another, CheckProvider func)
	{
		return new Spec().onTop(another, func);
	}

	public static Spec onTop(Locator locator, Number dist)
	{
		return new Spec().onTop(locator, dist);
	}

	public static Spec onTop(Locator locator, CheckProvider func)
	{
		return new Spec().onTop(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// onBottom
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec onBottom(String another, Number dist)
	{
		return new Spec().onBottom(another, dist);
	}

	public static Spec onBottom(String another, CheckProvider func)
	{
		return new Spec().onBottom(another, func);
	}

	public static Spec onBottom(Locator locator, Number dist)
	{
		return new Spec().onBottom(locator, dist);
	}

	public static Spec onBottom(Locator locator, CheckProvider func)
	{
		return new Spec().onBottom(locator, func);
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// lAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec lAlign(String another, Number dist)
	{
		return new Spec().lAlign(another, dist);
	}

	public static Spec lAlign(String another, CheckProvider func)
	{
		return new Spec().lAlign(another, func);
	}

	public static Spec lAlign(Locator locator, Number dist)
	{
		return new Spec().lAlign(locator, dist);
	}

	public static Spec lAlign(Locator locator, CheckProvider func)
	{
		return new Spec().lAlign(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// rAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec rAlign(String another, Number dist)
	{
		return new Spec().rAlign(another, dist);
	}

	public static Spec rAlign(String another, CheckProvider func)
	{
		return new Spec().rAlign(another, func);
	}

	public static Spec rAlign(Locator locator, Number dist)
	{
		return new Spec().rAlign(locator, dist);
	}

	public static Spec rAlign(Locator locator, CheckProvider func)
	{
		return new Spec().rAlign(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// tAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec tAlign(String another, Number dist)
	{
		return new Spec().tAlign(another, dist);
	}

	public static Spec tAlign(String another, CheckProvider func)
	{
		return new Spec().tAlign(another, func);
	}

	public static Spec tAlign(Locator locator, Number dist)
	{
		return new Spec().tAlign(locator, dist);
	}

	public static Spec tAlign(Locator locator, CheckProvider func)
	{
		return new Spec().tAlign(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// bAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec bAlign(String another, Number dist)
	{
		return new Spec().bAlign(another, dist);
	}

	public static Spec bAlign(String another, CheckProvider func)
	{
		return new Spec().bAlign(another, func);
	}

	public static Spec bAlign(Locator locator, Number dist)
	{
		return new Spec().bAlign(locator, dist);
	}

	public static Spec bAlign(Locator locator, CheckProvider func)
	{
		return new Spec().bAlign(locator, func);
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// hCenter
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec hCenter(String another, Number dist)
	{
		return new Spec().hCenter(another, dist);
	}

	public static Spec hCenter(String another, CheckProvider func)
	{
		return new Spec().hCenter(another, func);
	}

	public static Spec hCenter(Locator locator, Number dist)
	{
		return new Spec().hCenter(locator, dist);
	}

	public static Spec hCenter(Locator locator, CheckProvider func)
	{
		return new Spec().hCenter(locator, func);
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// vCenter
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec vCenter(String another, Number dist)
	{
		return new Spec().vCenter(another, dist);
	}

	public static Spec vCenter(String another, CheckProvider func)
	{
		return new Spec().vCenter(another, func);
	}

	public static Spec vCenter(Locator locator, Number dist)
	{
		return new Spec().vCenter(locator, dist);
	}

	public static Spec vCenter(Locator locator, CheckProvider func)
	{
		return new Spec().vCenter(locator, func);
	}
	
}
