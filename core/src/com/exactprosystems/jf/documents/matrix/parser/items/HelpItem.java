////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

public class HelpItem extends MatrixItem
{
    public HelpItem(Class<? extends MatrixItem> itemClazz)
    {
        this.itemClazz = itemClazz;
    }
    
    @Override
    public String getItemName()
    {
        return this.itemClazz.getSimpleName();
    }
    
    public static void itemReport(ReportBuilder report, MatrixItem item, Class<? extends MatrixItem> clazz)
    {
        MatrixItemAttribute attribute = clazz.getAnnotation(MatrixItemAttribute.class);
        if (attribute == null)
        {
            return;
        }
        if (!attribute.real() || clazz.equals(ActionItem.class) || clazz.equals(TempItem.class))
        {
            return;
        }

        report.itemIntermediate(item);
        ReportTable table = report.addTable(item.getItemName(), null, true, 100, new int[] { 30, 70 }, "", "");
        table.addValues("Description", attribute.description());
        table.addValues("Examples", attribute.examples());
        if (attribute.seeAlsoClass().length > 0)
        {
            StringBuilder sb = new StringBuilder();
            for (Class clz : attribute.seeAlsoClass()){
                sb.append(clz.getSimpleName()).append(" ");
            }
            table.addValues("See also", sb.toString());
        }
    }


    @Override
    protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
    {
        try
        {
            if (this.itemClazz != null)
            {
                itemReport(report, this, this.itemClazz);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        return new ReturnAndResult(start, Result.Passed); 
    }
	
    private Class<? extends MatrixItem> itemClazz;
}
