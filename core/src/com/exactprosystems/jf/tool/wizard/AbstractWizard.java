////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.api.wizard.WizardResult;

import java.util.Arrays;

public abstract class AbstractWizard implements Wizard
{
	protected IContext      context;
	protected WizardManager wizardManager;

	@Override
	public void init(IContext context, WizardManager wizardManager, Object... parameters)
	{
		this.context = context;
		this.wizardManager = wizardManager;
	}

	@Override
	public WizardResult run()
	{
		return WizardResult.deny();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	@Override
	public WizardManager manager()
	{
		return this.wizardManager;
	}

	@SuppressWarnings("unchecked")
	protected <T> T get(Class<T> clazz, Object[] parameters)
	{
		if (parameters == null)
		{
			return null;
		}

		Object res = Arrays.stream(parameters).filter(p -> p != null && clazz.isAssignableFrom(p.getClass())).findFirst().orElse(null);

		return (T) res;
	}
}
