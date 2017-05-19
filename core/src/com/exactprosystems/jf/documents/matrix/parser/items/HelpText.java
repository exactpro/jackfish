////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;


public class HelpText extends MatrixItem
{
	public HelpText(InputStream stream)
	{
        this.stream = stream;
	}

	@Override
	public String getItemName()
	{
		return "";
	}
	
	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
        try
        {
            StringBuilder sb = new StringBuilder();
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(this.stream)) )
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line).append("\n");
                }
            }
            report.outLine(this, null, sb.toString(), null);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        return new ReturnAndResult(start, Result.Passed); 
	}

    private InputStream stream = null;
}
