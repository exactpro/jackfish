/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
