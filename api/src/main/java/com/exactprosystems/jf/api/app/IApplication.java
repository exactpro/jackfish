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

import com.exactprosystems.jf.api.common.SerializablePair;

import java.util.Map;

public interface IApplication
{
	final static String serviceName = "MyAppServiceName";
	
	void 				init(IApplicationPool pool, IApplicationFactory factory) throws Exception;

    int                 reconnect(Map<String, String> parameters) throws Exception;
    /**
	 * key   : pid <br>
	 * value : port <br>
     * Deprecated use {@link IApplication#connect(ConnectionConfiguration, Map, Map)}
     */
    @Deprecated
    default SerializablePair<Integer, Integer> connect(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception {
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        configuration.setJar(jar);
        configuration.setStartPort(startPort);
        configuration.setWorkDirectory(work);
        configuration.setRemoteClassName(remoteClassName);

        return connect(configuration, driverParameters, parameters);
    }

	SerializablePair<Integer, Integer> connect(ConnectionConfiguration configuration, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception;
	/**
	 * key   : pid <br>
	 * value : port <br>
     * Deprecated {@link IApplication#start(ConnectionConfiguration, Map, Map)}
	 */
	@Deprecated
    default SerializablePair<Integer, Integer> start(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception {
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        configuration.setJar(jar);
        configuration.setStartPort(startPort);
        configuration.setWorkDirectory(work);
        configuration.setRemoteClassName(remoteClassName);

        return start(configuration, driverParameters, parameters);
    }

	SerializablePair<Integer, Integer> start(ConnectionConfiguration configuration, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception;
	void 				stop(boolean needKill) throws Exception;

	IRemoteApplication 	service();
	IApplicationPool	getPool();
	IApplicationFactory	getFactory();
}
