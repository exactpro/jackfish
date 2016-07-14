////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.pull.GitPullBean;
import com.exactprosystems.jf.tool.git.reset.FileWithStatusBean;
import com.exactprosystems.jf.tool.git.reset.GitResetBean;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.jgit.lib.Constants.*;

public class GitUtil
{
	private GitUtil()
	{

	}

	//region Clone
	public static void gitClone(String remotePath, File projectFolder, CredentialBean credentials, ProgressMonitor monitor) throws Exception
	{
		CredentialsProvider credentialsProvider = getCredentialsProvider(credentials);
		try (Git git = Git.cloneRepository().setURI(remotePath).setDirectory(projectFolder).setCredentialsProvider(credentialsProvider).setProgressMonitor(monitor).call()
		)
		{
			;
		}
	}
	//endregion

	//region Commit
	public static void gitCommit(CredentialBean bean, List<File> files, String msg, boolean isAmend) throws Exception
	{
		try (Git git = git(bean))
		{
			AddCommand add = git.add();
			files.stream().map(File::getPath).map(Common::getRelativePath).forEach(add::addFilepattern);
			add.call();
			git.commit().setAmend(isAmend).setAll(true).setMessage(msg).call();
		}
	}

	public static void gitPush(CredentialBean bean, List<File> files, String msg, boolean isAmend) throws Exception
	{
		if (!files.isEmpty())
		{
			gitCommit(bean, files, msg, isAmend);
		}
		try (Git git = git(bean))
		{
			git.push().setPushAll().setCredentialsProvider(getCredentialsProvider(bean)).setAtomic(true).call();
		}
	}

	public static List<String> gitUnpushingCommits(CredentialBean credentialBean) throws Exception
	{
		try (Git git = git(credentialBean))
		{
			List<String> list = new ArrayList<>();

			LogCommand log = git.log();
			Repository repo = git.getRepository();
			log.addRange(repo.exactRef(R_REMOTES + DEFAULT_REMOTE_NAME + "/" + MASTER).getObjectId(), repo.exactRef(Constants.HEAD).getObjectId());
			Iterable<RevCommit> call = log.call();
			for (RevCommit aCall : call)
			{
				list.add(aCall.getName());
			}
			return list;
		}
	}
	//endregion

	//region Pull
	public static List<GitPullBean> gitPull(CredentialBean bean, ProgressMonitor monitor) throws Exception
	{
		try (Git git = git(bean))
		{
			//from http://stackoverflow.com/a/26170467/3452146
			ObjectId oldHead = git.getRepository().resolve("HEAD^{tree}");

			List<GitPullBean> list = new ArrayList<>();
			try
			{
				PullResult pullResult = git.pull().setCredentialsProvider(getCredentialsProvider(bean)).setProgressMonitor(monitor).call();
				MergeResult m = pullResult.getMergeResult();
				if (m != null)
				{
					Map<String, int[][]> allConflicts = m.getConflicts();
					if (allConflicts != null)
					{
						for (String path : allConflicts.keySet()) {
							int[][] c = allConflicts.get(path);
							System.out.println("Conflicts in file " + path);
							list.add(new GitPullBean(path, true));
							for (int i = 0; i < c.length; ++i) {
								System.out.println("  Conflict #" + i);
								for (int j = 0; j < (c[i].length) - 1; ++j) {
									if (c[i][j] >= 0)
										System.out.println("    Chunk for " + m.getMergedCommits()[j] + " starts on line #" + c[i][j]);
								}
							}
						}
					}
				}
			}
			catch (CheckoutConflictException cce)
			{
				throw new Exception("\nNeed to commit the files before pulling : " + cce.getConflictingPaths().toString());
			}

			ObjectId head = git.getRepository().resolve("HEAD^{tree}");
			ObjectReader reader = git.getRepository().newObjectReader();
			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldHead);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, head);
			List<DiffEntry> diffs = git.diff()
					.setNewTree(newTreeIter)
					.setOldTree(oldTreeIter)
					.call();



