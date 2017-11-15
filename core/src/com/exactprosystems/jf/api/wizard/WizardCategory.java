////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

public enum WizardCategory
{
    MATRIX              ("Matrix wizard"),
    CONFIGURATION       ("Configuration wizard"),
    GUI_DICTIONARY      ("GUI dictionary wizard"),
    MESSAGE_DICTIONARY  ("Message dictionary wizard"),
    OTHER               ("Universal wizard");
    
    private WizardCategory(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return this.name;
    }

    private String name; 
}
