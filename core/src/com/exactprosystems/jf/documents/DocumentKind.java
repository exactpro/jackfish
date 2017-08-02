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
	MATRIX, LIBRARY, GUI_DICTIONARY, MESSAGE_DICTIONARY, SYSTEM_VARS, CONFIGURATION, PLAIN_TEXT, CSV, REPORTS;
	
    public static <T extends Document> DocumentKind byDocument(T doc)
    {
        Class<?> aClass = doc.getClass();
        DocumentInfo attr = aClass.getAnnotation(DocumentInfo.class);

        while (attr == null && aClass != null)
        {
            attr = aClass.getAnnotation(DocumentInfo.class);
            aClass = aClass.getSuperclass();
        }

        return attr.kind();
	}

}