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
import com.exactprosystems.jf.api.common.i18n.R;

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
	@DescriptionAttribute(text = R.DOSPEC_TEXT)
	public Spec text(@FieldParameter(name = "text") String text)
	{
		this.list.add(new Piece(PieceKind.TEXT).setText(text));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_COLOR)
	public Spec color(@FieldParameter(name = "color") Color color)
	{
		this.list.add(new Piece(PieceKind.COLOR).setColor(color));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_DBCOLOR)
	public Spec backColor(@FieldParameter(name = "color") Color color)
	{
		this.list.add(new Piece(PieceKind.BACK_COLOR).setColor(color));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_ATTR)
	public Spec attr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value)
	{
		this.list.add(new Piece(PieceKind.ATTR).setText(name).setText2(value));
		return this;
	}
	//endregion
	
	//region visible
	@DescriptionAttribute(text = R.DOSPEC_VISIBLE)
	public Spec visible()
	{
		this.list.add(new Piece(PieceKind.VISIBLE));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INVISIBLE)
	public Spec invisible()
	{
		this.list.add(new Piece(PieceKind.INVISIBLE));
		return this;
	}
	//endregion

	//region count
	@DescriptionAttribute(text = R.DOSPEC_COUNT)
	public Spec count(@FieldParameter(name = "count") Number count)
	{
		this.list.add(new Piece(PieceKind.COUNT).setRange(Range.EQUAL).setA(count.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_COUNT_WITH_PROVIDER)
	public Spec count(@FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.COUNT));
		return this;
	}
	//endregion
	
	//region width
	@DescriptionAttribute(text = R.DOSPEC_WIDTH)
	public Spec width(@FieldParameter(name = "width") Number dist)
	{
		this.list.add(new Piece(PieceKind.WIDTH).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_WIDTH_WITH_PROVIDER)
	public Spec width(@FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.WIDTH));
		return this;
	}
	//endregion
	
	//region height
	@DescriptionAttribute(text = R.DOSPEC_HEIGHT)
	public Spec height(@FieldParameter(name="height") Number dist)
	{
		this.list.add(new Piece(PieceKind.HEIGHT).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_HEIGHT_WITH_PROVIDER)
	public Spec height(@FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.HEIGHT));
		return this;
	}
	//endregion

	//region contains
	@DescriptionAttribute(text = R.DOSPEC_CONTAINS)
	public Spec contains(@FieldParameter(name = "another") String another)
	{
		this.list.add(new Piece(PieceKind.CONTAINS).setName(another));
		return this;
	}
	//endregion

	//region left
	@DescriptionAttribute(text = R.DOSPEC_LEFT_ANOTHER)
	public Spec left(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_LEFT_ANOTHER_WITH_PROVIDER)
	public Spec left(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_LEFT_LOCATOR)
	public Spec left(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_LEFT_LOCATOR_WITH_PROVIDER)
	public Spec left(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.LEFT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region right
	@DescriptionAttribute(text = R.DOSPEC_RIGHT_ANOTHER)
	public Spec right(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_RIGHT_ANOTHER_WITH_PROVIDER)
	public Spec right(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_RIGHT_LOCATOR)
	public Spec right(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_RIGHT_LOCATOR_WITH_PROVIDER)
	public Spec right(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.RIGHT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region top
	@DescriptionAttribute(text = R.DOSPEC_TOP_ANOTHER)
	public Spec top(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_TOP_ANOTHER_WITH_PROVIDER)
	public Spec top(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_TOP_LOCATOR)
	public Spec top(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_TOP_LOCATOR_WITH_PROVIDER)
	public Spec top(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.TOP).setLocator(null, locator));
		return this;
	}
	//endregion

	//region bottom
	@DescriptionAttribute(text = R.DOSPEC_BOTTOM_ANOTHER)
	public Spec bottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_BOTTOM_ANOTHER_WITH_PROVIDER)
	public Spec bottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_BOTTOM_LOCATOR)
	public Spec bottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_BOTTOM_LOCATOR_WITH_PROVIDER)
	public Spec bottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.BOTTOM).setLocator(null, locator));
		return this;
	}
	//endregion
	
	//region inLeft
	@DescriptionAttribute(text = R.DOSPEC_INSIDE_LEFT_ANOTHER)
	public Spec inLeft(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_LEFT_ANOTHER_WITH_PROVIDER)
	public Spec inLeft(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_LEFT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_LEFT_LOCATOR)
	public Spec inLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_LEFT_LOCATOR_WITH_PROVIDER)
	public Spec inLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_LEFT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region inRight
	@DescriptionAttribute(text = R.DOSPEC_INSIDE_RIGHT_ANOTHER)
	public Spec inRight(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_RIGHT_ANOTHER_WITH_PROVIDER)
	public Spec inRight(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_RIGHT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_RIGHT_LOCATOR)
	public Spec inRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_RIGHT_LOCATOR_WITH_PROVIDER)
	public Spec inRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_RIGHT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region inTop
	@DescriptionAttribute(text = R.DOSPEC_INSIDE_TOP_ANOTHER)
	public Spec inTop(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_TOP_ANOTHER_WITH_PROVIDER)
	public Spec inTop(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_TOP).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_TOP_LOCATOR)
	public Spec inTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_TOP_LOCATOR_WITH_PROVIDER)
	public Spec inTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_TOP).setLocator(null, locator));
		return this;
	}
	//endregion

	//region inBottom
	@DescriptionAttribute(text = R.DOSPEC_INSIDE_BOTTOM_ANOTHER)
	public Spec inBottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_BOTTOM_ANOTHER_WITH_PROVIDER)
	public Spec inBottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_BOTTOM).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_BOTTOM_LOCATOR)
	public Spec inBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.INSIDE_BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_BOTTOM_LOCATOR_WITH_PROVIDER)
	public Spec inBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.INSIDE_BOTTOM).setLocator(null, locator));
		return this;
	}
	//endregion

	//region onLeft
	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_LEFT_ANOTHER)
	public Spec onLeft(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_LEFT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_LEFT_ANOTHER_WITH_PROVIDER)
	public Spec onLeft(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_LEFT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_LEFT_LOCATOR)
	public Spec onLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_LEFT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_LEFT_LOCATOR_WITH_PROVIDER)
	public Spec onLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_LEFT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region onRight
	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_RIGHT_ANOTHER)
	public Spec onRight(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_RIGHT).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_RIGHT_ANOTHER_WITH_PROVIDER)
	public Spec onRight(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_RIGHT).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_RIGHT_LOCATOR)
	public Spec onRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_RIGHT).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_RIGHT_LOCATOR_WITH_PROVIDER)
	public Spec onRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_RIGHT).setLocator(null, locator));
		return this;
	}
	//endregion

	//region onTop
	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_TOP_ANOTHER)
	public Spec onTop(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_TOP).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_TOP_ANOTHER_WITH_PROVIDER)
	public Spec onTop(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_TOP).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_TOP_LOCATOR)
	public Spec onTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_TOP).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_TOP_LOCATOR_WITH_PROVIDER)
	public Spec onTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_TOP).setLocator(null, locator));
		return this;
	}
	//endregion

	//region onBottom
	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_BOTTOM_ANOTHER)
	public Spec onBottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_BOTTOM).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_BOTTOM_ANOTHER_WITH_PROVIDER)
	public Spec onBottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.ON_BOTTOM).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_BOTTOM_LOCATOR)
	public Spec onBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.ON_BOTTOM).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_BOTTOM_LOCATOR_WITH_PROVIDER)
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
	@DescriptionAttribute(text = R.DOSPEC_H_CENTRE_ANOTHER)
	public Spec hCenter(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.HORIZONTAL_CENTERED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_H_CENTRE_ANOTHER_WITH_PROVIDER)
	public Spec hCenter(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.HORIZONTAL_CENTERED).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_H_CENTRE_LOCATOR)
	public Spec hCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.HORIZONTAL_CENTERED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_H_CENTRE_LOCATOR_WITH_PROVIDER)
	public Spec hCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.HORIZONTAL_CENTERED).setLocator(null, locator));
		return this;
	}
	//endregion
	
	//region vCenter
	@DescriptionAttribute(text = R.DOSPEC_V_CENTRE_ANOTHER)
	public Spec vCenter(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.VERTICAL_CENTERED).setName(another).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_V_CENTRE_ANOTHER_WITH_PROVIDER)
	public Spec vCenter(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.VERTICAL_CENTERED).setName(another));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_V_CENTRE_LOCATOR)
	public Spec vCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		this.list.add(new Piece(PieceKind.VERTICAL_CENTERED).setLocator(null, locator).setRange(Range.EQUAL).setA(dist.longValue()));
		return this;
	}

	@DescriptionAttribute(text = R.DOSPEC_V_CENTRE_LOCATOR_WITH_PROVIDER)
	public Spec vCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		this.list.add(func.provide(PieceKind.VERTICAL_CENTERED).setLocator(null, locator));
		return this;
	}
	//endregion

	protected List<Piece> list;

	private static final long	serialVersionUID	= -9155953771178401088L;
}