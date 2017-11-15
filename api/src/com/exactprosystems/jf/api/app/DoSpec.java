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

import java.awt.*;

public class DoSpec
{
	//region CheckProvider functions
	private static final String checkProviderSeeMethods = "See DoSpec.less(number), DoSpec.great(number), DoSpec.about(number), DoSpec.between(number,number)";

	@DescriptionAttribute(text = "Check, that a number is less than passed number @n")
	public static  CheckProvider less(@FieldParameter(name = "n") Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.LESS).setA(n.longValue()); 
	}

	@DescriptionAttribute(text = "Check, that a number is great than passed number @n")
	public static CheckProvider great(@FieldParameter(name = "n") Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.GREAT).setA(n.longValue()); 
	}

	@DescriptionAttribute(text = "Check, that a number about passed number @n. The error is 10%")
	public static CheckProvider about(@FieldParameter(name = "n") Number n)
	{
		return  (kind) -> new Piece(kind).setRange(Range.ABOUT).setA(n.longValue()); 
	}

	@DescriptionAttribute(text = "Check, that a number is in the range from min(@n,@m) to max(@n, @m)")
	public static CheckProvider between(@FieldParameter(name = "n") Number n, @FieldParameter(name = "n") Number m)
	{
		return  (kind) -> new Piece(kind).setRange(Range.BETWEEN).setA(n.longValue()).setB(m.longValue()); 
	}
	//endregion

	//region attributes

	static final String text = "Check, that text @text equals text of the control";
	@DescriptionAttribute(text = DoSpec.text)
	public static Spec text(@FieldParameter(name = "text") String text)
	{
		return new Spec().text(text);
	}

	static final String color = "Check, that color @color equals color of the control";
	@DescriptionAttribute(text = DoSpec.color)
	public static Spec color(@FieldParameter(name = "color") Color color)
	{
		return new Spec().color(color);
	}

	static final String bgColor = "Check, that color @color equals background color of the control";
	@DescriptionAttribute(text = DoSpec.bgColor)
	public static Spec backColor(@FieldParameter(name = "color") Color color)
	{
		return new Spec().backColor(color);
	}

	static final String attr = "Check, that attribute @name of the control has value @value";
	@DescriptionAttribute(text = DoSpec.color)
	public static Spec attr(@FieldParameter(name = "name") String name, @FieldParameter(name = "value") String value)
	{
		return new Spec().attr(name, value);
	}

	//endregion

	//region visible

	static final String visible = "Check, that the control is visible";
	@DescriptionAttribute(text = DoSpec.visible)
	public static Spec visible()
	{
		return new Spec().visible();
	}

	static final String invisible = "Check, that the control is invisible";
	@DescriptionAttribute(text = DoSpec.invisible)
	public static Spec invisible()
	{
		return new Spec().invisible();
	}

	//endregion

	//region count
	static final String count = "Check, that found @count items of the control";
	@DescriptionAttribute(text = DoSpec.count)
	public static Spec count(@FieldParameter(name = "count") Number count)
	{
		return new Spec().count(count);
	}

	static  final String countWithProvider = "Check, that a count of find items to the control is satisfy by passed CheckProvider @func.\n" + checkProviderSeeMethods;
	@DescriptionAttribute(text = DoSpec.countWithProvider)
	public static Spec count(@FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().count(func);
	}
	//endregion

	//region width
	static final String width = "Check, that current control has width @width";
	@DescriptionAttribute(text = DoSpec.width)
	public static Spec width(@FieldParameter(name = "width") Number dist)
	{
		return new Spec().width(dist);
	}

	static final String widthWithProvider = "Check, that a width of the control is satisfy by passed CheckProvider @func.\n" + checkProviderSeeMethods;
	@DescriptionAttribute(text = DoSpec.widthWithProvider)
	public static Spec width(@FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().width(func);
	}
	//endregion
	
	//region height
	static final String height = "Check, that current control has a height @height";
	@DescriptionAttribute(text = DoSpec.height)
	public static Spec height(@FieldParameter(name="height") Number dist)
	{
		return new Spec().height(dist);
	}

	static final String heightWithProvider = "Check, that a height of the control is satisfy by passed CheckProvider @func.\n" + checkProviderSeeMethods;
	@DescriptionAttribute(text = DoSpec.heightWithProvider)
	public static Spec height(@FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().height(func);
	}
	//endregion
	
	//region contains
	static final String contains = "Check, that current control fully contains inside on self a element with id @another from a dictionary";
	@DescriptionAttribute(text = DoSpec.contains)
	public static Spec contains(@FieldParameter(name = "another") String another)
	{
		return new Spec().contains(another);
	}
	//endregion

	private static final String checkThat = "Check, that the control ";
	private static final String another = " from a control with id @another from a dictionary.";
	private static final String locator = " from a dynamic control with locator @locator.";

	private static final String atADistance  = " at a distance @dist";
	private static final String atADistanceWithProvider = " at distance, evaluating via CheckProvider @func";

	//region left
	private static final String leftDistance = " on the left" + atADistance;
	private static final String leftProvider = " on the left" + atADistanceWithProvider;

	static final String leftAnother = checkThat + leftDistance + another;
	@DescriptionAttribute(text = DoSpec.leftAnother)
	public static Spec left(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().left(another, dist);
	}

	static final String leftAnotherWithProvider = checkThat + leftProvider + another + "\n" + checkProviderSeeMethods;
	@DescriptionAttribute(text = DoSpec.leftAnotherWithProvider)
	public static Spec left(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().left(another, func);
	}

	static final String leftLocator = checkThat + leftDistance + locator;
	@DescriptionAttribute(text = DoSpec.leftLocator)
	public static Spec left(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().left(locator, dist);
	}

	static final String leftLocatorWithProvider = checkThat + leftProvider + locator + "\n" + checkProviderSeeMethods;
	@DescriptionAttribute(text = DoSpec.leftLocatorWithProvider)
	public static Spec left(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().left(locator, func);
	}
	//endregion

	//region right
	private static final String rightDistance = " on the right" + atADistance;
	private static final String rightProvider = " on the right" + atADistanceWithProvider;

	static final String rightAnother = checkThat + rightDistance + another;
	@DescriptionAttribute(text = rightAnother)
	public static Spec right(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().right(another, dist);
	}

	static final String rightAnotherWithProvider = checkThat + rightProvider + another;
	@DescriptionAttribute(text = rightAnotherWithProvider)
	public static Spec right(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().right(another, func);
	}

	static final String rightLocator = checkThat + rightDistance + locator;
	@DescriptionAttribute(text = rightLocator)
	public static Spec right(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().right(locator, dist);
	}

	static final String rightLocatorWithProvider = checkThat + rightProvider + locator;
	@DescriptionAttribute(text = rightLocatorWithProvider)
	public static Spec right(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().right(locator, func);
	}
	//endregion

	//region top
	private static final String topDistance = " on the top" + atADistance;
	private static final String topProvider = " on the top" + atADistanceWithProvider;

	static final String topAnother = checkThat + topDistance + another;
	@DescriptionAttribute(text = topAnother)
	public static Spec top(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().top(another, dist);
	}

	static final String topAnotherWithProvider = checkThat + topProvider + another;
	@DescriptionAttribute(text = topAnotherWithProvider)
	public static Spec top(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().top(another, func);
	}

	static final String topLocator = checkThat + topDistance + locator;
	@DescriptionAttribute(text = topLocator)
	public static Spec top(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().top(locator, dist);
	}

	static final String topLocatorWithProvider = checkThat + topProvider + locator;
	@DescriptionAttribute(text = topLocatorWithProvider)
	public static Spec top(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().top(locator, func);
	}
	//endregion

	//region bottom
	private static final String bottomDistance = " on the bottom" + atADistance;
	private static final String bottomProvider = " on the bottom" + atADistanceWithProvider;

	static final String bottomAnother = checkThat + bottomDistance + another;
	@DescriptionAttribute(text = bottomAnother)
	public static Spec bottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().bottom(another, dist);
	}

	static final String bottomAnotherWithProvider = checkThat + bottomProvider + another;
	@DescriptionAttribute(text = bottomAnotherWithProvider)
	public static Spec bottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().bottom(another, func);
	}

	static final String bottomLocator = checkThat + bottomDistance + locator;
	@DescriptionAttribute(text = bottomLocator)
	public static Spec bottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().bottom(locator, dist);
	}

	static final String bottomLocatorWithProvider = checkThat + bottomProvider + locator;
	@DescriptionAttribute(text = bottomLocatorWithProvider)
	public static Spec bottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().bottom(locator, func);
	}
	//endregion

	private static final String leftBorders = " left borders ";
	private static final String rightBorders = "right borders";
	private static final String topBorders = "top borders";
	private static final String bottomBorders = "bottom borders";

	private static final String equalsDistance   = " equals value @dist.";
	private static final String providerDistance = " evaluating via CheckProvider @func.";


	private static final String insideFromAnother = " inside from a control with id @another from a dictionary and distance between ";
	private static final String insideFromLocator = " inside from a dynamic control with locator @locator and distance between ";

	//region inLeft
	static final String insideLeftAnother = checkThat + insideFromAnother + leftBorders + equalsDistance;
	@DescriptionAttribute(text = insideLeftAnother)
	public static Spec inLeft(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inLeft(another, dist);
	}

	static final String insideLeftAnotherWithProvider = checkThat + insideFromAnother + leftBorders + providerDistance;
	@DescriptionAttribute(text = insideLeftAnotherWithProvider)
	public static Spec inLeft(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inLeft(another, func);
	}

	static final String insideLeftLocator = checkThat + insideFromLocator + leftBorders + equalsDistance;
	@DescriptionAttribute(text = insideLeftLocator)
	public static Spec inLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inLeft(locator, dist);
	}

	static final String insideLeftLocatorWithProvider = checkThat + insideFromLocator + leftBorders + insideFromLocator;
	@DescriptionAttribute(text = insideLeftLocatorWithProvider)
	public static Spec inLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inLeft(locator, func);
	}
	//endregion

	//region inRight
	static final String insideRightAnother = checkThat + insideFromAnother + rightBorders + equalsDistance;
	@DescriptionAttribute(text = insideRightAnother)
	public static Spec inRight(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inRight(another, dist);
	}

	static final String insideRightAnotherWithProvider = checkThat + insideFromAnother + rightBorders + providerDistance;
	@DescriptionAttribute(text = insideRightAnotherWithProvider)
	public static Spec inRight(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inRight(another, func);
	}

	static final String insideRightLocator = checkThat + insideFromLocator + rightBorders + equalsDistance;
	@DescriptionAttribute(text = insideRightLocator)
	public static Spec inRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inRight(locator, dist);
	}

	static final String insideRightLocatorWithProvider = checkThat + insideFromLocator + rightBorders + providerDistance;
	@DescriptionAttribute(text = insideRightLocatorWithProvider)
	public static Spec inRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inRight(locator, func);
	}
	//endregion

	//region inTop

	static final String insideTopAnother = checkThat + insideFromAnother + topBorders + equalsDistance;
	@DescriptionAttribute(text = insideTopAnother)
	public static Spec inTop(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inTop(another, dist);
	}

	static final String insideTopAnotherWithProvider = checkThat + insideFromAnother + topBorders + providerDistance;
	@DescriptionAttribute(text = insideTopAnotherWithProvider)
	public static Spec inTop(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inTop(another, func);
	}

	static final String insideTopLocator = checkThat + insideFromLocator + topBorders + equalsDistance;
	@DescriptionAttribute(text = insideTopLocator)
	public static Spec inTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inTop(locator, dist);
	}

	static final String insideTopLocatorWithProvider = checkThat + insideFromLocator + topBorders + providerDistance;
	@DescriptionAttribute(text = insideTopLocatorWithProvider)
	public static Spec inTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inTop(locator, func);
	}
	//endregion

	//region inBottom

	static final String insideBottomAnother = checkThat + insideFromAnother + bottomBorders + equalsDistance;
	@DescriptionAttribute(text = insideBottomAnother)
	public static Spec inBottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inBottom(another, dist);
	}

	static final String insideBottomAnotherWithProvider = checkThat + insideFromAnother + bottomBorders + providerDistance;
	@DescriptionAttribute(text = insideBottomAnotherWithProvider)
	public static Spec inBottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inBottom(another, func);
	}

	static final String insideBottomLocator = checkThat + insideFromLocator + bottomBorders + equalsDistance;
	@DescriptionAttribute(text = insideBottomLocator)
	public static Spec inBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().inBottom(locator, dist);
	}

	static final String insideBottomLocatorWithProvider = checkThat + insideFromLocator + bottomBorders + providerDistance;
	@DescriptionAttribute(text = insideBottomLocatorWithProvider)
	public static Spec inBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().inBottom(locator, func);
	}
	//endregion

	private static final String outsideFromAnother = " outside from a control with id @another from a dictionary and distance between ";
	private static final String outsideFromLocator = " outside from a dynamic control with locator @locator and distance between ";

	//region onLeft
	static final String outsideLeftAnother = checkThat + outsideFromAnother + leftBorders + equalsDistance;
	@DescriptionAttribute(text = outsideLeftAnother)
	public static Spec onLeft(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onLeft(another, dist);
	}

	static final String outsideLeftAnotherWithProvider = checkThat + outsideFromAnother + leftBorders + providerDistance;
	@DescriptionAttribute(text = outsideLeftAnotherWithProvider)
	public static Spec onLeft(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onLeft(another, func);
	}

	static final String outsideLeftLocator = checkThat + outsideFromLocator + leftBorders + equalsDistance;
	@DescriptionAttribute(text = outsideLeftLocator)
	public static Spec onLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onLeft(locator, dist);
	}

	static final String outsideLeftLocatorWithProvider = checkThat + outsideFromLocator + leftBorders + outsideFromLocator;
	@DescriptionAttribute(text = outsideLeftLocatorWithProvider)
	public static Spec onLeft(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onLeft(locator, func);
	}
	//endregion

	//region onRight
	static final String outsideRightAnother = checkThat + outsideFromAnother + rightBorders + equalsDistance;
	@DescriptionAttribute(text = outsideRightAnother)
	public static Spec onRight(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onRight(another, dist);
	}

	static final String outsideRightAnotherWithProvider = checkThat + outsideFromAnother + rightBorders + providerDistance;
	@DescriptionAttribute(text = outsideRightAnotherWithProvider)
	public static Spec onRight(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onRight(another, func);
	}

	static final String outsideRightLocator = checkThat + outsideFromLocator + rightBorders + equalsDistance;
	@DescriptionAttribute(text = outsideRightLocator)
	public static Spec onRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onRight(locator, dist);
	}

	static final String outsideRightLocatorWithProvider = checkThat + outsideFromLocator + rightBorders + providerDistance;
	@DescriptionAttribute(text = outsideRightLocatorWithProvider)
	public static Spec onRight(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onRight(locator, func);
	}
	//endregion

	//region onTop
	static final String outsideTopAnother = checkThat + outsideFromAnother + topBorders + equalsDistance;
	@DescriptionAttribute(text = outsideTopAnother)
	public static Spec onTop(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onTop(another, dist);
	}

	static final String outsideTopAnotherWithProvider = checkThat + outsideFromAnother + topBorders + providerDistance;
	@DescriptionAttribute(text = outsideTopAnotherWithProvider)
	public static Spec onTop(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onTop(another, func);
	}

	static final String outsideTopLocator = checkThat + outsideFromLocator + topBorders + equalsDistance;
	@DescriptionAttribute(text = outsideTopLocator)
	public static Spec onTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onTop(locator, dist);
	}

	static final String outsideTopLocatorWithProvider = checkThat + outsideFromLocator + topBorders + providerDistance;
	@DescriptionAttribute(text = outsideTopLocatorWithProvider)
	public static Spec onTop(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onTop(locator, func);
	}
	//endregion

	//region onBottom
	static final String outsideBottomAnother = checkThat + outsideFromAnother + bottomBorders + equalsDistance;
	@DescriptionAttribute(text = outsideBottomAnother)
	public static Spec onBottom(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onBottom(another, dist);
	}

	static final String outsideBottomAnotherWithProvider = checkThat + outsideFromAnother + bottomBorders + providerDistance;
	@DescriptionAttribute(text = outsideBottomAnotherWithProvider)
	public static Spec onBottom(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().onBottom(another, func);
	}

	static final String outsideBottomLocator = checkThat + outsideFromLocator + bottomBorders + equalsDistance;
	@DescriptionAttribute(text = outsideBottomLocator)
	public static Spec onBottom(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().onBottom(locator, dist);
	}

	static final String outsideBottomLocatorWithProvider = checkThat + outsideFromLocator + bottomBorders + providerDistance;
	@DescriptionAttribute(text = outsideBottomLocatorWithProvider)
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

	private static final String anotherCentre   = " and a control with id @another from a dictionary are ";
	private static final String locatorCentre   = " and a dynamic control with locator @locator are ";
	private static final String centeredAndDiff = " centered and difference " ;

	//region hCenter
	private static final String horizontally    = "horizontally";

	static final String hCenterAnother  = checkThat + anotherCentre + horizontally + centeredAndDiff + equalsDistance;
	@DescriptionAttribute(text = hCenterAnother)
	public static Spec hCenter(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().hCenter(another, dist);
	}

	static final String hCenterAnotherWithProvider  = checkThat + anotherCentre + horizontally + centeredAndDiff + providerDistance;
	@DescriptionAttribute(text = hCenterAnotherWithProvider)
	public static Spec hCenter(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().hCenter(another, func);
	}

	static final String hCenterLocator  = checkThat + locatorCentre + horizontally + centeredAndDiff + equalsDistance;
	@DescriptionAttribute(text = hCenterLocator)
	public static Spec hCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().hCenter(locator, dist);
	}

	static final String hCenterLocatorWithProvider  = checkThat + locatorCentre + horizontally + centeredAndDiff + providerDistance;
	@DescriptionAttribute(text = hCenterLocatorWithProvider)
	public static Spec hCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().hCenter(locator, func);
	}
	//endregion

	//region vCenter
	private static final String vertically    = "vertically";

	static final String vCenterAnother  = checkThat + anotherCentre + vertically + centeredAndDiff + equalsDistance;
	@DescriptionAttribute(text = vCenterAnother)
	public static Spec vCenter(@FieldParameter(name="another") String another, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().vCenter(another, dist);
	}

	static final String vCenterAnotherWithProvider  = checkThat + anotherCentre + vertically + centeredAndDiff + providerDistance;
	@DescriptionAttribute(text = vCenterAnotherWithProvider)
	public static Spec vCenter(@FieldParameter(name="another") String another, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().vCenter(another, func);
	}

	static final String vCenterLocator  = checkThat + locatorCentre + vertically + centeredAndDiff + equalsDistance;
	@DescriptionAttribute(text = vCenterLocator)
	public static Spec vCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name="dist") Number dist)
	{
		return new Spec().vCenter(locator, dist);
	}

	static final String vCenterLocatorWithProvider  = checkThat + locatorCentre + vertically + centeredAndDiff + providerDistance;
	@DescriptionAttribute(text = vCenterLocatorWithProvider)
	public static Spec vCenter(@FieldParameter(name="locator") Locator locator, @FieldParameter(name = "func") CheckProvider func)
	{
		return new Spec().vCenter(locator, func);
	}
	//endregion
	
}
