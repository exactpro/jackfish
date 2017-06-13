////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.io.Serializable;
import java.util.Date;

public class CookieBean implements Serializable 
{
    private static final long serialVersionUID = 322552609827913369L;

    public String   name;
    public String   value;
    public String   path;
    public String   domain;
    public Date     expiry;
    public boolean  isSecure;
    public boolean  isHttpOnly;
    
    public CookieBean(String name, String value)
    {
        this.name = name;
        this.value = value;
    }
    
    public CookieBean setPath(String path)
    {
        this.path = path;
        return this;
    }

    public CookieBean setDomain(String domain)
    {
        this.domain = domain;
        return this;
    }

    public CookieBean setExpary(Date expiry)
    {
        this.expiry = expiry;
        return this;
    }
    
    public CookieBean setSecure(boolean isSecure)
    {
        this.isSecure = isSecure;
        return this;
    }
    public CookieBean setHttpOnly(boolean isHttpOnly)
    {
        this.isHttpOnly = isHttpOnly;
        return this;
    }
    
    @Override
    public String toString()
    {
        return "Cookie{" + this.name + ":" + this.value + "}";
    }
}
