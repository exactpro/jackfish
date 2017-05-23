////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import java.util.List;

import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardResult;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

@WizardAttribute(
            name = "Test dictionary wizard",
            category = WizardCategory.GUI_DICTIONARY,
            shortDescription = "This wizard is only for test purpose.",
            detailedDescription = "Here you descrioption might be",
            strongCriteries = true,
            criteries = { IGuiDictionary.class, IWindow.class }
        )
public class DictionaryWizard extends AbstractWizard
{
    private IGuiDictionary  currentDictionary   = null;
    private IWindow         currentWindow       = null;
    
    public DictionaryWizard()
    {
    }

    @Override
    public void init(IContext context, Object ... parameters)
    {
        super.init(context, parameters);
        
        this.currentDictionary = super.get(IGuiDictionary.class, parameters);
        this.currentWindow     = super.get(IWindow.class, parameters);
    }

    @Override
    public WizardResult run()
    {
        super.run();
        
        DialogsHelper.showInfo("This is a small wizard " + this.currentDictionary + "  " + this.currentWindow);
        
        List<WizardCommand> commands = CommandBuilder
                .start()
                .build();
        
        return WizardResult.submit(commands);
    }

}
