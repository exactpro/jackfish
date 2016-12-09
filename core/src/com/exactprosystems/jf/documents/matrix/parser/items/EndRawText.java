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
description 	= "Closes the statement Text.", 
shouldContain 	= { Tokens.EndRawText},
mayContain 		= { }, 
closes			= RawText.class,
real			= false,
hasValue 		= false, 
hasParameters 	= false,
hasChildren 	= false
)
public class EndRawText extends MatrixItem
{
}
