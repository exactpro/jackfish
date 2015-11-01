////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

@ActionAttribute(
		group					= ActionGroups.System,
		suffix					= "EXEC",
		generalDescription 		= "Executes system command.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Result of output given command.",
		outputType				= String.class
	)
public class Execute extends AbstractAction 
{
	public final static String commandName = "Command";
	public final static String waitName = "Wait";
	public final static String workDirName = "WorkDir";

	@ActionFieldAttribute(name = commandName, mandatory = true, description = "System command that will be executed.")
	protected String command 	= "";

	@ActionFieldAttribute(name = waitName, mandatory = false, description = "Wait while process will finish.")
	protected Boolean wait 	= true; 
	
	@ActionFieldAttribute(name = workDirName, mandatory = false, description = "Working directory for this command.")
	protected String workDir 	= null;

	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		switch (fieldName)
		{
			case waitName: 
				return HelpKind.ChooseFromList;
			case workDirName:
				return HelpKind.ChooseFolder;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case waitName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;

			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Process p = null;
		Runtime runtime = Runtime.getRuntime();
		if (this.workDir != null)
		{
			p = runtime.exec(this.command, null, new File(this.workDir));
		}
		else
		{
			p = runtime.exec(this.command);
		}

	    StringBuilder sb = new StringBuilder();
		
		if (this.wait)
		{
		    try(BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream())))
		    {
			    String line = "";			
			    while ((line = reader.readLine()) != null) 
			    {
			    	sb.append(line + "\n");
			    }		
		    }
		    try(BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream())))
		    {
			    String line = "";			
			    while ((line = reader.readLine()) != null) 
			    {
			    	sb.append(line + "\n");
			    }		
		    }
		    p.waitFor();
		    
		}
	 
		super.setResult(sb.toString());
	}
}
