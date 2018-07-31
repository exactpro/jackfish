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
	void init(Context context, WizardManager wizardManager, Object... parameters);

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
