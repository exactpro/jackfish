////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.evaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEvaluator 
{
	public final static String EVALUATOR_NAME   = "evaluator";
    public final static String VERSION_NAME     = "config_version";

	private Map<String, Object> initVars = new HashMap<>();

	public abstract void addImports(Collection<String> imports);
	
	public abstract Variables getGlobals();
	
	public abstract Variables createLocals();

	public abstract Variables getLocals();
	
	public abstract void setLocals(Variables vars);

	public abstract String createString(String val);

	public void init(String key, Object value)
	{
		this.initVars.put(key, value);
	}

	public final void reset(String version)
	{
		getLocals().getVars().clear();
		getGlobals().getVars().clear();
		
		getGlobals().getVars().putAll(this.initVars);
		getGlobals().set(EVALUATOR_NAME, this);
        getGlobals().set(VERSION_NAME, version);
	}

	public final Object compile(String expression) throws Exception
	{
		return expression == null ? null : rawCompile(expression);
	}

	public final Object execute(Object compiled) throws Exception
	{
		return compiled == null ? null : rawExecute(compiled);
	}

	public final Object evaluate(String expression) throws Exception
	{
		return expression == null ? null : rawEvaluate(expression);
	}
	
	public final Object tryEvaluate(String expression)
	{
		if (expression == null)
		{
			return null;
		}
		
	    Object retValue = null;
		try
		{
			retValue = rawEvaluate(expression);
		}
		catch (Exception ignored)
		{

		}
		
		return retValue;
	}

	public final String templateEvaluate(String template) throws Exception
	{
		return template == null ? null : rawTemplateEvaluate(template);
	}

	protected abstract Object rawCompile(String expression)  throws Exception;

	protected abstract Object rawEvaluate(String expression) throws Exception;

	protected abstract Object rawExecute(Object compiled) throws Exception;

	protected abstract String rawTemplateEvaluate(String expression) throws Exception;
}
