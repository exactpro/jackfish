////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

import com.exactprosystems.jf.api.common.IContext;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface WizardManager
{
	String nameOf(Class<? extends Wizard> wizard);

    String pictureOf(Class<? extends Wizard> wizard);

    String shortDescriptionOf(Class<? extends Wizard> wizard);

	String detailedDescriptionOf(Class<? extends Wizard> wizard);

	WizardCategory categoryOf(Class<? extends Wizard> wizard);

	List<Class<? extends Wizard>> allWizards();

	List<Class<? extends Wizard>> suitableWizards(Object... criteries);

	void runWizard(Class<? extends Wizard> wizard, IContext context, Object... criteries);

	default void runWizardDefault(IContext context, Supplier<Object[]> supplierCriteria, Function<List<Class<? extends Wizard>>, Class<? extends Wizard>> supplierWizard, Consumer<String> notFoundConsumer)
	{
		Object[] criteries = supplierCriteria.get();
		List<Class<? extends Wizard>> suitableWizards = suitableWizards(criteries);
		Class<? extends Wizard> suitableWizard;
		if (suitableWizards.size() > 1)
		{
			suitableWizard = supplierWizard.apply(suitableWizards);
		}
		else if (suitableWizards.size() == 0)
		{
			notFoundConsumer.accept("Not found suitable wizards");
			return;
		}
		else
		{
			suitableWizard = suitableWizards.get(0);
		}
		runWizard(suitableWizard, context, criteries);
	}
}
