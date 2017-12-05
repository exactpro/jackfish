////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.ProcessTools;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.System,
		suffix					   = "EXEC",
		constantGeneralDescription = R.EXECUTE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.EXECUTE_OUTPUT_DESC,
		outputType				   = ExecuteResult.class,
		constantExamples 		   = R.EXECUTE_EXAMPLE
	)
public class Execute extends AbstractAction 
{
	public static final String commandName = "Command";
	public static final String waitName    = "Wait";
	public static final String workDirName = "WorkDir";

	@ActionFieldAttribute(name = commandName, mandatory = true, constantDescription = R.EXECUTE_COMMAND)
	protected String command;

	@ActionFieldAttribute(name = waitName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.EXECUTE_WAIT)
	protected Boolean wait;

	@ActionFieldAttribute(name = workDirName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.EXECUTE_WORK_DIR)
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
		if (waitName.equals(parameterToFill))
		{
			list.add(ReadableValue.TRUE);
			list.add(ReadableValue.FALSE);
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Runtime runtime = Runtime.getRuntime();
		Process process;
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
			this.readFromStream(process.getInputStream(), sb);
			this.readFromStream(process.getErrorStream(), sb);
			exitCode = process.waitFor();
		}

		super.setResult(new ExecuteResult(sb.toString(), exitCode, pid));
	}

	private void readFromStream(InputStream stream, StringBuilder sb) throws IOException
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset())))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line).append("\n");
			}
		}
	}
}
