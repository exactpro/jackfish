/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
				DialogGetProperties.class,
				DialogMove.class,
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
