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
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
            name = "Small wizard",
            pictureName = "SmallWizard.png",
            category = WizardCategory.MATRIX,
            shortDescription = "This wizard adds thre Let items to a matrix before current item.",
            detailedDescription = "Once upon a {{*time*}} far far away two robbers lived...",
            strongCriteries = false,
            criteries = { MatrixItem.class, MatrixFx.class }
        )
public class SmallWizard extends AbstractWizard
{
    private MatrixFx    currentMatrix   = null;
    private MatrixItem  currentItem     = null;
    private MatrixItem  parentItem      = null;
    private int         index           = 0;
    
    public SmallWizard()
    {
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        
        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentItem   = super.get(MatrixItem.class, parameters);
        this.parentItem    = this.currentItem.getParent();
        this.index = this.parentItem.index(this.currentItem);
    }

    @Override
    protected Supplier<List<WizardCommand>> initDialog(BorderPane borderPane)
    {
        borderPane.setCenter(new Button("test"));
        
        return () ->
        {
            MatrixItem let1 =  CommandBuilder.create(this.currentMatrix, Tokens.Let.name(), null);
            MatrixItem let2 =  CommandBuilder.create(this.currentMatrix, Tokens.For.name(), null);
            let1.getComments().add(new CommentString("let 1"));
            let2.getComments().add(new CommentString("let 2"));
            
            List<WizardCommand> commands = CommandBuilder
                    .start()
                    .addMatrixItem(this.currentMatrix, this.parentItem, let1, this.index)
                    .addMatrixItem(this.currentMatrix, this.parentItem, let2, ++this.index)
//                    .addMatrixItem(this.currentMatrix, this.parentItem, let3, this.index)
//                    .removeMatrixItem(this.currentMatrix, this.currentItem, 5)
                    .build();
            
            return commands;
        };
    }
}
