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

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Configuration;

import java.util.List;

public class ActionServiceHelper
{
	private ActionServiceHelper()
	{

	}

	public static ServiceConnection checkConnection(Object parameter) throws Exception
	{
		if (parameter == null)
		{
			throw new Exception(R.SERVICE_HELPER_CONNECTION_IS_NULL.get());
		}
		
		if (parameter instanceof ServiceConnection)
		{
			return (ServiceConnection)parameter;
		}
		
		throw new Exception(R.SERVICE_HELPER_SERVICE_NOT_LOADED.get());
	}

	public static void serviceNames(List<ReadableValue> list, AbstractEvaluator evaluator, Configuration configuration)
	{
		configuration.getServices().stream()
				.map(evaluator::createString)
				.map(ReadableValue::new)
				.forEach(list::add);
	}
}
