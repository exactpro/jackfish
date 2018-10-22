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

package com.exactprosystems.jf.actions.clients;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.client.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.Clients,
		suffix					   = "CLENC",
		constantGeneralDescription = R.CLIENT_ENCODE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.CLIENT_ENCODE_OUTPUT_DESC,
		outputType				   = byte[].class,
		constantExamples 		   = R.CLIENT_ENCODE_EXAMPLE
	)
public class ClientEncode extends AbstractAction
{
	public static final String connectionName = "ClientConnection";
	public static final String messageName    = "MapMessage";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_ENCODE_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = messageName, mandatory = true, constantDescription = R.CLIENT_ENCODE_MESSAGE)
	protected MapMessage message;
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Encoding);
		byte[] res = client.getCodec().encode(this.message.getMessageType(), this.message);

		super.setResult(res);
	}
}
