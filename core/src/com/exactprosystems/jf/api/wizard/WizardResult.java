////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class WizardResult implements Serializable
{
    private static final long serialVersionUID = 2096698115010000991L;

    private boolean submitted = false;
    
    private List<WizardCommand> commands = Collections.emptyList(); 
    
    
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
