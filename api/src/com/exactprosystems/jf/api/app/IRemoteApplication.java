////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.SerializablePair;

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

	Serializable                getProperty		(String name, Serializable prop) throws RemoteException;
    void                        setProperty     (String name, Serializable prop) throws RemoteException;

	void 						createLogger	(String logName, String serverLogLevel, String serverLogPattern) throws RemoteException;
    void                        setPluginInfo   (PluginInfo info) throws RemoteException;
	int 						connect			(Map<String, String> args) throws RemoteException;
	int 						run				(Map<String, String> args) throws RemoteException;
	void 						stop			(boolean needKill) throws RemoteException;
	SerializablePair<String, Boolean> getAlertText() throws RemoteException;
	void						navigate		(NavigateKind kind) throws RemoteException;
	void 						setAlertText	(String text, PerformKind performKind) throws RemoteException;
	void 						refresh			() throws RemoteException;
	Collection<String> 			titles			() throws RemoteException;
	void 						newInstance		(Map<String, String> args) throws Exception;
	String 						switchTo		(Map<String, String> criteria, boolean softCondition) throws RemoteException;
	void 						switchToFrame	(Locator owner, Locator element) throws RemoteException;
	void 						resize			(Resize resize, int height, int width, boolean maximize, boolean minimize,boolean normal) throws RemoteException;
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
}
