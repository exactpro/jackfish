/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.app;

import java.util.Set;

public abstract class AbstractApplicationFactory implements IApplicationFactory
{
	public static final String trimTextName = "TrimText";

	private IGuiDictionary dictionary = null;

	//region IApplicationFactory
	@Override
	public void init(IGuiDictionary dictionary)
	{
		this.dictionary = dictionary;
	}

	@Override
	public IGuiDictionary getDictionary()
	{
		return this.dictionary;
	}

	@Override
	public boolean isAllowed(ControlKind kind, OperationKind operation)
	{
		return getInfo().isAllowed(kind, operation);
	}

	@Override
	public boolean isSupported(ControlKind kind)
	{
		return getInfo().isSupported(kind);
	}

	@Override
	public Set<ControlKind> supportedControlKinds()
	{
		return getInfo().supportedControlKinds();
	}

	//endregion
}
