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
