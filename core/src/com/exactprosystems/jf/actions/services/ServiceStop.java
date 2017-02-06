////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.service.IService;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					= ActionGroups.Services,
		suffix					= "SRVSTP",
		generalDescription 		= "The following action is needed to stop a service that was run by {{@ServiceStart@}}.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Load MatrixService.`}}"
				+ "{{`2. Run a service with additional parameter Port, that was specified earlier.`}}"
				+ "{{`3. Close a connection.`}} "
				+ "{{##Id;#Action;#ServiceId\n"
				+ "SRVLD1;ServiceLoad;'MatrixService'\n"
				+ "\n"
				+ "#Id;#Action;#Port;#ServiceConnection\n"
				+ "SRVSTRT1;ServiceStart;10565;SRVLD1.Out\n"
				+ "\n"
				+ "#Id;#Action;#ServiceConnection\n"
				+ "SRVSTP1;ServiceStop;SRVLD1.Out#}}"
	)
public class ServiceStop extends AbstractAction 
{
	public final static String connectionName = "ServiceConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A connection with a service is specified, that needed to be closed." )
	protected ServiceConnection	connection	= null;

	public ServiceStop()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
		}
		else
		{
			IService service = connection.getService();
			service.stop();
			
			super.setResult(null);
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}
