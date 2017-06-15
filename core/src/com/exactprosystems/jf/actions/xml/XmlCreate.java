package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Xml;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


@ActionAttribute(
        group = ActionGroups.XML,
        generalDescription = "Create new xml.",
        examples = "{{#\n" +
                "#Id;#Action\n" +
                "xml;XmlCreate#}}",
        additionFieldsAllowed = false
)
public class XmlCreate extends AbstractAction
{
    @Override
    public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        super.setResult(new Xml(document));
    }
}
