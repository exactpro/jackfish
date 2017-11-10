////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.actions.app.*;
import com.exactprosystems.jf.actions.clients.*;
import com.exactprosystems.jf.actions.gui.*;
import com.exactprosystems.jf.actions.matrix.*;
import com.exactprosystems.jf.actions.message.*;
import com.exactprosystems.jf.actions.report.*;
import com.exactprosystems.jf.actions.services.*;
import com.exactprosystems.jf.actions.sql.*;
import com.exactprosystems.jf.actions.system.*;
import com.exactprosystems.jf.actions.tables.*;
import com.exactprosystems.jf.actions.text.*;
import com.exactprosystems.jf.actions.xml.*;

public class ActionsList 
{
	private ActionsList()
	{

	}

	public static final Class<?>[] actions = new Class<?>[]
			{
		        ApplicationConnectTo.class,
		        ApplicationGetProperties.class,
		        ApplicationSetProperties.class,
		        ApplicationRefresh.class,
		        ApplicationStart.class,
		        ApplicationStop.class,
		        ApplicationSwitchTo.class,
		        ApplicationResize.class,
				ApplicationCloseWindow.class,
				ApplicationNavigate.class,
				ApplicationMove.class,
		        ClientCheckMessage.class,
		        ClientCheckFields.class,
		        ClientClearMessages.class,
		        ClientConnect.class,
		        ClientCountMessages.class,
		        ClientEncode.class,
		        ClientDecode.class,
		        ClientGetMessage.class,
		        ClientLoad.class,
		        ClientSetProperties.class,
		        ClientSendMessage.class,
		        ClientSendMapMessage.class,
		        ClientSendRawMessage.class,
		        ClientStart.class,
		        ClientStop.class,
		        ServiceLoad.class,
		        ServiceStart.class,
		        ServiceStop.class,
		        Check.class,
		        Compare.class,
		        DesktopScreenshot.class,
		        DialogCheckLayout.class,
				DialogResize.class,
		        DialogClose.class,
		        DialogFill.class,
		        DialogAlert.class,
		        DialogSwitchToWindow.class,
		        DialogValidate.class,
		        MessageCheck.class,
		        MessageCompareTwo.class,
		        MessageCreate.class,
		        MessageReport.class,
		        Execute.class,
		        Input.class,
		        ReportStart.class,
		        ReportFinish.class,
		        ReportShow.class,
		        ImageGet.class,
		        ImageSave.class,
		        ImageReport.class,
		        MatrixRunFromText.class,
		        MatrixRun.class,
		        MatrixWait.class,
		        ChartReport.class,
		        Print.class,
		        Report.class,
		        ReportName.class,
		        Restore.class,
		        ResultTable.class,
		        ResultTableUserValue.class,
		        Show.class,
		        Store.class,
		        SQLconnect.class,
		        SQLdisconnect.class,
		        SQLexecute.class,
		        SQLselect.class,
		        SQLinsert.class,
		        SQLtableUpload.class,
		        TableAddColumns.class,
		        TableAddValue.class,
				TableColumnRename.class,
		        TableCompareTwo.class,
		        TableConsiderColumnsAs.class,
		        TableCreate.class,
		        TableEdit.class,
		        TableGetRowIndexes.class,
		        TableLeftJoin.class,
		        TableColumnAsList.class,
				TableRemoveColumns.class,
		        TableLoadFromFile.class,
				TableLoadFromDir.class,
				TableRemoveRow.class,
				TableReplace.class,
		        TableReport.class,
		        TableSaveToFile.class,
		        TableSelect.class,
		        TableSetValue.class,
				TableSort.class,
				TableUnion.class,
		        TextAddLine.class,
		        TextCreate.class,
		        TextLoadFromFile.class,
		        TextPerform.class,
                TextReport.class,
		        TextSaveToFile.class,
		        TextSetValue.class,
		        Wait.class,
				XmlCreate.class,
				XmlAddNode.class,
				XmlChildren.class,
				XmlCompare.class,
				XmlFindFirst.class,
				XmlFromText.class,
				XmlLoadFromFile.class,
				XmlSelect.class,
				XmlSelectFirst.class,
				XmlRemove.class,
				XmlReport.class,
				XmlSaveToFile.class,
				XmlSetNode.class, 
				ApplicationNewInstance.class, 
			};

}
