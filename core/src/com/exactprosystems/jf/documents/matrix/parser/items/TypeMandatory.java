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

import com.exactprosystems.jf.documents.matrix.parser.Parser;

public enum TypeMandatory
{
	System(Parser.systemPrefix),
	Mandatory(Parser.knownPrefix),
	NotMandatory(Parser.knownPrefix),
	Extra("");

	private String prefix;

	TypeMandatory(String prefix)
	{
		this.prefix = prefix;
	}

	public String getPrefix()
	{
		return this.prefix;
	}
}
