////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.text;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
        group                      = ActionGroups.Text,
        constantGeneralDescription = R.TEXT_ADD_LINE_GENERAL_DESC,
        additionFieldsAllowed      = false,
        constantExamples           = R.TEXT_ADD_LINE_EXAMPLE,
        seeAlsoClass               = {TextReport.class, TextPerform.class, TextLoadFromFile.class, TextCreate.class, TextSaveToFile.class, TextSetValue.class}
)
public class TextAddLine extends AbstractAction {
    public final static String textName = "Text";
    public final static String lineName = "Line";

    @ActionFieldAttribute(name = textName, mandatory = true, description = "Object {{$Text$}}.")
    protected Text text = null;

    @ActionFieldAttribute(name = lineName, mandatory = true, description = "The line that should be added.")
    protected String line = null;

    @Override
    public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception {
        text.add(this.line);

        super.setResult(null);
    }
}

