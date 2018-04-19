/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.client;

import java.util.List;

public interface IField
{
	
	String				getDescription();
	IAttribute			getAttribute(String name);
	List<IAttribute>	getAttributes();
	IAttribute			getValue(String name);
	List<IAttribute>	getValues();
	String				getName();
	IField				getReference();
	String				getId();
	IType				getType();
	String				getDefaultvalue();
	boolean				isRequired();
	boolean				isCollection();
}
