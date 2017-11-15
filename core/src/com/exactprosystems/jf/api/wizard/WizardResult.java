////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

import java.util.Collections;
import java.util.List;

public class WizardResult
{
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
