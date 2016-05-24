////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.git.status.GitStatusBean;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GitUtil
{
	private GitUtil()
	{

	}

	public static void gitPull(CredentialBean credentials, ProgressMonitor monitor) throws Exception
	{
		try (Git git = git(credentials))
		{
			//TODO think how to display result and how to get result
			git.pull().setCredentialsProvider(getCredentialsProvider(credentials)).setProgressMonitor(monitor).call();
		}
	}

	public static void gitClone(String remotePath, File projectFolder, CredentialBean credentials, ProgressMonitor monitor) throws Exception
	{
		CredentialsProvider credentialsProvider = getCredentialsProvider(credentials);
		try(Git git = Git.cloneRepository()
				.setURI(remotePath)
				.setDirectory(projectFolder)
				.setCredentialsProvider(credentialsProvider)
				.setProgressMonitor(monitor)
				.call()
		)
		{
			;
		}
	}

	public static List<GitStatusBean> gitStatus(CredentialBean credential) throws Exception
	{
		try (Git git = git(credential))
		{
			Status status = git.status().call();
			ArrayList<GitStatusBean> list = new ArrayList<>();
			status.getAdded().stream().map(st -> new GitStatusBean(GitStatusBean.Status.ADDED, new File(st))).forEach(list::add);
			status.getChanged().stream().map(st -> new GitStatusBean(GitStatusBean.Status.CHANGED, new File(st))).forEach(list::add);
			status.getRemoved().stream().map(st -> new GitStatusBean(GitStatusBean.Status.REMOVED, new File(st))).forEach(list::add);
			return list;
		}
	}

	private static Git git(CredentialBean bean) throws Exception
	{
		setSSH(bean);
		Repository localRepo = new FileRepositoryBuilder().findGitDir().build();
		return new Git(localRepo);
	}

	private static void setSSH(CredentialBean credentials)
	{
		CustomJschConfigSessionFactory jschConfigSessionFactory = new CustomJschConfigSessionFactory(credentials);
		if (jschConfigSessionFactory.isValid())
		{
			SshSessionFactory.setInstance(jschConfigSessionFactory);
		}
	}

	private static CredentialsProvider getCredentialsProvider(CredentialBean credentials)
	{
		return new CredentialsProvider()
		{
			@Override
			public boolean isInteractive()
			{
				return true;
			}

			@Override
			public boolean supports(CredentialItem... credentialItems)
			{
				return true;
			}

			@Override
			public boolean get(URIish urIish, CredentialItem... credentialItems) throws UnsupportedCredentialItem
			{
				for (CredentialItem item : credentialItems)
				{
					if (item instanceof CredentialItem.StringType)
					{
						((CredentialItem.StringType) item).setValue(credentials.getPassword());
					}
				}
				return true;
			}
		};
	}

	public static class CustomJschConfigSessionFactory extends JschConfigSessionFactory
	{
		private boolean isValid = true;
		private String pathToIdRsa;
		private String pathToKnownHosts;

		public CustomJschConfigSessionFactory(CredentialBean credentials)
		{
			String pathToRsa = credentials.getPathToRsa();
			String pathToHosts = credentials.getPathToHosts();
			this.isValid = !Str.IsNullOrEmpty(pathToHosts) && !Str.IsNullOrEmpty(pathToRsa);
			if (this.isValid)
			{
				this.pathToIdRsa = pathToRsa;
				this.pathToKnownHosts = pathToHosts;
			}
		}

		public boolean isValid()
		{
			return isValid;
		}

		@Override
		protected void configure(OpenSshConfig.Host host, Session session)
		{
			session.setConfig("StrictHostKeyChecking", "yes");
		}

		@Override
		protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException
		{
			JSch jsch = super.getJSch(hc, fs);
			jsch.removeAllIdentity();
			jsch.addIdentity(this.pathToIdRsa);
			jsch.setKnownHosts(this.pathToKnownHosts);
			return jsch;
		}
	}
}
