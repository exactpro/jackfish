////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

import java.util.List;

import com.exactprosystems.jf.documents.config.Context;

public interface WizardManager
{
	String nameOf(Class<? extends Wizard> wizard);

    String pictureOf(Class<? extends Wizard> wizard);

    String shortDescriptionOf(Class<? extends Wizard> wizard);

	String detailedDescriptionOf(Class<? extends Wizard> wizard);

	WizardCategory categoryOf(Class<? extends Wizard> wizard);

	List<Class<? extends Wizard>> allWizards();

	List<Class<? extends Wizard>> suitableWizards(Object... criteries);

	void runWizard(Class<? extends Wizard> wizard, Context context, Object... criteries);
}
