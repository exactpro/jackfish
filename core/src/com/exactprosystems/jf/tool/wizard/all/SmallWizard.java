////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import java.util.List;

@WizardAttribute(
            name = "Small wizard",
            category = WizardCategory.MATRIX,
            shortDescription = "This wizard adds thre Let items to a matrix before current item.",
            detailedDescription = "Once upon a {{*time*}} far far away two robbers lived...",
            strongCriteries = false,
            criteries = { MatrixItem.class, Matrix.class }
        )
public class SmallWizard extends AbstractWizard
{
    private Matrix      currentMatrix   = null;
    private MatrixItem  currentItem     = null;
    
    public SmallWizard()
    {
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        
        this.currentMatrix = super.get(Matrix.class, parameters);
        this.currentItem   = super.get(MatrixItem.class, parameters);
    }

    @Override
    public WizardResult run()
    {
        super.run();
        
//        DialogsHelper.showInfo("This is a small wizard");
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
        
        List<WizardCommand> commands = CommandBuilder
                .start()
                .addMatrixItem(this.currentMatrix, this.currentItem, 5)
                .removeMatrixItem(this.currentMatrix, this.currentItem, 5)
                .build();
        
        return WizardResult.submit(commands);
    }

}
