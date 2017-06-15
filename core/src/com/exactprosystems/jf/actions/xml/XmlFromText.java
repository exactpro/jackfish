package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Text;
import com.exactprosystems.jf.functions.Xml;

import java.io.Reader;

@ActionAttribute(
		group					= ActionGroups.XML,
		suffix					= "XMLFT",
		generalDescription 		= "The purpose of this action is to parse the XML structure from a text.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "XML structure.",
		outputType				= Xml.class,
		examples 				= "{{`1. Create Xml object by parsing the text.`}} "
				+ "{{`Contents of an text object:`}} "
				+ "{{#\n" +
				"<note> \n"
				+ "<to>\n"
				+ "<friend>\n"
				+ "<name id=\"first\">Tove</name>\n"
				+ "</friend>\n"
				+ "</to>\n"
				+ "<from>\n"
				+ "<friend>\n"
				+ "<name id=\"second\">Jani</name>\n"
				+ "</friend>\n"
				+ "</from>\n"
				+ "<heading>Reminder</heading>\n"
				+ "<body>Don't forget me this weekend!</body>\n"
				+ "</note>#}}"
				+ "\n"
				+ "{{`2. Make sure that the object has been created and contains nodes.`}} "
				+ "{{#\n" +
				"#Id;#Action;#Text\n"
				+ "XML1;XmlFromText;TEXT1\n"
				+ "#Assert;#Message\n"
				+ "XML1.Result.toString() == 'Passed';'No such attribute'#}}"
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
