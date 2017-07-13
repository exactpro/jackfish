////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.NewHelpBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Content;
import com.exactprosystems.jf.functions.ContentItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpTextLine extends MatrixItem
{
	public HelpTextLine(String name, Content content)
	{
		this.str = name;
		this.content = content;
	}

	@Override
	public String getItemName()
	{
		return this.str;
	}
	
	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
        try
        {
			checkText(this.str, this.content, report);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        return executeChildren(start, context, listener, evaluator, report, new Class<?>[] {  });
	}

	private void checkText(String text, Content content, ReportBuilder report){
		if(report instanceof NewHelpBuilder) {
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

					content.add(new ContentItem(
							String.format("<li role='presentation'>\n<a href='#%s'>%s</a>\n", mark.replaceAll("\\s+", "").toLowerCase(), mark))
					);

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

	private String str = null;
	private Content content = null;

}
