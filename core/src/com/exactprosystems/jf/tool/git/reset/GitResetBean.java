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
