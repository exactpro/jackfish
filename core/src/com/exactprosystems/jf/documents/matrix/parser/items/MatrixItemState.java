/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items;

/**
 * A state enum for a item
 * Use this enum for indicate icon on a GUI
 */
public enum MatrixItemState
{
	/**
	 * This enum used for no icon
	 */
	None,
	/**
	 * This enum used for display, that a item is on breakpoint
	 */
	BreakPoint,
	/**
	 * This enum used for display, that a item is executing now
	 */
	Executing,
	/**
	 * This enum used for a item, which are executing now and on breakpoint
	 */
	ExecutingWithBreakPoint,
	/**
	 * This enum used for a parent item, which executing now
	 */
	ExecutingParent,
}
