////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.all.*;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WizardManagerImpl implements WizardManager
{
    private static final Logger logger = Logger.getLogger(WizardManagerImpl.class);

    private static List<Class<? extends Wizard>> knownWizards = Arrays.asList(
            GherkinWizard.class,
			DictionaryWizard.class,
			SupportedControlWizard.class,
			CodeExampleWizard.class,
			XpathWizard.class,
			RefactorWizard.class,
			AutomateConvertWizard.class,
			NameSpaceWizard.class,
			ConnectionWizard.class,
			DialogFillWizard.class
	);
    
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
    public void runWizard(Class<? extends Wizard> wizard, Context context, Object... parameters)
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
            
            if (wiz.beforeRun())
            {
                WizardResult result = wiz.run();
                if (result.submitted())
                {
                    List<WizardCommand> commands = result.commands();
                    commands.forEach(c -> c.apply(context));
					DialogsHelper.showSuccess("Wizard {" + this.nameOf(wizard) + "} success");
				}
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
        
        if (attr.experimental() && !VersionInfo.isDevVersion())
        {
        	return false;
        }
        
        Class<?>[] criteriaClasses = Arrays.stream(criteries)
                .filter(o -> o != null)
                .map(o -> o.getClass())
                .toArray(Class[]::new);

        boolean res = contains(criteriaClasses, attr.criteries());
        if (attr.strongCriteries())
        {
            res = res && contains(attr.criteries(), criteriaClasses);
        }
        return res;
    }

    private boolean contains(Class<?>[] expected, Class<?>[] actual)
    {
        return Arrays.stream(expected).allMatch(c -> Arrays.stream(actual).anyMatch(cc -> c.isAssignableFrom(cc) || cc.isAssignableFrom(c)));
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
