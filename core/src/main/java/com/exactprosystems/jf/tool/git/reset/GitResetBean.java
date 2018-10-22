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

package com.exactprosystems.jf.tool.git.reset;

import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Date;
import java.util.List;

public class GitResetBean
{
	private RevCommit commit;
	private CredentialBean bean;

	public GitResetBean(RevCommit commit, CredentialBean bean)
	{
		this.commit = commit;
		this.bean = bean;
	}

	public String getCommitId()
	{
		return this.commit.getName();
	}

	public Date getDate()
	{
		return this.commit.getAuthorIdent().getWhen();
	}

	public String getMessage()
	{
		return this.commit.getFullMessage();
	}

	public String getUsername()
	{
		PersonIdent authorIdent = this.commit.getAuthorIdent();
		return authorIdent.getName() + "<" + authorIdent.getEmailAddress() + ">";
	}

	public List<FileWithStatusBean> getFiles() throws Exception
	{
		return GitUtil.getCommitFiles(bean, commit);
	}

	@Override
	public String toString()
	{
		return this.commit.toString();
	}
}
