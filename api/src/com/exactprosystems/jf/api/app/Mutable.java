////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

/**
 * A interface for any changeable objects
 */
public interface Mutable
{
	/**
	 * @return true, if a object will changed. This method should check all important fields of the object
	 */
	boolean 		isChanged();
	/**
	 * Notify, that all changes for the object were saved
	 */
	void			saved();
}
