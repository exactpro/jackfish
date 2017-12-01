////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

import com.exactprosystems.jf.api.common.i18n.R;

public enum WizardCategory
{
    MATRIX              (R.WIZARD_CATEGORY_MATRIX),
    CONFIGURATION       (R.WIZARD_CATEGORY_CONFIGURATION),
    GUI_DICTIONARY      (R.WIZARD_CATEGORY_GUI_DIC),
    MESSAGE_DICTIONARY  (R.WIZARD_CATEGORY_MESSAGE_DIC),
    OTHER               (R.WIZARD_CATEGORY_OTHER);
    
    private WizardCategory(R name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return this.name.get();
    }

    private R name;
}
