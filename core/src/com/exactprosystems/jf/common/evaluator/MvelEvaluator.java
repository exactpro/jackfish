////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.evaluator;

import com.exactprosystems.jf.api.common.Unique;
import com.exactprosystems.jf.api.conditions.Condition;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MvelEvaluator extends AbstractEvaluator 
{
    private ParserContext context = new ParserContext();
    private Variables     globals = new MvelVariables();
    private Variables     locals  = new MvelVariables();

    public MvelEvaluator()
	{
	}
	
	@Override
	protected Object rawCompile(String expression)  throws Exception
	{
		return MVEL.compileExpression(expression, this.context);
	}

	@Override
	protected Object rawExecute(Object compiled) throws Exception
	{
		if (compiled instanceof Serializable)
		{
			Serializable expr = (Serializable)compiled;
			return MVEL.executeExpression(expr, makeVars());
		}
		throw new Exception("Wrong type of precompiled: " + compiled);
	}

	@Override
	protected Object rawEvaluate(String expression) throws Exception
	{
		Serializable expr = MVEL.compileExpression(expression, this.context);
		return MVEL.executeExpression(expr, makeVars());
	}

	@Override
	protected String rawTemplateEvaluate(String expression)
	{
		CompiledTemplate expr = TemplateCompiler.compileTemplate(expression);
		return (String)TemplateRuntime.execute(expr, makeVars());
	}

	@Override
	public Variables getGlobals()
	{
		return this.globals;
	}

	@Override
	public Variables createLocals()
	{
		 MvelVariables vars = new MvelVariables();
	     vars.set(this.locals.getVars());
		 return vars;
	}

	@Override
	public Variables getLocals()
	{
		return this.locals;
	}

	@Override
	public void setLocals(Variables vars)
	{
		this.locals = vars;
	}

	@Override
	public void addImports(Collection<String> imports)
	{
		// JeckFish and MVEL could be loaded by different ClassLoaders (for example, by SF class-loader).
		// Specify proper classloader to load imports from this JAR
		this.context.getParserConfiguration().setClassLoader(this.getClass().getClassLoader());
		
		this.context.addPackageImport("java.util");
		this.context.addPackageImport(Condition.class.getPackage().getName());
		this.context.addPackageImport(Unique.class.getPackage().getName());
		
		for (String imp : imports)
		{
			this.context.addPackageImport(imp);
		}
	}
	
	@Override
	public String createString(String val)
	{
		if (val != null)
		{
			return "'" + val + "'";
		}
		return null;
	}
	
    private Map<String, Object> makeVars()
    {
        Map<String, Object> vars = new HashMap<>();
        vars.putAll(this.globals.getVars());
        vars.putAll(this.locals.getVars());
        return vars;
    }
}
