////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

public enum DocumentKind
{
	MATRIX, LIBRARY, GUI_DICTIONARY, MESSAGE_DICIONARY, SYSTEM_VARS, CONFIGURATION, PLAIN_TEXT, CSV;
	
    public static <T extends Document> DocumentKind byDocument(T doc)
    {
        DocumentInfo attr = doc.getClass().getAnnotation(DocumentInfo.class);
        if (attr != null)
        {
            return attr.kind();
        }
        return null;
	}
	
}