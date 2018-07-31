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

import com.exactprosystems.jf.api.common.i18n.R;

public enum WizardCategory
{
	MATRIX				(R.WIZARD_CATEGORY_MATRIX),
	CONFIGURATION		(R.WIZARD_CATEGORY_CONFIGURATION),
	GUI_DICTIONARY		(R.WIZARD_CATEGORY_GUI_DIC),
	MESSAGE_DICTIONARY	(R.WIZARD_CATEGORY_MESSAGE_DIC),
	OTHER				(R.WIZARD_CATEGORY_OTHER);

	private R name;

	WizardCategory(R name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return this.name.get();
	}
}
