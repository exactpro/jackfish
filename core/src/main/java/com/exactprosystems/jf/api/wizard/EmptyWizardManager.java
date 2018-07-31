/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.wizard;

import com.exactprosystems.jf.documents.config.Context;

import java.util.List;

public class EmptyWizardManager implements WizardManager
{
	@Override
	public String nameOf(Class<? extends Wizard> wizard)
	{
		return null;
	}

	@Override
	public String pictureOf(Class<? extends Wizard> wizard)
	{
		return null;
	}

	@Override
	public String shortDescriptionOf(Class<? extends Wizard> wizard)
	{
		return null;
	}

	@Override
	public String detailedDescriptionOf(Class<? extends Wizard> wizard)
	{
		return null;
	}

	@Override
	public WizardCategory categoryOf(Class<? extends Wizard> wizard)
	{
		return null;
	}

	@Override
	public List<Class<? extends Wizard>> allWizards()
	{
		return null;
	}

	@Override
	public List<Class<? extends Wizard>> suitableWizards(Object... criteries)
	{
		return null;
	}

	@Override
	public void runWizard(Class<? extends Wizard> wizard, Context context, Object... criteries)
	{

	}
}
