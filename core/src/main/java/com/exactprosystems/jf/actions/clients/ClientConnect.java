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

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.net.Socket;
import java.util.List;

@ActionAttribute(
		group 				  	      = ActionGroups.Clients,
		suffix 				  	      = "CLCNCT",
		constantGeneralDescription    = R.CLIENT_CONNECT_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantOutputDescription     = R.CLIENT_CONNECT_OUTPUT_DESC,
		constantAdditionalDescription = R.CLIENT_CONNECT_ADDITIONAL_DESC,
		constantExamples 			  = R.CLIENT_CONNECT_EXAMPLE
)
public class ClientConnect extends AbstractAction
{
	public static final String connectionName = "ClientConnection";
	public static final String socketName     = "Socket";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_CONNECT_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = socketName, mandatory = true, constantDescription = R.CLIENT_CONNECT_SOCKET)
	protected Socket socket;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.CONNECT, context, this.owner.getMatrix(), parameters, null, connectionName, null);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName) ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, null, parameterToFill);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();

		boolean res = client.connect(context, this.socket, parameters.select(TypeMandatory.Extra).makeCopy());
		if (res)
		{
			super.setResult(null);
		}
		else
		{
			super.setError("Connection can not be established.", ErrorKind.CLIENT_ERROR);
		}
	}
}
