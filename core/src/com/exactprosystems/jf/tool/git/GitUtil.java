////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;

public class GitUtil
{
	private static Settings settings;

	private String username;
	private String password;

	public static GitUtil getInstance()
	{
		return INSTANCE;
	}

	private static GitUtil INSTANCE = new GitUtil();

	public static void initSettings(Settings settings)
	{
		GitUtil.settings = settings;
	}

	private GitUtil()
	{

	}

	public void gitClone(String remotePath, File projectFolder, String username, String password, ProgressMonitor monitor) throws Exception
	{

		storeCredential(username, password);
		CredentialsProvider credentialsProvider = getCredentialsProvider();
		try(Git git = Git.cloneRepository()
				.setURI(remotePath)
				.setDirectory(projectFolder)
				.setCredentialsProvider(credentialsProvider)
				.setProgressMonitor(monitor)
				.call()
		)
		{

		}
	}

	private void storeCredential(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	private void checkCredential()
	{
		if (this.username == null || this.password == null)
		{
			new CredentialDialog(this::storeCredential).display(this.username, this.password);
		}
	}

	private Git git() throws Exception
	{
		setSSH();
		Repository localRepo = new FileRepositoryBuilder().findGitDir().build();
		return new Git(localRepo);
	}

	private void setSSH()
	{
		CustomJschConfigSessionFactory jschConfigSessionFactory = new CustomJschConfigSessionFactory(settings);
		if (jschConfigSessionFactory.isValid())
		{
			SshSessionFactory.setInstance(jschConfigSessionFactory);
		}
	}

	private CredentialsProvider getCredentialsProvider()
	{
		checkCredential();
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
						((CredentialItem.StringType) item).setValue(password);
						continue;
					}
				}
				return true;
			}
		};
	}

	public static class CustomJschConfigSessionFactory extends JschConfigSessionFactory
	{
		private final Settings settings;
		private boolean isValid = true;
		private String pathToIdRsa;
		private String pathToKnownHosts;

		public CustomJschConfigSessionFactory(Settings settings)
		{
			this.settings = settings;
			Settings.SettingsValue idRsa = this.settings.getValue("GLOBAL", SettingsPanel.GIT, SettingsPanel.GIT_SSH_IDENTITY);
			Settings.SettingsValue knownHosts = this.settings.getValue("GLOBAL", SettingsPanel.GIT, SettingsPanel.GIT_KNOWN_HOST);
			this.isValid = idRsa != null && knownHosts != null;
			if (this.isValid)
			{
				this.pathToIdRsa = idRsa.getValue();
				this.pathToKnownHosts = knownHosts.getValue();
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
