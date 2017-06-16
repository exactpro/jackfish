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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ExecuteResult;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.ProcessTools;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

@ActionAttribute(
		group					= ActionGroups.System,
		suffix					= "EXEC",
		generalDescription 		= "The following action is needed to run external processes via command line of the operating system.\n"
				+ "Depends on the operating system which is used, different commands can be used for the same actions in different systems.\n"
				+ "Using this action one can unpack an archive or download a file from FTP server.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Result of the standard output of the running process to the console.\n"
				+ "Available if stated true in parameter wait.\n"
				+ "One can get a text using Out.Text, to get ExitCode use Out.ExitCode, get PID use Out.PID.",
		outputType				= ExecuteResult.class,
		examples = "{{`1. Run system command “help”`}}"
				+ "2. Check if you get an answer in the form of text when executing the current command.\n"
				+ "{{#\n#Id;#Action;#Command\n"
				+ "EXEC1;Execute;'help'\n"
				+ "#Assert;#Message\n"
				+ "Str.IsNullOrEmpty(EXEC1.Out.Text);'Command wasn't executed'#}}"
	)
public class Execute extends AbstractAction 
{
	public final static String commandName = "Command";
	public final static String waitName = "Wait";
	public final static String workDirName = "WorkDir";

	@ActionFieldAttribute(name = commandName, mandatory = true, description = "A system command that will be executed.")
	protected String command 	= "";

	@ActionFieldAttribute(name = waitName, mandatory = false, def = DefaultValuePool.True, description = "waiting for the command to terminate. "
			+ "If true - execution of the matrix is stopped until the command is executed. By default - true.")
	protected Boolean wait; 
	
	@ActionFieldAttribute(name = workDirName, mandatory = false, def = DefaultValuePool.Null, description = "A task of the working directory for the"
			+ " current command.")
	protected String workDir;
	
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
		Process process = null;
		Runtime runtime = Runtime.getRuntime();
		if (this.workDir != null)
		{
			process = runtime.exec(this.command, null, new File(this.workDir));
		}
		else
		{
			process = runtime.exec(this.command);
		}

	    StringBuilder sb = new StringBuilder();
	    int exitCode = 0;
	    int pid = ProcessTools.processId(process);
		
		if (this.wait)
		{
		    try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
		    {
			    String line = "";			
			    while ((line = reader.readLine()) != null) 
			    {
			    	sb.append(line + "\n");
			    }		
		    }
		    try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream())))
		    {
			    String line = "";			
			    while ((line = reader.readLine()) != null) 
			    {
			    	sb.append(line + "\n");
			    }		
		    }
		    exitCode = process.waitFor();
		    
		}
	 
		super.setResult(new ExecuteResult(sb.toString(), exitCode, pid));
	}

}
