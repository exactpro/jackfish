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
package com.exactprosystems.jf.tool.git.pull;

import com.exactprosystems.jf.api.common.i18n.R;

public class GitPullBean
{
	private final String fileName;
	private boolean needMerge;

	public GitPullBean(String fileName, boolean needMerge)
	{
		this.fileName = fileName;
		this.needMerge = needMerge;
	}

	public String getFileName()
	{
		return fileName;
	}

	public boolean isNeedMerge()
	{
		return needMerge;
	}

	public void resolve()
	{
		this.needMerge = false;
	}

	@Override
	public String toString()
	{
		return String.format(R.GIT_PULL_BEAN_NEED_MERGE.get(), this.fileName, this.needMerge);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GitPullBean that = (GitPullBean) o;

		return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;

	}

	@Override
	public int hashCode()
	{
		return fileName != null ? fileName.hashCode() : 0;
	}
}
