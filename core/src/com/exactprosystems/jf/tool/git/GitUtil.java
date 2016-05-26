////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git;

import com.exactprosystems.jf.api.common.Str;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.AddCommand;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GitUtil
{
	private GitUtil()
	{

	}

	public static void revertFiles(CredentialBean bean, List<File> files) throws Exception
	{
		System.out.println("not implemented now");
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
		try (Git git = Git.cloneRepository().setURI(remotePath).setDirectory(projectFolder).setCredentialsProvider(credentialsProvider).setProgressMonitor(monitor).call()
		)
		{
			;
		}
	}

	public static void gitCommit(CredentialBean bean, List<File> files, String msg) throws Exception
	{
		try (Git git = git(bean))
		{
			AddCommand add = git.add();
			files.stream().map(File::getPath).forEach(add::addFilepattern);
			add.call();
			git.commit().setMessage(msg).call();
		}
	}

	public static void gitPush(CredentialBean bean, List<File> files, String msg) throws Exception
	{
		gitCommit(bean, files, msg);
		try (Git git = git(bean))
		{
			//TODO add pushing command
//			git.commit().setMessage(msg).call();
		}
	}

	public static void gitDummy(Object... objects) throws Exception
	{
		for (int i = 0; i < 5; i++)
		{
			System.out.println("tick " + i);
			Thread.sleep(3000);
		}
	}

	public static List<GitBean> gitStatus(CredentialBean credential) throws Exception
	{
		try (Git git = git(credential))
		{
			Status status = git.status().call();
			ArrayList<GitBean> list = new ArrayList<>();
			//get all files. uncommitted contains all files without untracked
			status.getUncommittedChanges().stream().map(st -> new GitBean(GitBean.Status.UNSTAGED, new File(st))).forEach(list::add);
			status.getUntracked().stream().map(st -> new GitBean(GitBean.Status.UNTRACKED, new File(st))).forEach(list::add);

			replaceFiles(list, status.getAdded(), GitBean.Status.ADDED);
			replaceFiles(list, status.getChanged(), GitBean.Status.CHANGED);
			replaceFiles(list, status.getRemoved(), GitBean.Status.REMOVED);
			Collections.sort(list, (b1,b2) -> b1.getStatus().compareTo(b2.getStatus()));
			return list;
		}
	}

	private static void replaceFiles(List<GitBean> mainList, Set<String> newList, GitBean.Status status)
	{
		List<GitBean> collect = newList.stream().map(st -> new GitBean(status, new File(st))).collect(Collectors.toList());
		mainList.stream().filter(collect::contains).forEach(bean -> bean.updateStatus(status));
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
