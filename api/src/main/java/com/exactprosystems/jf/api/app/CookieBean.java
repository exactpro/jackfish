/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
