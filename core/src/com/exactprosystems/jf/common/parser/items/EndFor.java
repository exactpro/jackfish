////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.items;

import com.exactprosystems.jf.common.parser.Tokens;

@MatrixItemAttribute(
		description 	= "Closes the statement For.", 
		shouldContain 	= { Tokens.EndFor },
		mayContain 		= { }, 
		closes			= For.class,
		real			= false,
		hasValue 		= false, 
		hasParameters 	= false, 
        hasChildren 	= false
	)
public class EndFor extends MatrixItem
{
}
