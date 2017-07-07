////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.xmltree.XmlTreeView;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.ImageAndOffset;

import javafx.concurrent.Service;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

@WizardAttribute(
        name 				= "Test dictionary wizard",
        pictureName 		= "DictionaryWizard.png",
        category 			= WizardCategory.GUI_DICTIONARY,
        shortDescription 	= "This wizard is only for test purpose.",
        detailedDescription = "Here you descrioption might be",
        experimental 		= true,
        strongCriteries 	= true,
        criteries 			= { DictionaryFx.class, Window.class }
    )
public class DictionaryWizard extends AbstractWizard
{
    private AppConnection               currentConnection  = null;
    private DictionaryFx                currentDictionary  = null;
    private Window                      currentWindow      = null;

    private ExecutorService             executor           = null;
    private Service<ImageAndOffset>     imageService       = null;
    private Service<Document>           documentService    = null;

    private volatile ImageAndOffset     imageAndOffset     = null;
    private volatile Document           document           = null;
    private volatile Node               currentNode        = null;

    private ImageViewWithScale          imageViewWithScale = null;
    private XmlTreeView                 xmlTreeView        = null;
    private FindPanel<org.w3c.dom.Node> findPanel          = null;
    
    public DictionaryWizard()
    {
        this.executor = Executors.newFixedThreadPool(1);
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        
        this.currentConnection = super.get(AppConnection.class, parameters);
        this.currentDictionary = super.get(DictionaryFx.class, parameters);
        this.currentWindow     = super.get(Window.class, parameters);
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
        boolean res =  this.currentConnection != null && this.currentConnection.isGood();
        if (!res)
        {
            DialogsHelper.showError("Application is not started.\nStart it before call the wizard.");
        }
        return res;
    }

}
