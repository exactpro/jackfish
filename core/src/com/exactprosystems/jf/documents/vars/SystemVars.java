////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.vars;

import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.LinkedProperties;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@DocumentInfo(
		newName = "NewVars", 
		extentioin = "ini", 
		description = "System variables"
)
public class SystemVars extends AbstractDocument
{
	public final static String NAME_VARIABLES = "variables";

    public SystemVars(String fileName, DocumentFactory factory)
    {
    	super(fileName, factory);

        parameters = new Parameters();
    }

    //------------------------------------------------------------------------------------------------------------------
    // interface Document
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public void load(Reader reader) throws Exception
	{
    	LinkedProperties prop = new LinkedProperties();
        prop.load(reader);
        this.parameters.clear();
        
        for (Map.Entry<Object, Object> entry : prop.entrySet())
        {
            this.parameters.add(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
	}

    @Override
    public boolean canClose()  throws Exception
    {
    	return true;
    }

    @Override
    public void save(String fileName) throws Exception
    {
    	super.save(fileName);
    	
    	LinkedProperties prop = new LinkedProperties();
        for (Parameter var : parameters)
        {
            prop.put(var.getName(), var.getExpression());
        }
        
    	prop.store(new FileWriter(fileName), null);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    // interface Mutable
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
        return this.parameters.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
        this.parameters.saved();
    }
    
    //------------------------------------------------------------------------------------------------------------------
    public List<Parameter> getVariables()
    {
        ArrayList<Parameter> res = new ArrayList<>();
        for (Parameter parameter : parameters)
        {
            res.add(parameter);
        }
        return res;
    }

    public void setParameters(List<Parameter> parameters)
    {
        this.parameters.clear();
        for (Parameter parameter : parameters)
        {
            this.parameters.add(parameter.getName(), parameter.getExpression());
        }
    }

    public Parameters getParameters()
    {
        return this.parameters;
    }

    public List<Parameter> getParameterList()
    {
        ArrayList<Parameter> res = new ArrayList<>();
        for (Parameter parameter : parameters)
        {
            res.add(parameter);
        }
        return res;
    }

	public void injectVariables(AbstractEvaluator evaluator) throws Exception
	{
		this.evaluator = evaluator;
		for (Parameter entry : this.parameters)
		{
			String key = entry.getName();
			String value = entry.getExpression();
			Object result = evaluator.evaluate(value);
			evaluator.init(key, result);
		}
	
	}

	protected AbstractEvaluator evaluator;
	
    private Parameters parameters;
}
