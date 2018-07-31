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
