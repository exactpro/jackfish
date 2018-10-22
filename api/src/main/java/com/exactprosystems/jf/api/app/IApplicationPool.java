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

import com.exactprosystems.jf.api.common.IPool;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IApplicationPool extends IPool
{
	List<String> appNames();
	
	Set<ControlKind> 	supportedControlKinds(String id) throws Exception;
	
	boolean 			isLoaded(String id);
	IApplicationFactory	loadApplicationFactory(String id) throws Exception;
	AppConnection 		connectToApplication(String id, Map<String, String> args) throws Exception;
	AppConnection 		startApplication(String id, Map<String, String> params) throws Exception;
    void                reconnectToApplication(AppConnection connection, Map<String, String> args) throws Exception;
    List<AppConnection> getConnections();
	void 				stopApplication(AppConnection connection, boolean needKill) throws Exception;
	void 				stopAllApplications(boolean needKill) throws Exception;
}
