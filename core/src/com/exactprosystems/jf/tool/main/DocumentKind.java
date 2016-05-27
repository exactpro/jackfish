////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.common.evaluator.SystemVars;
import com.exactprosystems.jf.common.xml.messages.MessageDictionary;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.csv.CsvFx;
import com.exactprosystems.jf.tool.text.PlainTextFx;

public enum DocumentKind
{
	MATRIX, GUI_DICTIONARY, MESSAGE_DICIONARY, SYSTEM_VARS, CONFIGURATION, PLAIN_TEXT, CSV;
	
	public static <T extends Document> DocumentKind byDocument(T doc)
	{
		if (doc instanceof Configuration)
		{
			return CONFIGURATION;
		}
		if (doc instanceof GuiDictionary)
		{
			return GUI_DICTIONARY;
		}
		if (doc instanceof Matrix)
		{
			return MATRIX;
		}
		if (doc instanceof MessageDictionary)
		{
			return MESSAGE_DICIONARY;
		}
		if (doc instanceof SystemVars)
		{
			return SYSTEM_VARS;
		}
		if (doc instanceof PlainTextFx)
		{
			return PLAIN_TEXT;
		}
		if (doc instanceof CsvFx)
		{
			return CSV;
		}
		
		return null;
	}
	
}