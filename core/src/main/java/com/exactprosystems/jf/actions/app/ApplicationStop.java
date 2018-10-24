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

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.App,
		suffix					   = "APPSTP",
		constantGeneralDescription = R.APP_STOP_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples           = R.APP_STOP_EXAMPLE,
		seeAlsoClass               = {ApplicationConnectTo.class, ApplicationStart.class}
	)
public class ApplicationStop extends AbstractAction 
{
	public static final String connectionName = "AppConnection";
	public static final String needKillName   = "Kill";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_STOP_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = needKillName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.APPLICATION_STOP_NEED_KILL)
	protected Boolean needKill;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		context.getConfiguration().getApplicationPool().stopApplication(this.connection, this.needKill);
		super.setResult(null);
	}
}
