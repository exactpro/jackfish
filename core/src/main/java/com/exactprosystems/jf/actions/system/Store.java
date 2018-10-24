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

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group 					   = ActionGroups.System,
		constantGeneralDescription = R.STORE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.STORE_EXAMPLE,
		seeAlsoClass 			   = {Restore.class}
)

public class Store extends AbstractAction
{
	public static final String nameName  = "Name";
	public static final String valueName = "Value";

	@ActionFieldAttribute(name = nameName, mandatory = true, constantDescription = R.STORE_NAME)
	protected String name;

	@ActionFieldAttribute(name = valueName, mandatory = false, constantDescription = R.STORE_VALUE)
	protected Object value;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		context.getConfiguration().storeGlobal(this.name, this.value);
		super.setResult(null);
	}
}
