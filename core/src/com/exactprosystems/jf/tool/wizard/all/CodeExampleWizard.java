/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
        name 				= R.CODE_EXAMPLE_WIZARD_NAME,
        pictureName 		= "GherkinWizard.jpg",
        category 			= WizardCategory.MATRIX,
        shortDescription 	= R.CODE_EXAMPLE_WIZARD_SHORT_DESCRIPTION,
        detailedDescription = R.CODE_EXAMPLE_WIZARD_DETAILED_DESCRIPTION,
        experimental 		= true,
        strongCriteries 	= true,
        criteries 			= { MatrixItem.class, MatrixFx.class }
    )
public class CodeExampleWizard extends AbstractWizard
{
    private MatrixItem     	currentItem    	= null;
    private TextArea 		text 			= null;
    
    public CodeExampleWizard()
    {
    }

    @Override
    public void init(Context context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        
        this.currentItem = super.get(MatrixItem.class, parameters);
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
    	String string = textFromMatrix();
    	this.text = new TextArea(string);
        borderPane.setCenter(this.text);

        borderPane.setPrefSize(800.0, 600.0);
        borderPane.setMinSize(800.0, 600.0);
    }

	private String textFromMatrix()
	{
		String string;
    	
		try
		{
			List<MatrixItem> list = new ArrayList<>();
			this.currentItem.bypass(i -> { if (i != this.currentItem) {list.add(i); } });
			if (list.isEmpty())
			{
				list.add(this.currentItem);
			}
			Parser parser = new Parser();
			string = parser.itemsToString(list.toArray(new MatrixItem[] {}));
		} 
		catch (Exception e)
		{
			string = e.getMessage();
		}
		
		StringBuilder sb = new StringBuilder("    \"{{#");
		String prefix = "";
		for(String part : string.split("\n"))
		{
			sb.append(prefix).append(part);
			prefix = "\\n\"\n    + \"";
		}
		sb.append("#}}\"\n");
		
		return sb.toString();
	}

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return () -> CommandBuilder
                .start()
                .clipboard(this.text.getText())
                .build();
    }

    @Override
    public boolean beforeRun()
    {
        return true;
    }

}
