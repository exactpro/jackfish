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

package com.exactprosystems.jf.api.app;

import java.awt.*;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

public interface IRemoteApplication extends Remote
{
	String rectangleName = "jf_rectangle";
	String visibleName = "jf_visible";
	String baseParnetName = "jf_base_parent";

	Serializable                getProperty		(String name, Serializable prop) throws RemoteException;
    void                        setProperty     (String name, Serializable prop) throws RemoteException;

	void 						createLogger	(String logName, String serverLogLevel, String serverLogPattern) throws RemoteException;
    void                        setPluginInfo   (PluginInfo info) throws RemoteException;
	int 						connect			(Map<String, String> args) throws RemoteException;
	int 						run				(Map<String, String> args) throws RemoteException;
	void 						stop			(boolean needKill) throws RemoteException;

	String 						getAlertText	() throws RemoteException;
	void						navigate		(NavigateKind kind) throws RemoteException;
	void 						setAlertText	(String text, PerformKind performKind) throws RemoteException;
	void 						refresh			() throws RemoteException;
	Collection<String> 			titles			() throws RemoteException;
	void 						newInstance		(Map<String, String> args) throws Exception;
	String 						switchTo		(Map<String, String> criteria, boolean softCondition) throws RemoteException;
	void 						switchToFrame	(Locator owner, Locator element) throws RemoteException;
	void 						resize			(Resize resize, int height, int width) throws RemoteException;
	ImageWrapper 				getImage		(Locator owner, Locator element) throws RemoteException;
	Rectangle 					getRectangle	(Locator owner, Locator element) throws RemoteException;
	Collection<String> 			findAll			(Locator owner, Locator element) throws RemoteException;
	OperationResult 			operate			(Locator owner, Locator element, Locator row, Locator header, Operation operation) throws RemoteException;
	CheckingLayoutResult 		checkLayout		(Locator owner, Locator element, Spec spec) throws RemoteException;
	int 						closeAll		(Locator element, Collection<LocatorAndOperation> operations) throws RemoteException;
	String 						closeWindow		() throws RemoteException;
	byte[]                      getTreeBytes    (Locator owner) throws RemoteException;

	void                        startNewDialog  () throws RemoteException;
	void 						moveWindow(int x, int y) throws RemoteException;

	void 						moveDialog(Locator owner, int x, int y) throws RemoteException;
	void						resizeDialog(Locator owner, Resize resize, int height, int width) throws RemoteException;
	Dimension					getDialogSize(Locator owner) throws RemoteException;
	Point						getDialogPosition(Locator owner) throws RemoteException;

}
