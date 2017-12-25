////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.FieldParameter;

import java.awt.Color;
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
			sb.append(piece.toString());
		}
		return sb.toString();
	}
	
	public void tune(IWindow window, ITemplateEvaluator templateEvaluator) throws Exception
	{
		for (Piece piece : this.list)
		{
			piece.tune(window, templateEvaluator);
		}
	}
	
	public <T> CheckingLayoutResult perform(OperationExecutor<T> executor, Locator owner, Locator locator) throws Exception
	{
		CheckingLayoutResult result = new CheckingLayoutResult();
		List<T> self = executor.findAll(owner, locator);
		
		for (Piece check : this.list)
		{
			check.kind.perform(check, executor, self, locator, result);
		}
		
		return result;
	}

	//region attributes
	@DescriptionAttribute(text = DoSpec.text)
	public Spec text(@FieldParameter(name = "text") String text)
	{
		this.list.add(new Piece(PieceKind.TEXT).setText(text));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.color)
	public Spec color(@FieldParameter(name = "color") Color color)
	{
		this.list.add(new Piece(PieceKind.COLOR).setColor(color));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.bgColor)
	public Spec backColor(@FieldParameter(name = "color") Color color)
	{
		this.list.add(new Piece(PieceKind.BACK_COLOR).setColor(color));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.attr)
	public Spec attr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value)
	{
		this.list.add(new Piece(PieceKind.ATTR).setText(name).setText2(value));
		return this;
	}
	//endregion
	
	//region visible
	@DescriptionAttribute(text = DoSpec.visible)
	public Spec visible()
	{
		this.list.add(new Piece(PieceKind.VISIBLE));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.invisible)
	public Spec invisible()
	{
		this.list.add(new Piece(PieceKind.INVISIBLE));
		return this;
	}
	//endregion

	//region count
	@DescriptionAttribute(text = DoSpec.count)
	public Spec count(@FieldParameter(name = "count") Number count)
	{
		this.list.add(new Piece(PieceKind.COUNT).setRange(Range.EQUAL).setA(count.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.countWithProvider)
	public Spec count(@FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.COUNT));
		return this;
	}
	//endregion
	
	//region width
	@DescriptionAttribute(text = DoSpec.width)
	public Spec width(@FieldParameter(name = "width") Number dist)
	{
		this.list.add(new Piece(PieceKind.WIDTH).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.widthWithProvider)
	public Spec width(@FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.WIDTH));
		return this;
	}
	//endregion
	
	//region height
	@DescriptionAttribute(text = DoSpec.height)
	public Spec height(@FieldParameter(name="height") Number dist)
	{
		this.list.add(new Piece(PieceKind.HEIGHT).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.heightWithProvider)
	public Spec height(@FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.HEIGHT));
		return this;
	}
	//endregion

	//region contains
	@DescriptionAttribute(text = DoSpec.contains)
	public Spec contains(@FieldParameter(name = "another") String another)
	{
		this.list.add(new Piece(PieceKind.CONTAINS).setName(another));
		return this;
	}
	//endregion

	//region left
	@DescriptionAttribute(text = DoSpec.leftAnother)
	public Spec left(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.leftAnotherWithProvider)
	public Spec left(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.leftLocator)
	public Spec left(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.leftLocatorWithProvider)
	public Spec left(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region right
	@DescriptionAttribute(text = DoSpec.rightAnother)
	public Spec right(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.rightAnotherWithProvider)
	public Spec right(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.rightLocator)
	public Spec right(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.rightLocatorWithProvider)
	public Spec right(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region top
	@DescriptionAttribute(text = DoSpec.topAnother)
	public Spec top(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.topAnotherWithProvider)
	public Spec top(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.topLocator)
	public Spec top(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.topLocatorWithProvider)
	public Spec top(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP).setLocator(null, locator));
		return this;
	}
	//endregion

	//region bottom
	@DescriptionAttribute(text = DoSpec.bottomAnother)
	public Spec bottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.bottomAnotherWithProvider)
	public Spec bottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.bottomLocator)
	public Spec bottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.bottomLocatorWithProvider)
	public Spec bottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM).setLocator(null, locator));
		return this;
	}
	//endregion
	
	//region inLeft
	@DescriptionAttribute(text = DoSpec.insideLeftAnother)
	public Spec inLeft(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideLeftAnotherWithProvider)
	public Spec inLeft(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_LEFT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideLeftLocator)
	public Spec inLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideLeftLocatorWithProvider)
	public Spec inLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_LEFT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region inRight
	@DescriptionAttribute(text = DoSpec.insideRightAnother)
	public Spec inRight(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideRightAnotherWithProvider)
	public Spec inRight(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_RIGHT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideRightLocator)
	public Spec inRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideRightLocatorWithProvider)
	public Spec inRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_RIGHT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region inTop
	@DescriptionAttribute(text = DoSpec.insideTopAnother)
	public Spec inTop(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideTopAnotherWithProvider)
	public Spec inTop(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_TOP).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideTopLocator)
	public Spec inTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideTopLocatorWithProvider)
	public Spec inTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_TOP).setLocator(null, locator));
		return this;
	}
	//endregion

	//region inBottom
	@DescriptionAttribute(text = DoSpec.insideBottomAnother)
	public Spec inBottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideBottomAnotherWithProvider)
	public Spec inBottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_BOTTOM).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideBottomLocator)
	public Spec inBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.insideBottomLocatorWithProvider)
	public Spec inBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_BOTTOM).setLocator(null, locator));
		return this;
	}
	//endregion

	//region onLeft
	@DescriptionAttribute(text = DoSpec.outsideLeftAnother)
	public Spec onLeft(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideLeftAnotherWithProvider)
	public Spec onLeft(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_LEFT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideLeftLocator)
	public Spec onLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideLeftLocatorWithProvider)
	public Spec onLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_LEFT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region onRight
	@DescriptionAttribute(text = DoSpec.outsideRightAnother)
	public Spec onRight(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideRightAnotherWithProvider)
	public Spec onRight(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_RIGHT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideRightLocator)
	public Spec onRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideRightLocatorWithProvider)
	public Spec onRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_RIGHT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region onTop
	@DescriptionAttribute(text = DoSpec.outsideTopAnother)
	public Spec onTop(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideTopAnotherWithProvider)
	public Spec onTop(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_TOP).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideTopLocator)
	public Spec onTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideTopLocatorWithProvider)
	public Spec onTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_TOP).setLocator(null, locator));
		return this;
	}
	//endregion

	//region onBottom
	@DescriptionAttribute(text = DoSpec.outsideBottomAnother)
	public Spec onBottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideBottomAnotherWithProvider)
	public Spec onBottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_BOTTOM).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideBottomLocator)
	public Spec onBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.outsideBottomLocatorWithProvider)
	public Spec onBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_BOTTOM).setLocator(null, locator));
		return this;
	}
	//endregion
	
	//------------------------------------------------------------------------------------------------------------------------------
	// lAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec lAlign(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec lAlign(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT_ALIGNED).setName(another));
		return this;
	}

	public Spec lAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec lAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT_ALIGNED).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// rAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec rAlign(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec rAlign(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT_ALIGNED).setName(another));
		return this;
	}

	public Spec rAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec rAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT_ALIGNED).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// tAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec tAlign(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec tAlign(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP_ALIGNED).setName(another));
		return this;
	}

	public Spec tAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec tAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP_ALIGNED).setLocator(null, locator));
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// bAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public Spec bAlign(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM_ALIGNED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec bAlign(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM_ALIGNED).setName(another));
		return this;
	}

	public Spec bAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM_ALIGNED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	public Spec bAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM_ALIGNED).setLocator(null, locator));
		return this;
	}
	
	//region hCenter
	@DescriptionAttribute(text = DoSpec.hCenterAnother)
	public Spec hCenter(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.HORIZONTAL_CENTERED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.hCenterAnotherWithProvider)
	public Spec hCenter(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.HORIZONTAL_CENTERED).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.hCenterLocator)
	public Spec hCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.HORIZONTAL_CENTERED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.hCenterLocatorWithProvider)
	public Spec hCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.HORIZONTAL_CENTERED).setLocator(null, locator));
		return this;
	}
	//endregion
	
	//region vCenter
	@DescriptionAttribute(text = DoSpec.vCenterAnother)
	public Spec vCenter(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.VERTICAL_CENTERED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.vCenterAnotherWithProvider)
	public Spec vCenter(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.VERTICAL_CENTERED).setName(another));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.vCenterLocator)
	public Spec vCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.VERTICAL_CENTERED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = DoSpec.vCenterLocatorWithProvider)
	public Spec vCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.VERTICAL_CENTERED).setLocator(null, locator));
		return this;
	}
	//endregion

	protected List<Piece> list;

	private static final long	serialVersionUID	= -9155953771178401088L;
}