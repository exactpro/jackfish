////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents;

import java.net.URL;
import java.util.ResourceBundle;

import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.ContainingParent;

import javafx.fxml.Initializable;
import javafx.scene.Parent;

public abstract class AbstactDocumentController<T extends Document> implements Initializable, ContainingParent
{
    protected T model;
    protected Parent parent;

    public AbstactDocumentController()
    {
    }
    
    public void setParent(Parent parent)
    {
        this.parent = parent;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
    }
    
    @SuppressWarnings("unchecked")
    protected void init(Document model)
    {
        this.model = (T)model;
    }
    
    protected abstract void close();
}
