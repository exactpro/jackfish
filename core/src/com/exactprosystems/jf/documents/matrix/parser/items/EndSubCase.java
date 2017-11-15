////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.documents.matrix.parser.Tokens;

@MatrixItemAttribute(
		description 	= "Marks end of sub case.", 
		shouldContain 	= { Tokens.EndSubCase },
		mayContain 		= { }, 
		closes			= SubCase.class,
		real			= false,
		hasValue 		= false, 
		hasParameters 	= false,
        hasChildren 	= false
	)
public class EndSubCase extends MatrixItem
{
	@Override
	protected MatrixItem makeCopy()
	{
		return new EndSubCase();
	}
}
