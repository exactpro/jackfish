////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.common.evaluator.SystemVars;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.xml.gui.GuiDictionary;
import com.exactprosystems.jf.common.xml.messages.MessageDictionary;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.tool.csv.CsvFx;
import com.exactprosystems.jf.tool.text.PlainTextFx;

public interface 		DocumentFactory
{
	Configuration 		createConfig();

	Matrix 				createMatrix();

	MessageDictionary 	createClientDictionary();

	GuiDictionary 		createAppDictionary();

	Csv 				createCsv();

	PlainTextFx 		createPlainText(); // TODO create intermediate class

	SystemVars 			createVars();
}
