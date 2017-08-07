////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api2.wizard;

import com.exactprosystems.jf.api.common.IContext;

public interface Wizard
{
	WizardManager manager();
    void init(IContext context, WizardManager wizardManager, Object... parameters);
    boolean beforeRun();
    WizardResult run();
}
