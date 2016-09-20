////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 					= ActionGroups.System, 
		suffix 					= "", 
		generalDescription 		= "Sets the only additional fields into current record of result table", 
		additionFieldsAllowed 	= true 
)
public class ResultTableUserValue extends AbstractAction
{
	public final static String	matrixName		= "Matrix";

	@ActionFieldAttribute(name = matrixName, mandatory = false, description = "If it is used then the result table for the matrix will be used.")
	protected MatrixRunner		matrix			= null;

	@Override
	public void initDefaultValues()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Table result = this.matrix == null ? context.getTable() : this.matrix.getTable();
		if (result == null)
		{
			super.setError("The result table is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		int size = result.size();
		if (size == 0)
		{
			super.setError("The result table is empty", ErrorKind.FAIL);
			return;
		}
		Set<String> predefinedColumns = new HashSet<>(Arrays.asList(Context.resultColumns));
		
		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
			String name = parameter.getName();
			Object value = parameter.getValue();
			if (predefinedColumns.contains(name))
			{
				continue;
			}

			boolean found = false;
			for (int i = 0; i < result.getHeaderSize(); i++)
			{
				if (result.getHeader(i).equals(name))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				result.addColumns(name);
			}
			
			result.changeValue(name, size - 1, value);
		}

		super.setResult(null);
	}
}
