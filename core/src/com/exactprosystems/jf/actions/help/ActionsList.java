////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.help;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.app.*;
import com.exactprosystems.jf.actions.clients.*;
import com.exactprosystems.jf.actions.gui.*;
import com.exactprosystems.jf.actions.matrix.*;
import com.exactprosystems.jf.actions.message.*;
import com.exactprosystems.jf.actions.services.*;
import com.exactprosystems.jf.actions.sql.*;
import com.exactprosystems.jf.actions.system.*;
import com.exactprosystems.jf.actions.tables.*;
import com.exactprosystems.jf.actions.text.*;
import com.exactprosystems.jf.actions.xml.*;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;

@ActionAttribute(
		group					= ActionGroups.Help,
		generalDescription 		= "Prints a list of actions followed by short descriptions.",
		additionFieldsAllowed 	= false
	)
public class ActionsList extends AbstractAction 
{
	public static Class<?>[] actions = new Class<?>[]
			{
		        ActionsList.class,
		        ApplicationConnectTo.class,
		        ApplicationRefresh.class,
		        ApplicationStart.class,
		        ApplicationStop.class,
		        ApplicationSwitchTo.class,
				ApplicationCloseWindow.class,
		        MessageCheck.class,
		        ClientCheckMessage.class,
		        ClientClearMessages.class,
		        ClientConnect.class,
		        ClientCountMessages.class,
		        ClientCreateMapMessage.class,
		        ClientEncode.class,
		        ClientDecode.class,
		        ClientGetMessage.class,
		        ClientLoad.class,
		        ClientSendMessage.class,
		        ClientSendMapMessage.class,
		        ClientStart.class,
		        ClientStop.class,
		        ServiceLoad.class,
		        ServiceStart.class,
		        ServiceStop.class,
		        Check.class,
		        DialogFill.class,
		        MessageCompareTwo.class,
		        MessageCreate.class,
		        Execute.class,
		        ImageGet.class,
		        ImageSave.class,
		        ImageReport.class,
		        MatrixRunFromText.class,
		        MatrixRun.class,
		        MatrixWait.class,
		        Print.class,
		        Report.class,
		        ReportName.class,
		        Restore.class,
		        Store.class,
		        SQLconnect.class,
		        SQLdisconnect.class,
		        SQLexecute.class,
		        SQLselect.class,
		        SQLtableUpload.class,
		        TableAddColumns.class,
		        TableAddValue.class,
		        TableCompareTwo.class,
		        TableConsiderColumnsAs.class,
		        TableCreate.class,
				TableRemoveColumns.class,
		        TableLoadFromFile.class,
				TableRemoveRow.class,
				TableReplace.class,
		        TableReport.class,
		        TableSaveToFile.class,
		        TableSelect.class,
		        TableSetValue.class,
				TableSort.class,
		        TextAddLine.class,
		        TextCreate.class,
		        TextLoadFromFile.class,
		        TextPerform.class,
		        TextReport.class,
		        TextSaveToFile.class,
		        TextSetValue.class,
		        TestAction.class,
		        Wait.class,
				Vars.class,
				XmlAddNode.class,
				XmlCompare.class,
				XmlFindFirst.class,
				XmlLoadFromFile.class,
				XmlSelect.class,
				XmlSelectFirst.class,
				XmlRemove.class,
				XmlReport.class,
				XmlSaveToFile.class,
				XmlSetNode.class,
				ApplicationNewInstance.class,
				DialogClose.class
		    };
	
    public ActionsList()
    {
    }
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		doRealDocumetation(context, report);
		super.setResult(null);
	}

	protected void doRealDocumetation(Context context, ReportBuilder report)
	{
		ReportTable table = report.addTable("Registered actions", 100, new int[] { 20, 80 }, "Action", "Description");
		for (Class<?> clazz : actions)
		{
			table.addValues(clazz.getSimpleName(), clazz.getAnnotation(ActionAttribute.class).generalDescription());
		}
	}
	
}
