////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

import java.util.List;

import com.exactprosystems.jf.api.common.IContext;

public interface WizardManager
{
    String nameOf(Class<? extends Wizard> wizard);
    String shortDescriptionOf(Class<? extends Wizard> wizard);
    String detailedDescriptionOf(Class<? extends Wizard> wizard);
    WizardCategory categoryOf(Class<? extends Wizard> wizard);
    
    List<Class<? extends Wizard>> allWizards();
    List<Class<? extends Wizard>> suitableWizards(Object ... criteries);
    
    void runWizard(Class<? extends Wizard> wizard, IContext context, Object ... criteries); 
}
