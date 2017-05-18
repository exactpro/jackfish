package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group = ActionGroups.App,
		generalDescription = "Plug-in depend action. This action is used for moving the main window",
		additionFieldsAllowed = false,
		seeAlsoClass = {ApplicationStart.class, ApplicationConnectTo.class},
		additionalDescription = "Moving the main window"
)
public class ApplicationMove extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String xName = "X";
	public static final String yName = "Y";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies"
			+ " the started application session. This object is required in many other actions to specify the "
			+ "session of the application the indicated action belongs to. It is the output value of such actions "
			+ "as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}." )
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = xName, mandatory = true, description = "The X coordinate for move window.")
	protected Integer x;

	@ActionFieldAttribute(name = yName, mandatory = true, description = "The Y coordinate for move window")
	protected Integer y;

	@Override
	public void initDefaultValues()
	{

	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication application = connection.getApplication();
		application.service().moveWindow(this.x, this.y);
		super.setResult(null);
	}
}
