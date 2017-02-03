////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser.items;

import java.lang.reflect.Field;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.HTMLhelper;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

@MatrixItemAttribute(
		description = "Help item item",
		shouldContain = {},
		mayContain = {},
		real = true,
		hasValue = false,
		hasParameters = false,
		hasChildren = false)
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
    
    public static void actionReport(ReportBuilder report, MatrixItem item, Class<? extends AbstractAction> clazz)
    {
        ActionAttribute attr = clazz.getAnnotation(ActionAttribute.class);

        ReportTable table = report.addTable("", null, true, 0, new int[] { 30, 70 }, "Action item", clazz.getSimpleName());

        table.addValues("Description", attr.generalDescription());
        if (attr.additionFieldsAllowed())
        {
            table.addValues("Additional fields", "Yes");
            table.addValues("Additional fields description", attr.additionalDescription());
        }
        else
        {
            table.addValues("Additional fields", "No");
        }
        table.addValues("See also", attr.seeAlso());
        table.addValues("Examples", HTMLhelper.htmlescape(attr.examples()));

        // Input
        Field[] fields = clazz.getDeclaredFields();
        table = report.addTable("Input:", null, true, 4, new int[] { 0, 0, 60, 0, 0 }, "Field name", "Field type",
                "Description", "Mandatory");
        table.addValues();
        for (Field f : fields)
        {
            ActionFieldAttribute annotation = f.getAnnotation(ActionFieldAttribute.class);
            if (annotation == null)
            {
                continue;
            }
            table.addValues(annotation.name(), f.getType().getSimpleName(), annotation.description(),
                    annotation.mandatory() ? "Yes" : "No");
        }

        // Output
        table = report.addTable("Output:", null, true, 100, new int[] { 20, 40 }, "Output type", "Description");
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
