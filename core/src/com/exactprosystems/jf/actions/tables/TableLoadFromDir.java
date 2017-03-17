////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
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
        group                 = ActionGroups.Tables,
        suffix                = "TBLD",
        generalDescription    = "This action is determined to get directory structure as an object Table."
                + "Can be used to check required files in  this directory.",
        additionFieldsAllowed = false,
        outputDescription     = "Table which consists columns Name, Size, Date, Is directory, Hidden. Each table row contains"
                + " data corresponding a file/folder in this directory. If it is directed to a null directory or file,"
                + " an object Table is created without any columns or rows. If it is directed to an empty directory, a"
                + " table is created with column titles and 0 rows.",
        outputType = Table.class,
        seeAlso 			  = "{{@RawTable@}}, {{@TableCreate@}}, {{@TableLoadFromFile@}}, {{@TableSelect@}}",
        examples              ="{{`1. Create a table with information about files/folders in directory Home.`}}\n"
                + "{{##Id;#Action;#Dir\n"
                + "TBLD1;TableLoadFromDir;System.getProperty('user.home')#}}",
        seeAlsoClass = {RawTable.class, TableCreate.class, TableLoadFromFile.class, TableSelect.class}
)
public class TableLoadFromDir extends AbstractAction
{
    public final static String dirName = "Dir";

    @ActionFieldAttribute(name = dirName,description = "Path to directory. It’s not permitted to use ways with metacharacters (wildcard).",mandatory = true)
    protected String directory = null;

    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
    {
        return HelpKind.ChooseFolder;
    }

    @Override
    public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
    {
        File dir = new File(directory);

        if (dir.exists() && dir.isDirectory()) {
            super.setResult(new Table(directory,evaluator));
        }else{
            super.setError("Directory '" + directory + "' doesn't exists.", ErrorKind.WRONG_PARAMETERS);
        }

    }

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}