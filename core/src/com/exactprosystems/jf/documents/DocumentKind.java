/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.documents.csv.CsvFxController;
import com.exactprosystems.jf.tool.documents.guidic.DictionaryFxController;
import com.exactprosystems.jf.tool.documents.text.PlainTextFxController;
import com.exactprosystems.jf.tool.documents.vars.SystemVarsFxController;
import com.exactprosystems.jf.tool.matrix.MatrixFxController;

public enum DocumentKind
{
	MATRIX(true, MatrixFxController.class),
	LIBRARY(true, MatrixFxController.class),
	GUI_DICTIONARY(true, DictionaryFxController.class),
	MESSAGE_DICTIONARY(false, null),
	SYSTEM_VARS(true, SystemVarsFxController.class),
	CONFIGURATION(false, null),
	PLAIN_TEXT(true, PlainTextFxController.class),
	CSV(true, CsvFxController.class),
	REPORTS(true, null);

	private boolean                                                         useNewMVP;
	private Class<? extends AbstractDocumentController<? extends Document>> clazz;

	DocumentKind(boolean useNewMVP, Class<? extends AbstractDocumentController<? extends Document>> clazz)
	{
		this.useNewMVP = useNewMVP;
		this.clazz = clazz;
	}

	public boolean isUseNewMVP()
	{
		return this.useNewMVP;
	}

	public Class<? extends AbstractDocumentController<? extends Document>> getClazz()
	{
		return this.clazz;
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