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

import java.util.Collections;
import java.util.List;

public class WizardResult
{
	private boolean submitted;

	private List<WizardCommand> commands;

	private WizardResult(List<WizardCommand> commands, boolean submitted)
	{
		this.commands = commands;
		this.submitted = submitted;
	}

	public static WizardResult submit(List<WizardCommand> commands)
	{
		return new WizardResult(commands, true);
	}

	public static WizardResult deny()
	{
		return new WizardResult(Collections.emptyList(), false);
	}

	public boolean submitted()
	{
		return this.submitted;
	}

	public List<WizardCommand> commands()
	{
		return this.commands;
	}
}
