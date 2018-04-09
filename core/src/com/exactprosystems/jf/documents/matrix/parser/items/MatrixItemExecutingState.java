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
 * Executing state enum for a item
 * Use this enum for indicate process on a GUI
 */
public enum MatrixItemExecutingState
{
	None,
	/**
	 * Used for display, that a item executing was failed
 	 */
	Failed,
	/**
	 * Used for display, that a item executing was passed
	 */
	Passed,
	/**
	 * Used for display, that a item is executing now
	 */
	Executing
}
