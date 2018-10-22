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
package com.exactprosystems.jf.tool.git.merge.editor;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.main.Main;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MergeEditor
{
	private static final Logger logger = Logger.getLogger(MergeEditor.class);

	private final String filePath;
	private final Main model;

	private List<String> yourLines;
	private List<String> theirLines;
	private List<Chunk> conflicts;

	private MergeEditorController controller;

	boolean isSuccessful;

	public MergeEditor(Main model, String filePath) throws Exception
	{
		logger.trace("Start merge editor");
		this.filePath = filePath;
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("MergeEditor.fxml"));
		this.controller.init(this, this.filePath);

		this.yourLines = GitUtil.getYours(this.model.getCredential(), filePath);
		logger.trace("Get your lines. List size : " + this.yourLines.size());
		logger.trace("Your lines : \n" + this.yourLines.stream().collect(Collectors.joining("\n")));

		this.theirLines = GitUtil.getTheirs(this.model.getCredential(), filePath);
		logger.trace("Get their lines. List size : " + this.theirLines.size());
		logger.trace("Their lines : \n" + this.theirLines.stream().collect(Collectors.joining("\n")));

		this.conflicts = GitUtil.getConflicts(this.model.getCredential(), this.filePath);
		if (this.conflicts != null)
		{
			logger.trace("Get conflicts. List size : " + this.conflicts.size());
			logger.trace("Conflicts : \n" + this.conflicts.stream().map(Chunk::toString).collect(Collectors.joining("\n")));
		}

		evaluate();
	}

	public void display()
	{
		this.controller.show();
	}

	public void close()
	{
		this.isSuccessful = false;
		this.controller.closeDialog();
	}

	public void saveResult(String result) throws Exception
	{
		Common.writeToFile(new File(GitUtil.checkFile(this.model.getCredential(), this.filePath)), Arrays.asList(result.split("\n")));
		GitUtil.addFileToIndex(this.model.getCredential(), this.filePath);
		this.isSuccessful = true;
		this.controller.closeDialog();
	}

	public boolean isSuccessful()
	{
		return isSuccessful;
	}

	private void evaluate() throws Exception
	{
		Iterator<Chunk> iterator = this.conflicts.iterator();
		int i = 0;
		int yourLastPos = 0;
		int theirLastPos = 0;
		while (iterator.hasNext())
		{
			if (yourLastPos > this.yourLines.size() || theirLastPos > this.theirLines.size())
			{
				break;
			}
			logger.trace("Your last pos : " + yourLastPos);
			logger.trace("Their last pos : " + theirLastPos);
			logger.trace("i : " + i);

			Chunk chunk = iterator.next();
			logger.trace("Next chunk : " + chunk);
			logger.trace("!chunk.isHasConflict() : " + !chunk.isHasConflict());
			if (!chunk.isHasConflict())
			{
				logger.trace("Chunk don't has conflict");
				int diff = chunk.getEnd() - chunk.getStart();
				logger.trace("int diff = chunk.getEnd() - chunk.getStart() : " + diff);

				int oldLastPos = yourLastPos;
				yourLastPos += diff;
				logger.trace("yourLastPos : " + yourLastPos);
				if (yourLastPos > this.yourLines.size())
				{
					logger.trace("Break from loop");
					break;
				}

				String yourText = listToStr(this.yourLines.subList(oldLastPos, yourLastPos));
				logger.trace("yourText : " + yourText);

				oldLastPos = theirLastPos;
				theirLastPos += diff;
				logger.trace("theirLastPos : " + theirLastPos);
				if (yourLastPos > this.theirLines.size())
				{
					logger.trace("Break from loop");
					break;
				}
				String theirText = listToStr(this.theirLines.subList(oldLastPos, theirLastPos));
				logger.trace("theirText : " + theirText);

				this.controller.addLines(yourText, theirText, yourText, false, i);
			}
			else
			{
				logger.trace("Chunk has conflict");
				Chunk theirChunk = iterator.next();
				logger.trace("Their chunk : " + theirChunk);

				int yourDiff = chunk.getEnd() - chunk.getStart();
				logger.trace("int yourDiff = chunk.getEnd() - chunk.getStart() : " + yourDiff);

				int oldLastPos = yourLastPos;
				yourLastPos += yourDiff;
				logger.trace("yourLastPos : " + yourLastPos);
				if (yourLastPos > this.yourLines.size())
				{
					logger.trace("Break from loop");
					break;
				}

				String yourText = listToStr(this.yourLines.subList(oldLastPos, yourLastPos));
				logger.trace("yourText : " + yourText);

				int theirDiff = theirChunk.getEnd() - theirChunk.getStart();
				logger.trace("int theirDiff = theirChunk.getEnd() - theirChunk.getStart() : " + theirDiff);

				oldLastPos = theirLastPos;
				theirLastPos += theirDiff;
				logger.trace("theirLastPos : " + theirLastPos);
				if (yourLastPos > this.theirLines.size())
				{
					logger.trace("Break from loop");
					break;
				}

				String theirText = listToStr(this.theirLines.subList(oldLastPos, theirLastPos));
				logger.trace("Their text : " + theirText);

				this.controller.addLines(yourText, theirText, "", true, i);
			}
			i++;
		}
	}

	private String listToStr(List<String> list)
	{
		return list.stream().collect(Collectors.joining("\n"));
	}
}
