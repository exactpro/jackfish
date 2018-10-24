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
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group 					   = ActionGroups.Clients,
		suffix					   = "CLDEC",
		constantGeneralDescription = R.CLIENT_DECODE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.CLIENT_DECODE_OUTPUT_DESC,
		outputType 				   = MapMessage.class,
		constantExamples 		   = R.CLIENT_DECODE_EXAMPLE
		)
public class ClientDecode extends AbstractAction
{
	public static final String connectionName = "ClientConnection";
	public static final String arrayName      = "Array";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_DECODE_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = arrayName, mandatory = true, constantDescription = R.CLIENT_DECODE_ARRAY)
	protected Byte[] array;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Decoding);
		MapMessage res = client.getCodec().decode(Converter.convertToByteArray(this.array));

		super.setResult(res);
	}
}
