////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.utils;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

@Deprecated
public class JfService<T> extends Service<T>
{
    private Supplier<T> resultSupplier;
    
    public JfService(Executor executor, Supplier<T> resultSupplier)
    {
        this.resultSupplier = resultSupplier;
        setExecutor(executor);
        
    }
    

    @Override
    protected Task<T> createTask()
    {
        return new Task<T>()
        {
            @Override
            protected T call() throws Exception
            {
                if (resultSupplier != null)
                {
                    return resultSupplier.get();
                }
                return null;
            }
        };
    }
    
}