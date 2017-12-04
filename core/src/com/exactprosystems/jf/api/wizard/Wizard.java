////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

import com.exactprosystems.jf.api.common.IContext;

public interface Wizard
{
	WizardManager manager();
	void init(IContext context, WizardManager wizardManager, Object... parameters);
	boolean beforeRun();
	WizardResult run();
}
