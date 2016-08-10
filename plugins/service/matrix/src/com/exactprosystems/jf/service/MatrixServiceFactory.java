////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.service;

import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.service.IService;
import com.exactprosystems.jf.api.service.IServiceFactory;
import com.exactprosystems.jf.api.service.ServiceHelper;
import com.exactprosystems.jf.api.service.ServicePossibility;

import java.util.Set;

public class MatrixServiceFactory implements IServiceFactory
{
	public final static String portName 				= "Port";
	public final static String onConnectedName 		= "OnConnected";
	
	private static final int requiredMajorVersion = 2;
	private static final int requiredMinorVersion = 19;

	@Override
	public boolean canFillParameter(String parameterToFill)
	{
		return false;
	}

	@Override
	public String[] listForParameter(String parameterToFill)
	{
		return new String[0];
	}

	@Override
	public String[] wellKnownParameters()
	{
		return knownParameters;
	}

	@Override
	public String[] wellKnownStartArgs()
	{
		return knownStartArgs;
	}

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

	@Override
	public int requiredMajorVersion()
	{
		return requiredMajorVersion;
	}

	@Override
	public int requiredMinorVersion()
	{
		return requiredMinorVersion;
	}

	@Override
	public boolean isSupported(int major, int minor)
	{
		return (major * 1000 + minor) >= (requiredMajorVersion * 1000 + requiredMinorVersion);
	}

	@Override
	public String[] wellKnownParameters(ParametersKind kind)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private static String[] knownParameters = { };
	
	private static String[] knownStartArgs = { portName, onConnectedName };
}
