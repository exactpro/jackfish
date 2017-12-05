////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.HTMLhelper;
import com.exactprosystems.jf.common.report.HelpBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HelpItem extends MatrixItem
{
	private Class<? extends MatrixItem> itemClazz;

	public HelpItem(Class<? extends MatrixItem> itemClazz)
    {
        this.itemClazz = itemClazz;
    }

	@Override
	protected MatrixItem makeCopy()
	{
		return new HelpItem(this.itemClazz);
	}

	@Override
    public String getItemName()
    {
        return "";
    }
    
    public void itemReport(ReportBuilder report, MatrixItem item, Class<? extends MatrixItem> clazz)
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
        report.putMark(this.itemClazz.getSimpleName());
        report.outLine(this, null, "{{`{{3" + this.itemClazz.getSimpleName() + "3}}`}}", null);
        report.outLine(this, null, "{{`" + attribute.constantGeneralDescription().get() + "`}}", null);
        report.outLine(this, null, "{{`{{*Examples*}}`}}", null);
        if (report instanceof HelpBuilder){
            report.outLine(this, null, "{{`" + HTMLhelper.htmlescape(attribute.constantExamples().get()) + "`}}", null);
        }
        else
        {
            report.outLine(this, null, "{{`" + attribute.constantExamples().get() + "`}}", null);
        }
        if (attribute.seeAlsoClass().length != 0)
        {
            report.outLine(this, null, "{{`{{*See also*}}`}}", null);
            String s = Arrays.stream(attribute.seeAlsoClass()).map(c -> report.decorateLink(c.getSimpleName(), c.getSimpleName())).collect(Collectors.joining(", "));
            report.outLine(this, null, "{{`" + s + "`}}", null);
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
}
