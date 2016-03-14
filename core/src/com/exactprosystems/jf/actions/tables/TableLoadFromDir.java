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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
        group = ActionGroups.Tables,
        suffix = "TBLD",
        generalDescription = "Loads information about all files in a directory into a table",
        additionFieldsAllowed = false,
        outputDescription = "Table object",
        outputType = Table.class
)
public class TableLoadFromDir extends AbstractAction
{
    public final static String dirName = "Dir";

    @ActionFieldAttribute(name = dirName,description = "Path to a directory",mandatory = true)
    protected String directory = null;

    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
    {
        return HelpKind.ChooseFolder;
    }

    @Override
    public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
    {
        super.setResult(new Table(directory,evaluator));
    }
}