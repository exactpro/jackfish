/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
