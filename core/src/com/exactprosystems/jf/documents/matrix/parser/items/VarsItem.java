////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.exceptions.ParametersException;
import com.exactprosystems.jf.functions.Table;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
		description = "Create vars",
		shouldContain = {Tokens.VarsItem},
		mayContain = {Tokens.Off},
		real = true,
		hasValue = true,
		hasParameters = true,
		hasChildren = false)
public class VarsItem extends MatrixItem
{
    public VarsItem() {
        super();
        this.name = new MutableValue<String>();
    }

    @Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 1);
        driver.showComment(this, layout, 0, 0, getComments());
        driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
        driver.showTitle(this, layout, 1, 1, Tokens.VarsItem.get(), context.getFactory().getSettings());
        driver.showParameters(this, layout, 1, 3, this.parameters, null, false, false);
        driver.showCheckBox(this, layout, 1, 2, "Global", this.global, this.global);
		return layout;
	}

    @Override
    public MatrixItem clone() throws CloneNotSupportedException
    {
        VarsItem varsItem = (VarsItem) super.clone();
        varsItem.name = this.name.clone();
        return varsItem;
    }

    public String getName()
    {
        return this.name.get();
    }

    @Override
    protected void initItSelf(Map<Tokens, String> systemParameters)
    {
        this.name.set(systemParameters.get(Tokens.VarsItem));
    }

    @Override
    protected String itemSuffixSelf()
    {
        return "VARSITEM_";
    }

    @Override
    protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
    {
        addParameter(firstLine, secondLine, Tokens.VarsItem.get(), this.name.get());

        for (Parameter entry : getParameters())
        {
            super.addParameter(firstLine, secondLine, entry.getName(), entry.getExpression());
        }
    }

    @Override
    protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator,
                                            ReportBuilder report, Parameters parameters)
    {
        try
        {
            boolean paramIsValid = false;
            for (Parameter param : parameters)
            {
                paramIsValid = param.evaluate(evaluator);
                if (!paramIsValid)
                {
                    break;
                }
                if (super.isGlobal())
                {
                    evaluator.getGlobals().set(parameters.select(TypeMandatory.Extra));
                }
                else
                {
                    evaluator.getLocals().set(parameters.select(TypeMandatory.Extra));
                }
            }

            if (!paramIsValid)
            {
                reportParameters(report, parameters);
                throw new ParametersException("Errors in parameters expressions #VarsItem", parameters);
            }

            reportParameters(report, parameters);
            return new ReturnAndResult(Result.Passed);
        }
        catch (ParametersException e)
        {
            List<String> errors = e.getParameterErrors();
            listener.error(getMatrix(), getNumber(), this, errors.get(0));
            return new ReturnAndResult(Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
    }

    private void reportParameters(ReportBuilder report, Parameters parameters)
    {
        ReportTable table = report.addTable("Input parameters", null, true, 2, new int[] { 20, 40, 40 }, "Parameter", "Expression", "Value");

        for (Parameter parameter : parameters)
        {
            table.addValues(parameter.getName(), parameter.getExpression(), parameter.getValue());
        }
    }

    private MutableValue<String> name;
}
