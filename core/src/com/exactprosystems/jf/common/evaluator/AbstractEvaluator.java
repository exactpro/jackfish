/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.common.evaluator;

import com.exactprosystems.jf.api.app.ITemplateEvaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEvaluator implements ITemplateEvaluator
{
	public static final String              EVALUATOR_NAME = "evaluator";
	public static final String              VERSION_NAME   = "config_version";
	private             Map<String, Object> initVars       = new HashMap<>();

	public void init(String key, Object value)
	{
		this.initVars.put(key, value);
	}

	//region public final methods
	public final void reset(String version)
	{
		this.getLocals().getVars().clear();
		this.getGlobals().getVars().clear();

		this.getGlobals().getVars().putAll(this.initVars);
		this.getGlobals().set(EVALUATOR_NAME, this);
		this.getGlobals().set(VERSION_NAME, version);
	}

	/**
	 * @param expression which need be compiled
	 * @return a compiled object from a expression
	 * @throws Exception if compile was failed
	 */
	public final Object compile(String expression) throws Exception
	{
		return expression == null ? null : this.rawCompile(expression);
	}

	/**
	 * @param compiled which need be executed
	 * @return a executed object from a compiled object
	 * @throws Exception if executing was failed
	 */
	public final Object execute(Object compiled) throws Exception
	{
		return compiled == null ? null : this.rawExecute(compiled);
	}

	/**
	 * @param expression which need be evaluating
	 * @return a evaluating object from a expression
	 * @throws Exception if evaluating was failed
	 */
	public final Object evaluate(String expression) throws Exception
	{
		return expression == null ? null : this.rawEvaluate(expression);
	}

	/**
	 * Try to evaluate expression. If evaluating was failed, will return null.
	 * @param expression which need be evaluating
	 * @return a evaluating object from a expression or null, if evaluating was failed
	 */
	public final Object tryEvaluate(String expression)
	{
		if (expression == null)
		{
			return null;
		}
		
	    Object retValue = null;
		try
		{
			retValue = this.rawEvaluate(expression);
		}
		catch (Exception ignored)
		{}
		
		return retValue;
	}

	/**
	 * Replace all templates from the passed template to string representation of a object
	 * @param template which need be evaluated
	 * @return evaluated text
	 * @throws Exception if evaluated was failed.
	 */
	public final String templateEvaluate(String template) throws Exception
	{
		return template == null ? null : this.rawTemplateEvaluate(template);
	}

	/**
	 * Try to replace all templates from the passed template to string representation of a object
	 * @param template which need be evaluated
	 * @return evaluated text or @{null} value, if replacing was failed
	 */
	public final String tryEvaluateTemplate(String template) {
		try
		{
			return this.templateEvaluate(template);
		}
		catch (Exception e)
		{
			;//nothing do
		}
		return null;
	}
	//endregion

	//region public abstract methods
	public abstract void addImports(Collection<String> imports);

	public abstract Variables getGlobals();

	public abstract Variables createLocals();

	public abstract Variables getLocals();

	public abstract void setLocals(Variables vars);

	public abstract String createString(String val);
	//endregion

	//region protected abstract methods
	protected abstract Object rawCompile(String expression)  throws Exception;

	protected abstract Object rawEvaluate(String expression) throws Exception;

	protected abstract Object rawExecute(Object compiled) throws Exception;

	protected abstract String rawTemplateEvaluate(String expression) throws Exception;
	//endregion
}
