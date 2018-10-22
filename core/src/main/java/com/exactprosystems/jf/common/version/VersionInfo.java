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

package com.exactprosystems.jf.common.version;

public class VersionInfo
{
	private VersionInfo()
	{

	}

	private static final String LOCAL_BUILD = "LocalBuild";

	public static String getVersion()
	{
		Package pkg = VersionInfo.class.getPackage();
		String version = pkg.getImplementationVersion();

		return version == null ? LOCAL_BUILD : version;
	}

	public static boolean isDevVersion()
	{
		return getVersion().equals(LOCAL_BUILD);
	}
}
