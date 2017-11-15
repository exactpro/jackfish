////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.documents.matrix.parser.Parser;

public enum TypeMandatory
{
    System      (Parser.systemPrefix),
    Mandatory   (Parser.knownPrefix),
    NotMandatory(Parser.knownPrefix),
    Extra       ("");

    private TypeMandatory(String prefix)
    {
        this.prefix = prefix;
    }
    
    public String getPrefix()
    {
        return this.prefix;
    }
    
    private String prefix;
}
