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
	MATRIX(true),
	LIBRARY(true),
	GUI_DICTIONARY(false),
	MESSAGE_DICTIONARY(false),
	SYSTEM_VARS(true),
	CONFIGURATION(false),
	PLAIN_TEXT(true),
	CSV(true),
	REPORTS(true);

	private boolean useNewMVP;

	private DocumentKind(boolean useNewMVP)
	{
		this.useNewMVP = useNewMVP;
	}

	public boolean isUseNewMVP()
	{
		return this.useNewMVP;
	}

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