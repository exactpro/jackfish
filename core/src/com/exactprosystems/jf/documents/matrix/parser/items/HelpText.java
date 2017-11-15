////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.HelpBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

public class HelpText extends MatrixItem
{
	private InputStream stream = null;

	//TODO remove this stream
	public HelpText(InputStream stream)
    {
        this.stream = stream;
    }

	@Override
	protected MatrixItem makeCopy()
	{
		//TODO
		return new HelpText(this.stream);
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
            checkText(parts[counter++],  report);

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

                checkText(parts[counter++],  report);
            }
            if (counter < parts.length)
            {
                checkText(parts[counter++],  report);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        return new ReturnAndResult(start, Result.Passed); 
    }

    private void checkText(String text, ReportBuilder report){
        if(report instanceof HelpBuilder) {
            boolean isSection = text.contains("{{1") && text.contains("1}}");
            boolean isSubSection = text.contains("{{2") && text.contains("2}}");
            if (isSection | isSubSection) {
                String reg = "((\\{\\{[1|2]).*?([2|1]\\}\\}))";
                Pattern patt = Pattern.compile(reg, Pattern.DOTALL);
                String[] split = patt.split(text);
                int counter = 0;
                report.outLine(this, null, split[counter++], null);
                Matcher m = patt.matcher(text);
                while (m.find()) {
                    String foundedText = m.group();
                    String mark = foundedText.replace("{{1", "").replace("1}}", "")
                            .replace("{{2", "").replace("2}}", "");

                    report.putMark(mark.replaceAll("\\s+", "").toLowerCase());
                    report.outLine(this, null, m.group(), null);
                    report.outLine(this, null, split[counter++], null);
                }
                if (counter < split.length) {
                    report.outLine(this, null, split[counter++], null);
                }
            } else {
                report.outLine(this, null, text, null);
            }
        } else {
            report.outLine(this, null, text, null);
        }
    }
}
