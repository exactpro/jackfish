////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
