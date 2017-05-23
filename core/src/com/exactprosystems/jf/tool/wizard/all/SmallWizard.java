////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardResult;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;

@WizardAttribute(
            name = "Small wizard",
            category = WizardCategory.MATRIX,
            shortDescription = "This wizard adds thre Let items to a matrix before current item.",
            detailedDescription = "Once upon a {{*time*}} far far away two robbers lived...",
            criteries = { MatrixItem.class, Matrix.class }
        )
public class SmallWizard extends AbstractWizard
{
    private MatrixItem currentItem = null;
    
    public SmallWizard()
    {
    }

    @Override
    public void init(IContext context, Object ... parameters)
    {
        super.init(context, parameters);

        // TODO Auto-generated method stub
    }

    @Override
    public WizardResult run()
    {
        super.run();
        
        // TODO Auto-generated method stub
        return null;
    }

}
