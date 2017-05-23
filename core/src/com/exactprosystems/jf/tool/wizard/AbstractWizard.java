////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import java.util.Arrays;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardResult;

public abstract class AbstractWizard implements Wizard
{
    protected IContext context;

    @Override
    public void init(IContext context, Object ... parameters)
    {
        this.context = context;
    }

    @Override
    public WizardResult run()
    {
        return WizardResult.deny();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }
    
    @SuppressWarnings("unchecked")
    protected <T> T get(Class<T> clazz, Object[] parameters)
    {
        if (parameters == null)
        {
            return null;
        }
        
        Object res = Arrays.stream(parameters)
                .filter(p -> p != null && clazz.isAssignableFrom(p.getClass()))
                .findFirst()
                .orElse(null);
        
        return (T)res;
    }
}
