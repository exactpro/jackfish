package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Text;
import com.exactprosystems.jf.functions.Xml;

import java.io.Reader;

@ActionAttribute(
		group					   = ActionGroups.XML,
		suffix					   = "XMLFT",
		constantGeneralDescription = R.XML_FROM_TEXT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.XML_FROM_TEXT_OUTPUT_DESC,
		outputType				   = Xml.class,
		constantExamples 		   = R.XML_FROM_TEXT_EXAMPLE
)
public class XmlFromText extends AbstractAction
{
	public final static String textName		= "Text";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "{{$Text$}} object related to the xml.")
	protected Text text = null;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		try(Reader reader = CommonHelper.readerFromString(this.text.toString()))
		{
			super.setResult(new Xml(reader));
		}
	}
}
