/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.functions;

public enum HelpKind
{ 
	String				(""),
	Number				(""),
	Boolean				(""),
	Expression			(""),
	ChooseSaveFile		("…"),
	ChooseOpenFile		("…"),
	ChooseFolder		("…"),
	ChooseDateTime		("D"),
	ChooseFromList		("≡"),
	BuildQuery			("S"),
	BuildXPath			("X"),
	;
	private String label;

	HelpKind (String label)
	{
		this.label = label;
	}
	
	public String getLabel()
	{
		return this.label;
	}
}
