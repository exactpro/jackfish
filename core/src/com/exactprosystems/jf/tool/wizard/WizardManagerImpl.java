////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import java.util.Arrays;
import java.util.List;

import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardManager;

public class WizardManagerImpl implements WizardManager
{
    private static List<Class<? extends Wizard>> knownWizards = Arrays.asList(); 
            
    @Override
    public String nameOf(Class<? extends Wizard> wizard)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String shortDescriptionOf(Class<? extends Wizard> wizard)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String detailedDescriptionOf(Class<? extends Wizard> wizard)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WizardCategory categoryOf(Class<? extends Wizard> wizard)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Class<? extends Wizard>> allWizards()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Class<? extends Wizard>> suitableWizards(Object ... criteries)
    {
        
        
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void runWizard(Class<? extends Wizard> wizard, Object... criteries)
    {
        // TODO Auto-generated method stub
        
    }
    
    
    private WizardAttribute attributes(Class<? extends Wizard> wizard)
    {
        if (wizard == null)
        {
            return null;
        }
        
        WizardAttribute attributes = wizard.getAnnotation(WizardAttribute.class);
        return attributes;
    }

}
