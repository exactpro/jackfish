/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.documents.matrix.parser.items.help;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
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
        report.outLine(this, null, "{{`" + classAttribute.text().get() + "`}}", null);

        Method[] methods = clazz.getDeclaredMethods();
        if (methods.length != 0)
        {
            report.outLine(this, null, "{{`{{5" + R.HELP_CLASS_METHODS.get() + "5}}`}}", null);
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
            	report.outLine(item, null, "{{`" + methodAttribute.text().get() + "`}}", null);
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
