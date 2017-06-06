////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser.items;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

public class HelpActionItem extends MatrixItem
{
    public HelpActionItem(Class<? extends AbstractAction> itemClazz)
    {
        this.actionClazz = itemClazz;
    }
    
    @Override
    public String getItemName()
    {
        return "Action " + this.actionClazz.getSimpleName();
    }
    
    public void actionReport(ReportBuilder report, MatrixItem item, Class<? extends AbstractAction> clazz)
    {
        ActionAttribute attr = clazz.getAnnotation(ActionAttribute.class);

        report.itemIntermediate(item);
        report.outLine(this, null, "{{`" + attr.generalDescription() + "`}}", null);

        if (attr.additionFieldsAllowed())
        {
            report.outLine(this, null, "{{`{{*Additional fields - Yes*}}`}}", null);
            report.outLine(this, null, "{{`" + attr.additionalDescription() + "`}}", null);
        }
        else
        {
            report.outLine(this, null, "{{`{{*Additional fields - No*}}`}}", null);
        }
        report.outLine(this, null, "{{`{{*Examples*}}`}}", null);
        report.outLine(this, null, "{{`" + attr.examples() + "`}}", null);
        if (attr.seeAlsoClass().length != 0)
        {
            report.outLine(this, null, "{{`{{*See also*}}`}}", null);
            String s = Arrays.stream(attr.seeAlsoClass()).map(c -> "{{@" + c.getSimpleName() + "@}}").collect(Collectors.joining(", "));
            report.outLine(this, null, "{{`" + s + "`}}", null);
        }
        
        // Input
        Field[] fields = clazz.getDeclaredFields();
        ReportTable table = report.addTable("{{*Input:*}}", null, true, false, new int[] { 15, 15, 50, 10, 10 }, "Field name", "Field type",
                "Description", "Mandatory", "Default");

        for (Field f : fields)
        {
            ActionFieldAttribute annotation = f.getAnnotation(ActionFieldAttribute.class);
            if (annotation == null)
            {
                continue;
            }
            if (annotation.mandatory())
            {
                table.addValues(annotation.name(), f.getType().getSimpleName(), annotation.description(), "Yes", "");
            }
            else
            {
                table.addValues(annotation.name(), f.getType().getSimpleName(), annotation.description(), "No", annotation.def());
            }
        }

        // Output
        table = report.addTable("{{*Output:*}}", null, true, false, new int[] { 30, 70 }, "Result type", "Description");
        table.addValues(attr.outputType().getSimpleName(), attr.outputDescription());
    }


    @Override
    protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
    {
        try
        {
            if (this.actionClazz != null)
            {
                actionReport(report, this, this.actionClazz);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        return new ReturnAndResult(start, Result.Passed); 
    }
	
    private Class<? extends AbstractAction> actionClazz;
}
