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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
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
            
            String source = sb.toString();
            
            String reg = "((\\{\\{[=|-]).*?([=|-]\\}\\}))";
            
            Pattern patt = Pattern.compile(reg, Pattern.DOTALL);
            String[] parts = patt.split(source);

            int counter = 0;
            report.outLine(this, null, parts[counter++], null);
            
            Matcher m = patt.matcher(source);
            while (m.find())
            {
                String text = m.group(2);
                boolean bordered = text.contains("=");
                
                String strTable = m.group();
                String[] lines = strTable.split("``");
                ReportTable table = null;
                for (String line : lines)
                {
                    String[] cells = line.split("`");
                    if (table == null)
                    {
                        int[] widths = new int[cells.length];
                        String[] columns = new String[cells.length];
                        for (int i = 0; i < cells.length; i++)
                        {
                            String[] columnNWidth = cells[i].split("\\|");
                            columns[i] = columnNWidth[0];
                            widths[i] = columnNWidth.length > 1 ? Integer.parseInt(columnNWidth[1]) : 0; 
                        }
                        table = report.addExplicitTable("", null, true, bordered, widths, columns);
                    }
                    else
                    {
                        table.addValues(cells);
                    }
                }
                report.itemIntermediate(this);
                
                report.outLine(this, null, parts[counter++], null);
            }
            if (counter < parts.length)
            {
                report.outLine(this, null, parts[counter++], null);
            }
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
