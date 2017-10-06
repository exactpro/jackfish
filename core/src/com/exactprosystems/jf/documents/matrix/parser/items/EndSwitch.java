////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.documents.matrix.parser.Tokens;

@MatrixItemAttribute(
		description 	= "Marks end of switch statement.", 
		shouldContain 	= { Tokens.EndSwitch }, 
		mayContain 		= {}, 
		closes 			= Switch.class,
		real			= false,
		hasValue 		= false, 
		hasParameters 	= false, 
		hasChildren		= false
		)
public class EndSwitch extends MatrixItem
{
	@Override
	protected MatrixItem makeCopy()
	{
		return new EndSwitch();
	}
}
