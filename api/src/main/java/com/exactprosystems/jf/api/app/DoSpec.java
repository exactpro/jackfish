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

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.FieldParameter;
import com.exactprosystems.jf.api.common.i18n.R;

import java.awt.*;

public class DoSpec
{
	//region CheckProvider functions

	@DescriptionAttribute(text = R.DOSPEC_LESS)
	public static  CheckProvider less(@FieldParameter(name = "n") Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.LESS).setA(n.longValue()); 
	}

	@DescriptionAttribute(text = R.DOSPEC_GREAT)
	public static CheckProvider great(@FieldParameter(name = "n") Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.GREAT).setA(n.longValue()); 
	}

	@DescriptionAttribute(text = R.DOSPEC_ABOUT)
	public static CheckProvider about(@FieldParameter(name = "n") Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.ABOUT).setA(n.longValue()); 
	}

	@DescriptionAttribute(text = R.DOSPEC_BETWEEN)
	public static CheckProvider between(@FieldParameter(name = "n") Number n, @FieldParameter(name = "n") Number m)
	{
		return  (kind) -> new Piece(kind).setRange(Range.BETWEEN).setA(n.longValue()).setB(m.longValue()); 
	}
	//endregion

	//region attributes

	@DescriptionAttribute(text = R.DOSPEC_TEXT)
	public static Spec text(@FieldParameter(name = "text") String text)
	{
		return new Spec().text(text);
	}

	@DescriptionAttribute(text = R.DOSPEC_COLOR)
	public static Spec color(@FieldParameter(name = "color") Color color)
	{
		return new Spec().color(color);
	}

	@DescriptionAttribute(text = R.DOSPEC_DBCOLOR)
	public static Spec backColor(@FieldParameter(name = "color") Color color)
	{
		return new Spec().backColor(color);
	}

	@DescriptionAttribute(text = R.DOSPEC_ATTR)
	public static Spec attr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value)
	{
		return new Spec().attr(name, value);
	}

	//endregion

	//region visible

	@DescriptionAttribute(text = R.DOSPEC_VISIBLE)
	public static Spec visible()
	{
		return new Spec().visible();
	}

	@DescriptionAttribute(text = R.DOSPEC_INVISIBLE)
	public static Spec invisible()
	{
		return new Spec().invisible();
	}

	//endregion

	//region count
	@DescriptionAttribute(text = R.DOSPEC_COUNT)
	public static Spec count(@FieldParameter(name = "count") Number count)
	{
		return new Spec().count(count);
	}

	@DescriptionAttribute(text = R.DOSPEC_COUNT_WITH_PROVIDER)
	public static Spec count(@FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().count(func);
	}
	//endregion

	//region width
	@DescriptionAttribute(text = R.DOSPEC_WIDTH)
	public static Spec width(@FieldParameter(name = "width") Number dist)
	{
		return new Spec().width(dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_WIDTH_WITH_PROVIDER)
	public static Spec width(@FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().width(func);
	}
	//endregion
	
	//region height
	@DescriptionAttribute(text = R.DOSPEC_HEIGHT)
	public static Spec height(@FieldParameter(name="height") Number dist)
	{
		return new Spec().height(dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_HEIGHT_WITH_PROVIDER)
	public static Spec height(@FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().height(func);
	}
	//endregion
	
	//region contains
	@DescriptionAttribute(text = R.DOSPEC_CONTAINS)
	public static Spec contains(@FieldParameter(name = "another") String another)
	{
		return new Spec().contains(another);
	}
	//endregion

	@DescriptionAttribute(text = R.DOSPEC_LEFT_ANOTHER)
	public static Spec left(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().left(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_LEFT_ANOTHER_WITH_PROVIDER)
	public static Spec left(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().left(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_LEFT_LOCATOR)
	public static Spec left(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().left(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_LEFT_LOCATOR_WITH_PROVIDER)
	public static Spec left(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().left(locator, func);
	}
	//endregion

	//region right

	@DescriptionAttribute(text = R.DOSPEC_RIGHT_ANOTHER)
	public static Spec right(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().right(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_RIGHT_ANOTHER_WITH_PROVIDER)
	public static Spec right(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().right(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_RIGHT_LOCATOR)
	public static Spec right(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().right(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_RIGHT_LOCATOR_WITH_PROVIDER)
	public static Spec right(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().right(locator, func);
	}
	//endregion

	//region top

	@DescriptionAttribute(text = R.DOSPEC_TOP_ANOTHER)
	public static Spec top(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().top(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_TOP_ANOTHER_WITH_PROVIDER)
	public static Spec top(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().top(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_TOP_LOCATOR)
	public static Spec top(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().top(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_TOP_LOCATOR_WITH_PROVIDER)
	public static Spec top(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().top(locator, func);
	}
	//endregion

	//region bottom

	@DescriptionAttribute(text = R.DOSPEC_BOTTOM_ANOTHER)
	public static Spec bottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().bottom(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_BOTTOM_ANOTHER_WITH_PROVIDER)
	public static Spec bottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().bottom(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_BOTTOM_LOCATOR)
	public static Spec bottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().bottom(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_BOTTOM_LOCATOR_WITH_PROVIDER)
	public static Spec bottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().bottom(locator, func);
	}
	//endregion

	//region inLeft
	@DescriptionAttribute(text = R.DOSPEC_INSIDE_LEFT_ANOTHER)
	public static Spec inLeft(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inLeft(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_LEFT_ANOTHER_WITH_PROVIDER)
	public static Spec inLeft(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inLeft(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_LEFT_LOCATOR)
	public static Spec inLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inLeft(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_LEFT_LOCATOR_WITH_PROVIDER)
	public static Spec inLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inLeft(locator, func);
	}
	//endregion

	//region inRight
	@DescriptionAttribute(text = R.DOSPEC_INSIDE_RIGHT_ANOTHER)
	public static Spec inRight(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inRight(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_RIGHT_ANOTHER_WITH_PROVIDER)
	public static Spec inRight(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inRight(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_RIGHT_LOCATOR)
	public static Spec inRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inRight(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_RIGHT_LOCATOR_WITH_PROVIDER)
	public static Spec inRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inRight(locator, func);
	}
	//endregion

	//region inTop

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_TOP_ANOTHER)
	public static Spec inTop(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inTop(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_TOP_ANOTHER_WITH_PROVIDER)
	public static Spec inTop(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inTop(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_TOP_LOCATOR)
	public static Spec inTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inTop(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_TOP_LOCATOR_WITH_PROVIDER)
	public static Spec inTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inTop(locator, func);
	}
	//endregion

	//region inBottom

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_BOTTOM_ANOTHER)
	public static Spec inBottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inBottom(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_BOTTOM_ANOTHER_WITH_PROVIDER)
	public static Spec inBottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inBottom(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_BOTTOM_LOCATOR)
	public static Spec inBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inBottom(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_INSIDE_BOTTOM_LOCATOR_WITH_PROVIDER)
	public static Spec inBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inBottom(locator, func);
	}
	//endregion

	//region onLeft
	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_LEFT_ANOTHER)
	public static Spec onLeft(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onLeft(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_LEFT_ANOTHER_WITH_PROVIDER)
	public static Spec onLeft(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onLeft(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_LEFT_LOCATOR)
	public static Spec onLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onLeft(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_LEFT_LOCATOR_WITH_PROVIDER)
	public static Spec onLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onLeft(locator, func);
	}
	//endregion

	//region onRight
	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_RIGHT_ANOTHER)
	public static Spec onRight(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onRight(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_RIGHT_ANOTHER_WITH_PROVIDER)
	public static Spec onRight(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onRight(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_RIGHT_LOCATOR)
	public static Spec onRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onRight(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_RIGHT_LOCATOR_WITH_PROVIDER)
	public static Spec onRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onRight(locator, func);
	}
	//endregion

	//region onTop
	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_TOP_ANOTHER)
	public static Spec onTop(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onTop(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_TOP_ANOTHER_WITH_PROVIDER)
	public static Spec onTop(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onTop(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_TOP_LOCATOR)
	public static Spec onTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onTop(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_TOP_LOCATOR_WITH_PROVIDER)
	public static Spec onTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onTop(locator, func);
	}
	//endregion

	//region onBottom
	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_BOTTOM_ANOTHER)
	public static Spec onBottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onBottom(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_BOTTOM_ANOTHER_WITH_PROVIDER)
	public static Spec onBottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onBottom(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_BOTTOM_LOCATOR)
	public static Spec onBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onBottom(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_OUTSIDE_BOTTOM_LOCATOR_WITH_PROVIDER)
	public static Spec onBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onBottom(locator, func);
	}
	//endregion
	
	//------------------------------------------------------------------------------------------------------------------------------
	// lAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec lAlign(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().lAlign(another, dist);
	}

	public static Spec lAlign(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().lAlign(another, func);
	}

	public static Spec lAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().lAlign(locator, dist);
	}

	public static Spec lAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().lAlign(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// rAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec rAlign(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().rAlign(another, dist);
	}

	public static Spec rAlign(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().rAlign(another, func);
	}

	public static Spec rAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().rAlign(locator, dist);
	}

	public static Spec rAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().rAlign(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// tAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec tAlign(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().tAlign(another, dist);
	}

	public static Spec tAlign(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().tAlign(another, func);
	}

	public static Spec tAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().tAlign(locator, dist);
	}

	public static Spec tAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().tAlign(locator, func);
	}

	//------------------------------------------------------------------------------------------------------------------------------
	// bAlign
	//------------------------------------------------------------------------------------------------------------------------------
	public static Spec bAlign(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().bAlign(another, dist);
	}

	public static Spec bAlign(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().bAlign(another, func);
	}

	public static Spec bAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().bAlign(locator, dist);
	}

	public static Spec bAlign(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().bAlign(locator, func);
	}


	//region hCenter

	@DescriptionAttribute(text = R.DOSPEC_H_CENTRE_ANOTHER)
	public static Spec hCenter(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().hCenter(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_H_CENTRE_ANOTHER_WITH_PROVIDER)
	public static Spec hCenter(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().hCenter(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_H_CENTRE_LOCATOR)
	public static Spec hCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().hCenter(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_H_CENTRE_LOCATOR_WITH_PROVIDER)
	public static Spec hCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().hCenter(locator, func);
	}
	//endregion

	//region vCenter

	@DescriptionAttribute(text = R.DOSPEC_V_CENTRE_ANOTHER)
	public static Spec vCenter(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().vCenter(another, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_V_CENTRE_ANOTHER_WITH_PROVIDER)
	public static Spec vCenter(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().vCenter(another, func);
	}

	@DescriptionAttribute(text = R.DOSPEC_V_CENTRE_LOCATOR)
	public static Spec vCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().vCenter(locator, dist);
	}

	@DescriptionAttribute(text = R.DOSPEC_V_CENTRE_LOCATOR_WITH_PROVIDER)
	public static Spec vCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().vCenter(locator, func);
	}
	//endregion
	
}
