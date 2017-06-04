////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
        name 				= "Test dictionary wizard",
        pictureName 		= "DictionaryWizard.png",
        category 			= WizardCategory.GUI_DICTIONARY,
        shortDescription 	= "This wizard is only for test purpose.",
        detailedDescription = "Here you descrioption might be",
        experimental 		= true,
        strongCriteries 	= true,
        criteries 			= { IGuiDictionary.class, IWindow.class }
    )
public class DictionaryWizard extends AbstractWizard
{
    private IGuiDictionary  currentDictionary   = null;
    private IWindow         currentWindow       = null;
    
    public DictionaryWizard()
    {
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        
        this.currentDictionary = super.get(IGuiDictionary.class, parameters);
        this.currentWindow     = super.get(IWindow.class, parameters);
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
        borderPane.setCenter(new Text("test"));
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return () ->
        {
            List<WizardCommand> commands = CommandBuilder
                    .start()
                    .build();
            
            return commands;
        };
    }

    @Override
    public boolean beforeRun()
    {
        boolean res =  this.currentWindow.getName().startsWith("n");
        if (!res)
        {
            DialogsHelper.showError("Some error message");
        }
        return res;
    }

}
