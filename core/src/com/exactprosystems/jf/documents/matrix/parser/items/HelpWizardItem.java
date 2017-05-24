////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

public class HelpWizardItem extends MatrixItem
{
    public HelpWizardItem(WizardManager manager, Class<? extends Wizard> itemClazz)
    {
        this.manager = manager;
        this.wizardClazz = itemClazz;
    }
    
    @Override
    public String getItemName()
    {
        return this.manager.nameOf(this.wizardClazz);
    }
    
    public void actionReport(ReportBuilder report)
    {
        String picture = this.manager.pictureOf(this.wizardClazz);
        String shortDescription = this.manager.shortDescriptionOf(this.wizardClazz);
        String detailedDescription = this.manager.detailedDescriptionOf(this.wizardClazz);
        
        report.itemIntermediate(this);
        
        report.outLine(this, null, "{{`" + shortDescription + "`}}", null);
        report.outImage(this, null, picture, "{{*Picture*}}", false); 
        report.outLine(this, null, "{{`" + detailedDescription + "`}}", null);
    }


    @Override
    protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
    {
        try
        {
            if (this.wizardClazz != null)
            {
                actionReport(report);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        return new ReturnAndResult(start, Result.Passed); 
    }
	
    private Class<? extends Wizard> wizardClazz;
    private WizardManager manager;
}
