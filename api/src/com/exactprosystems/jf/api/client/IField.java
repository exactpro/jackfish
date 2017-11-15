////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
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
	IField				getReference();
	String				getId();
	IType				getType();
	String				getDefaultvalue();
	boolean				isRequired();
	boolean				isCollection();
}