			for (DiffEntry diff : diffs)
			{
				String fileName = null;
				DiffEntry.ChangeType changeType = diff.getChangeType();
				switch (changeType)
				{
					case ADD:
						fileName = diff.getNewPath();
						break;
					case MODIFY:
						fileName = diff.getNewPath();
						break;
					case DELETE:
						fileName = diff.getOldPath();
						break;
					case RENAME:
						break;
					case COPY:
						break;
				}
				GitPullBean pullBean = new GitPullBean(fileName, false);
				list.add(pullBean);
			}
			return list;
		}
	}
	//endregion

	//region Reset
	public static void gitReset(CredentialBean bean, String ref) throws Exception
	{
		try (Git git = git(bean))
		{
			git.reset().setRef(ref).setMode(ResetCommand.ResetType.HARD).call();
		}
	}

	public static List<FileWithStatusBean> getCommitFiles(CredentialBean bean, RevCommit commit) throws Exception
	{
		try (Git git = git(bean))
		{
			List<FileWithStatusBean> list = new ArrayList<>();

			ObjectId currentId = commit.getTree().getId();
			ObjectId parentId = commit.getTree().getId();
			if (commit.getParentCount() != 0)
			{
				parentId = commit.getParent(0).getTree().getId();
			}

			ObjectReader reader = git.getRepository().newObjectReader();

			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, parentId);

			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, currentId);

			List<DiffEntry> diffs = git.diff()
					.setNewTree(newTreeIter)
					.setOldTree(oldTreeIter)
					.call();

			list.addAll(diffs.stream().map(FileWithStatusBean::new).collect(Collectors.toList()));

			return list;
		}
	}

	public static List<GitResetBean> gitGetCommits(CredentialBean bean) throws Exception
	{
		try (Git git = git(bean))
		{
			List<GitResetBean> list = new ArrayList<>();
			Iterable<RevCommit> commits = git.log().all().call();
			for (RevCommit commit : commits)
			{
				list.add(new GitResetBean(commit, bean));
			}
			return list;
		}
	}
	//endregion

	//region Status
	public static void revertFiles(CredentialBean bean, List<File> files) throws Exception
	{
		try (Git git = git(bean))
		{
			CheckoutCommand checkout = git.checkout();
			files.stream().map(File::getPath).map(Common::getRelativePath).forEach(checkout::addPath);
			checkout.call();
		}
	}

	public static void ignoreFiles(List<File> files) throws Exception
	{
		File gitIgnore = checkGitIgnoreFile();
		try (FileWriter writer = new FileWriter(gitIgnore))
		{
			for (File file : files)
			{
				writer.write(Common.getRelativePath(file.getAbsolutePath())+"\n");
			}
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
	//endregion

	public static void gitDummy(Object... objects) throws Exception
	{
		for (int i = 0; i < 5; i++)
		{
			System.out.println("tick " + i);
			Thread.sleep(3000);
		}
	}

	//region private methods
	private static File checkGitIgnoreFile() throws Exception
	{
		File file = new File(Constants.GITIGNORE_FILENAME);
		if (!file.exists())
		{
			file.createNewFile();
		}
		return file;
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
					//for github
					if (item instanceof CredentialItem.Username)
					{
						((CredentialItem.StringType) item).setValue(credentials.getUsername());
					}
					else if (item instanceof CredentialItem.Password)
					{
						((CredentialItem.Password) item).setValue(credentials.getPassword().toCharArray());
					}
					//for ssh
					else if (item instanceof CredentialItem.StringType)
					{
						((CredentialItem.StringType) item).setValue(credentials.getPassword());
					}
				}
				return true;
			}
		};
	}

	private static class CustomJschConfigSessionFactory extends JschConfigSessionFactory
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
	//endregion
}
