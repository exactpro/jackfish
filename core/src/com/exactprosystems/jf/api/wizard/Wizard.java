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
	/**
	 * @return WizardManager for work with Wizards
	 * @see WizardManager
	 * @see com.exactprosystems.jf.tool.wizard.WizardManagerImpl
	 */
	WizardManager manager();

	/**
	 * Initialize current wizard.
	 * @param context context for initializing
	 * @param wizardManager wizardManager for initializing
	 * @param parameters parameters, which will passed to initialize the wizard
	 */
	void init(IContext context, WizardManager wizardManager, Object... parameters);

	/**
	 * Check, that the wizard has correct parameters
	 * @return true if all passed parameters are correct and sufficient for work wizard. Otherwise return false.
	 */
	boolean beforeRun();

	/**
	 * Run the wizard and returned result
	 * @return WizardResult object - result of work the wizard
	 *
	 * @see WizardResult
	 */
	WizardResult run();
}
