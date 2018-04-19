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

public interface WizardManager
{
	/**
	 * @return name of the wizard
	 *
	 * @see WizardAttribute#name()
	 */
	String nameOf(Class<? extends Wizard> wizard);

	/**
	 * @return picture name of the wizard
	 *
	 * @see WizardAttribute#pictureName()
	 */
    String pictureOf(Class<? extends Wizard> wizard);

	/**
	 * @return short description of the wizard
	 *
	 * @see WizardAttribute#shortDescription()
	 */
    String shortDescriptionOf(Class<? extends Wizard> wizard);

	/**
	 * @return detailed description of the wizard
	 *
	 * @see WizardAttribute#detailedDescription()
	 */
	String detailedDescriptionOf(Class<? extends Wizard> wizard);

	/**
	 * @return category of the wizard
	 *
	 * @see WizardAttribute#category()
	 * @see WizardCategory
	 */
	WizardCategory categoryOf(Class<? extends Wizard> wizard);

	/**
	 * @return List of all registered wizards.
	 *
	 * @see Wizard
	 */
	List<Class<? extends Wizard>> allWizards();

	/**
	 * @return List of all wizards, which matched the passed criteries
	 *
	 * @see Wizard
	 * @see WizardAttribute#criteries()
	 */
	List<Class<? extends Wizard>> suitableWizards(Object... criteries);

	/**
	 * Execute the wizard
	 * @param wizard which will execute's
	 * @param context context for executing the wizard
	 * @param criteries for execute the wizard
	 */
	void runWizard(Class<? extends Wizard> wizard, Context context, Object... criteries);
}
