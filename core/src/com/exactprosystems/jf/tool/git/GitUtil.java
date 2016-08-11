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
import com.exactprosystems.jf.tool.git.merge.editor.Chunk;
import com.exactprosystems.jf.tool.git.pull.GitPullBean;
import com.exactprosystems.jf.tool.git.reset.FileWithStatusBean;
import com.exactprosystems.jf.tool.git.reset.GitResetBean;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeChunk;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.merge.StrategyResolve;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.FileOutputStream;
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
		try (Git git = Git.cloneRepository().setURI(remotePath).setDirectory(projectFolder).setCredentialsProvider(credentialsProvider).setProgressMonitor(monitor).call())
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
			Iterable<PushResult> call = git.push().setPushAll().setCredentialsProvider(getCredentialsProvider(bean)).call();
			for (PushResult pushResult : call)
			{
				for (RemoteRefUpdate update : pushResult.getRemoteUpdates())
				{
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=478199#c2
					switch (update.getStatus())
					{
						case NOT_ATTEMPTED:
							break;
						case UP_TO_DATE:
							break;
						case REJECTED_NONFASTFORWARD:
							throw new Exception("You need to pull your local copy, merge and after that push");
						case REJECTED_NODELETE:
							break;
						case REJECTED_REMOTE_CHANGED:
							break;
						case REJECTED_OTHER_REASON:
							break;
						case NON_EXISTING:
							break;
						case AWAITING_REPORT:
							break;
						case OK:
							break;
					}
				}
			}
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
			ObjectId oldHead = git.getRepository().resolve("HEAD^{tree}");

			List<GitPullBean> list = new ArrayList<>();
			try
			{
				PullResult pullResult = git.pull().setCredentialsProvider(getCredentialsProvider(bean)).setProgressMonitor(monitor).call();

//				MergeResult m = pullResult.getMergeResult();
//				if (m != null)
//				{
//					Map<String, int[][]> allConflicts = m.getConflicts();
//					if (allConflicts != null)
//					{
//						for (String path : allConflicts.keySet())
//						{
//							int[][] c = allConflicts.get(path);
//							System.out.println("Conflicts in file " + path);
//							list.add(new GitPullBean(path, true));
//							for (int i = 0; i < c.length; i++)
//							{
//								System.out.println("  Conflict #" + i);
//								for (int j = 0; j < c[i].length - 1; j++)
//								{
//									if (c[i][j] >= 0)
//										System.out.println("    Chunk for " + m.getMergedCommits()[j] + " starts on line #" + c[i][j]);
//								}
//							}
//						}
//					}
//				}
			}
			catch (CheckoutConflictException cce)
			{
				throw new Exception("\nNeed to commit the files before pulling : " + cce.getConflictingPaths().toString());
			}

			ObjectId head = git.getRepository().resolve("refs/remotes/origin/HEAD^{tree}");
			if (head == null)
			{
				head = git.getRepository().resolve("HEAD^{tree}");
				DialogsHelper.showInfo("Something wrong. Pulled files can't showing correctly. Try to push all your files and reclone project");
			}
			ObjectReader reader = git.getRepository().newObjectReader();
			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldHead);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, head);
			List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();

			for (DiffEntry diff : diffs)
			{
				DiffEntry.ChangeType changeType = diff.getChangeType();
				String fileName = changeType == DiffEntry.ChangeType.DELETE ? diff.getOldPath() : diff.getNewPath();
				GitPullBean pullBean = new GitPullBean(fileName, false);
				if (!list.contains(pullBean))
				{
					list.add(pullBean);
				}
			}
			return list;
		}
	}

	public static void main(String[] args) throws Exception
	{
		CredentialBean bean = new CredentialBean("AndrewBystrov", "Andrew17051993", "", "");
//		gitPull(bean, new TextProgressMonitor());
//		getConflicts(bean, "qq");
//		qq.forEach(System.out::println);

		String asd = "asd";
	}

	public static List<Chunk> getConflictsNew(CredentialBean bean, String fileName) throws Exception
	{
		try (Git git = git(bean))
		{
			Repository repo = git.getRepository();
			ThreeWayMerger merger = new StrategyResolve().newMerger(repo, true);
			merger.merge(repo.resolve(Constants.HEAD), repo.resolve(Constants.FETCH_HEAD));
			ResolveMerger resolveMerger = (ResolveMerger) merger;

			Map<String, org.eclipse.jgit.merge.MergeResult<?>> mergeResults = resolveMerger.getMergeResults();

			org.eclipse.jgit.merge.MergeResult<?> mergeChunks = mergeResults.get(fileName);
			if (mergeChunks == null)
			{
				return null;
			}
			if (!mergeChunks.containsConflicts())
			{
				return null;
			}
			List<Chunk> lines = new ArrayList<>();
			Chunk curCh = null;

			int nrOfConflicts = 0;
			// just counting
			for (MergeChunk mergeChunk : mergeChunks) {
				if (mergeChunk.getConflictState().equals(MergeChunk.ConflictState.FIRST_CONFLICTING_RANGE)) {
					nrOfConflicts++;
				}
			}

			String asd = "aaaaa";

			for (MergeChunk mergeChunk : mergeChunks)
			{
				MergeChunk.ConflictState conflictState = mergeChunk.getConflictState();
				switch (conflictState)
				{
					case NO_CONFLICT:
						lines.add(new Chunk(false, mergeChunk.getBegin(), mergeChunk.getEnd(), null));
						break;
					case FIRST_CONFLICTING_RANGE:
						lines.add(new Chunk(true, mergeChunk.getBegin(), mergeChunk.getEnd(), Chunk.ChunkState.Your));
						break;
					case NEXT_CONFLICTING_RANGE:
						lines.add(new Chunk(true, mergeChunk.getBegin(), mergeChunk.getEnd(), Chunk.ChunkState.Their));
						break;
				}
			}

			return lines;
		}
	}

	/*@Deprecated
	public static List<Chunk> getConflicts(CredentialBean bean, String fileName) throws Exception
	{
		try (Git git = git(bean))
		{
			Repository repo = git.getRepository();
			ThreeWayMerger merger = new StrategyResolve().newMerger(repo, true);
			merger.merge(repo.resolve(Constants.HEAD), repo.resolve(Constants.FETCH_HEAD));
			ResolveMerger resolveMerger = (ResolveMerger) merger;

			Map<String, org.eclipse.jgit.merge.MergeResult<?>> mergeResults = resolveMerger.getMergeResults();

			org.eclipse.jgit.merge.MergeResult<?> mergeChunks = mergeResults.get(fileName);
			if (mergeChunks == null)
			{
				return null;
			}
			if (!mergeChunks.containsConflicts())
			{
				return null;
			}
			List<Chunk> lines = new ArrayList<>();
			Chunk curCh = null;

			for (MergeChunk mergeChunk : mergeChunks)
			{
				MergeChunk.ConflictState conflictState = mergeChunk.getConflictState();
				switch (conflictState)
				{
					case NO_CONFLICT: break;
					case FIRST_CONFLICTING_RANGE:
						curCh = new Chunk(mergeChunk.getBegin(), mergeChunk.getEnd());
						lines.add(curCh);
						break;
					case NEXT_CONFLICTING_RANGE:
						curCh.setSecondStart(mergeChunk.getBegin());
						curCh.setSecondEnd(mergeChunk.getEnd());
						break;
				}
			}

//			int currentConflict = -1;
//
//
//			int[][] ret = new int[nrOfConflicts][3];
//			for (MergeChunk mergeChunk : mergeChunks)
//			{
//				// to store the end of this chunk (end of the last conflicting range)
//				int endOfChunk = 0;
//				if (mergeChunk.getConflictState().equals(MergeChunk.ConflictState.FIRST_CONFLICTING_RANGE))
//				{
//					if (currentConflict > -1)
//					{
//						// there was a previous conflicting range for which the end
//						// is not set yet - set it!
//						ret[currentConflict][2] = endOfChunk;
//					}
//					currentConflict++;
//					endOfChunk = mergeChunk.getEnd();
//					ret[currentConflict][mergeChunk.getSequenceIndex()] = mergeChunk.getBegin();
//				}
//				if (mergeChunk.getConflictState().equals(MergeChunk.ConflictState.NEXT_CONFLICTING_RANGE))
//				{
//					if (mergeChunk.getEnd() > endOfChunk)
//					{
//						endOfChunk = mergeChunk.getEnd();
//					}
//					ret[currentConflict][mergeChunk.getSequenceIndex()] = mergeChunk.getBegin();
//				}
//			}
//
//			String asd = "asd";
//			return null;
			return lines;
		}
	}*/
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

			List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();

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
				writer.write(Common.getRelativePath(file.getAbsolutePath()) + "\n");
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
			replaceFiles(list, status.getConflicting(), GitBean.Status.CONFLICTING);
			Collections.sort(list, (b1, b2) -> b1.getStatus().compareTo(b2.getStatus()));
			return list;
		}
	}

	public static String gitState(CredentialBean bean) throws Exception
	{
		try (Git git = git(bean))
		{
			return git.getRepository().getRepositoryState().getDescription();
		}
	}
	//endregion

	//region Merge
	public static List<String> getTheirs(CredentialBean bean, String filePath) throws Exception
	{
		try(Git git = git(bean))
		{
			Repository repository = git.getRepository();
			ObjectId lastCommitId = repository.resolve(Constants.MERGE_HEAD);
			try (RevWalk revWalk = new RevWalk(repository))
			{
				RevCommit commit = revWalk.parseCommit(lastCommitId);
				RevTree tree = commit.getTree();
				try (TreeWalk treeWalk = new TreeWalk(repository))
				{
					treeWalk.addTree(tree);
					treeWalk.setRecursive(true);
					treeWalk.setFilter(PathFilter.create(filePath));
					if (!treeWalk.next())
					{
						return Collections.emptyList();
					}
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					File tmpFile = File.createTempFile("temp", null);
					loader.copyTo(new FileOutputStream(tmpFile));
					return Common.readFile(tmpFile, true);
				}
			}
		}
	}

	public static void mergeTheirs(CredentialBean credentialBean, String filePath) throws Exception
	{
		List<String> theirs = getTheirs(credentialBean, filePath);
		Common.writeToFile(new File(filePath), theirs);
		try (Git git = git(credentialBean))
		{
			git.add().addFilepattern(filePath).call();
		}

	}

	public static List<String> getYours(CredentialBean bean, String filePath) throws Exception
	{
		try (Git git = git(bean))
		{
			Repository repository = git.getRepository();
			ObjectId lastCommitId = repository.resolve(Constants.HEAD);
			try (RevWalk revWalk = new RevWalk(repository))
			{
				RevCommit commit = revWalk.parseCommit(lastCommitId);
				RevTree tree = commit.getTree();
				try (TreeWalk treeWalk = new TreeWalk(repository))
				{
					treeWalk.addTree(tree);
					treeWalk.setRecursive(true);
					treeWalk.setFilter(PathFilter.create(filePath));
					if (!treeWalk.next())
					{
						return Collections.emptyList();
					}
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					File tmpFile = File.createTempFile("temp", null);
					loader.copyTo(new FileOutputStream(tmpFile));
					return Common.readFile(tmpFile, true);
				}
			}
		}
	}

	public static void mergeYours(CredentialBean credentialBean, String filePath) throws Exception
	{
		List<String> yours = getYours(credentialBean, filePath);
		Common.writeToFile(new File(filePath), yours);
		try (Git git = git(credentialBean))
		{
			git.add().addFilepattern(filePath).call();
		}
	}

	public static void addFileToIndex(CredentialBean bean, String filePath) throws Exception
	{
		try (Git git = git(bean))
		{
			git.add().addFilepattern(filePath).call();
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
