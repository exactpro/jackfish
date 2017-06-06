////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.common.report.ReportHelper;

public enum DefaultValuePool
{
    Null            (null),
    Semicolon       (";"),
    EmptyString     (""),
    EmptyArrString  (new String[] {}),
    True            (true),
    False           (false), 
    Int20000        (20000),
    IntMin          (Integer.MIN_VALUE),
    Long0           (0L),
    ;
    
    DefaultValuePool (Object value)
    {
        this.value = value;
    }
    
    public Object getValue()
    {
        return this.value;
    }
    
    @Override
    public String toString()
    {
        return ReportHelper.objToString(this.value, false);
    }

    private Object value;
}
