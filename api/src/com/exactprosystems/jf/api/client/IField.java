////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
	Object				getReference();
	String				getId();
	IType				getType();
	String				getDefaultvalue();
	boolean				isRequired();
	boolean				isCollection();
}
