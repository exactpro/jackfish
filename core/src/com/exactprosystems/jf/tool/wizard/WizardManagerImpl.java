////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.common.VerboseLevel;
import com.exactprosystems.jf.documents.ConsoleDocumentFactory;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.HelpItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.wizard.all.DictionaryWizard;
import com.exactprosystems.jf.tool.wizard.all.SmallWizard;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WizardManagerImpl implements WizardManager
{
    public static void main(String[] args)
    {
        DocumentFactory factory = new ConsoleDocumentFactory(VerboseLevel.All);
        Context context = factory.createContext();
        WizardManagerImpl manager = new WizardManagerImpl();
        
        Matrix matrix = new Matrix("Matrix", factory);
        MatrixItem item = new HelpItem(HelpItem.class);
        
        List<Class<? extends Wizard>> list = manager.suitableWizards(item, matrix);
        System.err.println("suitable = " + list);
        Class<? extends Wizard> wizard = list.get(0);
        manager.runWizard(wizard, context, item, matrix);
    }

    
    private static final Logger logger = Logger.getLogger(WizardManagerImpl.class);

    private static List<Class<? extends Wizard>> knownWizards = Arrays.asList(
            SmallWizard.class, DictionaryWizard.class); 
    
    public WizardManagerImpl()
    {
    }
            
    @Override
    public String nameOf(Class<? extends Wizard> wizard)
    {
        WizardAttribute attr = attributes(wizard);
        if (attr == null)
        {
            return null;
        }
        return attr.name();
    }

    @Override
    public String pictureOf(Class<? extends Wizard> wizard)
    {
        WizardAttribute attr = attributes(wizard);
        if (attr == null)
        {
            return null;
        }
        return attr.pictureName();
    }

    @Override
    public String shortDescriptionOf(Class<? extends Wizard> wizard)
    {
        WizardAttribute attr = attributes(wizard);
        if (attr == null)
        {
            return null;
        }
        return attr.shortDescription();
    }

    @Override
    public String detailedDescriptionOf(Class<? extends Wizard> wizard)
    {
        WizardAttribute attr = attributes(wizard);
        if (attr == null)
        {
            return null;
        }
        return attr.detailedDescription();
    }

    @Override
    public WizardCategory categoryOf(Class<? extends Wizard> wizard)
    {
        WizardAttribute attr = attributes(wizard);
        if (attr == null)
        {
            return null;
        }
        return attr.category();
    }

    @Override
    public List<Class<? extends Wizard>> allWizards()
    {
        return knownWizards;
    }

    @Override
    public List<Class<? extends Wizard>> suitableWizards(Object ... criteries)
    {
        return knownWizards.stream().filter(w -> isSuitable(w, criteries)).collect(Collectors.toList());
    }


    @Override
    public void runWizard(Class<? extends Wizard> wizard, IContext context, Object... parameters)
    {
        if (wizard == null)
        {
            return;
        }
        
        if (context == null)
        {
            return;
        }
        
        try
        {
            Wizard wiz = wizard.newInstance();
            wiz.init(context, this, parameters);
            
            WizardResult result = wiz.run();
            if (result.submitted())
            {
                List<WizardCommand> commands = result.commands();
                commands.forEach(c -> c.apply(context));
            }
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }
    
    private boolean isSuitable(Class<? extends Wizard> wizard, Object ... criteries)
    {
        if (wizard == null)
        {
            return false;
        }
        
        WizardAttribute attr = attributes(wizard);
        
        if (attr == null)
        {
            return false;
        }
        
        if (attr.strongCriteries() && criteries.length != attr.criteries().length)
        {
            return false;
        }
        
        List<Class<?>> criteriaClasses = Arrays.stream(criteries)
                .filter(o -> o != null)
                .map(o -> o.getClass())
                .collect(Collectors.toList());
        
        return Arrays.stream(attr.criteries())
                .allMatch(c -> criteriaClasses.stream().anyMatch(cc -> c.isAssignableFrom(cc)));
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
