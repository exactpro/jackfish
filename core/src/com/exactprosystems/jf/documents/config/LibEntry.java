////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Deprecated
@XmlAccessorType(XmlAccessType.NONE)
public class LibEntry extends Entry
{
	@XmlElement(name = Configuration.libPath)
	protected String libPathValue;

	@Override
	public String get(String name) throws Exception
	{
		switch (name)
		{
			case Configuration.libPath: 	return this.libPathValue;
		}
		return null;
	}

	@Override
	public void set(String name, Object value) throws Exception
	{
		switch (name)
		{
			case Configuration.libPath: 	this.libPathValue  = "" + value;	return;
		}
	}
}