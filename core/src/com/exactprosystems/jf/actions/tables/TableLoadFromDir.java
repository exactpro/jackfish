////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

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
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.io.File;

@ActionAttribute(
        group                      = ActionGroups.Tables,
        suffix                     = "TBLD",
        constantGeneralDescription =  R.TABLE_LOAD_FROM_DIR_GENERAL_DESC ,
        additionFieldsAllowed      = false,
        constantOutputDescription  = R.TABLE_LOAD_FROM_DIR_OUTPUT_DESC,
        outputType                 = Table.class,
        constantExamples           = R.TABLE_LOAD_FROM_DIR_EXAMPLE,
        seeAlsoClass               = {RawTable.class, TableCreate.class, TableLoadFromFile.class, TableSelect.class}
)
public class TableLoadFromDir extends AbstractAction
{
	public static final String dirName = "Dir";

	@ActionFieldAttribute(name = dirName, mandatory = true, constantDescription = R.TABLE_LOAD_FROM_DIR_DIRECTORY)
	protected String directory;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return HelpKind.ChooseFolder;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		File dir = new File(this.directory);

		if (dir.exists() && dir.isDirectory())
		{
			super.setResult(new Table(this.directory, evaluator));
		}
		else
		{
			super.setError(String.format("Directory '%s' doesn't exists.", this.directory), ErrorKind.WRONG_PARAMETERS);
		}
	}
}