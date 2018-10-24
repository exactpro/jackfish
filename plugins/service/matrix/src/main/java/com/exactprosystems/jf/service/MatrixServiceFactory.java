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

package com.exactprosystems.jf.service;

import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.service.IService;
import com.exactprosystems.jf.api.service.IServiceFactory;
import com.exactprosystems.jf.api.service.ServiceHelper;
import com.exactprosystems.jf.api.service.ServicePossibility;

import java.util.Set;

public class MatrixServiceFactory implements IServiceFactory
{
	public final static String portName 			= "Port";
	public final static String onConnectedName 		= "OnConnected";
	
	private static String[] empty = {  };

	//----------------------------------------------------------------------------------------------
	// IFactory
	//----------------------------------------------------------------------------------------------
	@Override
	public String[] wellKnownParameters(ParametersKind kind)
	{
		switch (kind)
		{
			case START:	return new String[] { portName, onConnectedName };
			default:	return empty;	
		}
	}

	@Override
	public boolean canFillParameter(String parameterToFill)
	{
		return false;
	}

	@Override
	public String[] listForParameter(String parameterToFill)
	{
		return empty;
	}

	//----------------------------------------------------------------------------------------------
	// IServiceFactory
	//----------------------------------------------------------------------------------------------
	@Override
	public IService createService()
	{
		return new MatrixService();
	}

	@Override
	public Set<ServicePossibility> possebilities()
	{
		return ServiceHelper.possebilities(MatrixService.class);
	}
}
