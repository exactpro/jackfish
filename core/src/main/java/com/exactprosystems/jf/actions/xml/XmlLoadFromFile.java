/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;

import java.io.File;

@ActionAttribute(
		group					   = ActionGroups.XML,
		suffix					   = "XML",
		constantGeneralDescription = R.XML_LOAD_FROM_FILE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.XML_LOAD_FROM_FILE_OUTPUT_DESC,
		outputType				   = Xml.class,
		constantExamples 		   = R.XML_LOAD_FROM_FILE_EXAMPLE
	)
public class XmlLoadFromFile extends AbstractAction 
{
	public static final String fileName = "File";

	@ActionFieldAttribute(name = fileName, mandatory = true, constantDescription = R.XML_LOAD_FROM_FILE_FILE)
	protected String file;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return fileName.equals(fieldName) ? HelpKind.ChooseOpenFile : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (!new File(this.file).exists())
		{
			setError(String.format("File '%s' does not exists", this.file), ErrorKind.WRONG_PARAMETERS);
			return;
		}
		if (new File(this.file).isDirectory())
		{
			setError(String.format("Cant load xml file from directory '%s'", this.file), ErrorKind.WRONG_PARAMETERS);
			return;
		}
		super.setResult(new Xml(this.file));
	}
}
