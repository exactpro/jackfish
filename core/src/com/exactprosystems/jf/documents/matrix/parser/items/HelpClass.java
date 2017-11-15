////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class HelpClass extends MatrixItem
{
	private Class<?> clazz;

	public HelpClass(Class<?> clazz)
    {
        this.clazz = clazz;
    }

	@Override
	protected MatrixItem makeCopy()
	{
		return new HelpClass(this.clazz);
	}
    
    @Override
    public String getItemName()
    {
        return "";
    }
    
    public void itemReport(ReportBuilder report, MatrixItem item, Class<?> clazz)
    {
        DescriptionAttribute classAttribute = clazz.getAnnotation(DescriptionAttribute.class);
        if (classAttribute == null)
        {
            return;
        }
        
        report.itemIntermediate(item);
        report.putMark(this.clazz.getSimpleName());
        report.outLine(this, null, "{{`{{3" + this.clazz.getSimpleName() + "3}}`}}", null);
        report.outLine(this, null, "{{`" + classAttribute.text() + "`}}", null);

        Method[] methods = clazz.getDeclaredMethods();
        if (methods.length != 0)
        {
            report.outLine(this, null, "{{`{{5Methods5}}`}}", null);
            for (Method method : methods)
            {
            	DescriptionAttribute methodAttribute = method.getAnnotation(DescriptionAttribute.class);
            	if (methodAttribute == null)
            	{
            		continue;
            	}
            	String ret = method.getReturnType().getSimpleName();
            	String name = method.getName();
                String s = Arrays.stream(method.getParameters()).map(c -> c.getName()).collect(Collectors.joining(", "));
            	String str = String.format("{{``}}{{`%s {{*%s*}}(%s)`}}", ret, name, s);
            	report.outLine(item, null, str, null);
            	report.outLine(item, null, "{{`" + methodAttribute.text() + "`}}", null);
            }
        }
    }


    @Override
    protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
    {
        try
        {
            if (this.clazz != null)
            {
                itemReport(report, this, this.clazz);
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
