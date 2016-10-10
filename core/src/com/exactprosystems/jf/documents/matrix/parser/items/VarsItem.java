////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;

@MatrixItemAttribute(
		description = "Create vars",
		shouldContain = {Tokens.VarsItem},
		mayContain = {Tokens.Off},
		real = true,
		hasValue = false,
		hasParameters = true,
		hasChildren = false)
public class VarsItem extends MatrixItem
{
    public VarsItem() {
        super();
    }

    @Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 1);
        driver.showComment(this, layout, 0, 0, getComments());
        driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
        driver.showTitle(this, layout, 1, 1, Tokens.VarsItem.get(), context.getFactory().getSettings());
        driver.showParameters(this, layout, 1, 3, this.parameters, null, false, false);
        driver.showCheckBox(this, layout, 1, 2, "Global", this.global, this.global);
		return layout;
	}
}
