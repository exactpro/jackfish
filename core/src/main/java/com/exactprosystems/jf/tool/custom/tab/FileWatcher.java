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

package com.exactprosystems.jf.tool.custom.tab;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public abstract class FileWatcher implements AutoCloseable
{
	private File file;
	private Timer timer;
	private long timeStamp;
	private boolean changed = false;
	
	public FileWatcher()
	{
		this.timeStamp = 0;
	}

	@Override
	public final void close()
	{
		if (this.timer != null)
		{
			this.timer.cancel();
			this.timer = null;
		}
	}
	
	public void saved(String fileName)
	{
		this.changed = false;
		if (this.timer != null)
		{
			this.timer.cancel();
		}
		if (fileName != null)
		{
			this.file = new File(fileName);
		}
		if (this.file != null)
		{
			this.timeStamp = this.file.lastModified();
			createTimerTask();
		}
	}
	
	public boolean isChanged()
	{
		return this.changed;
	}

	public abstract void onChanged();
	
	private void createTimerTask()
	{
		this.timer = new Timer();
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (file == null)
				{
					return;
				}
				
				long lastModified = file.lastModified();
				if (timeStamp != lastModified)
				{
					changed = true;
					onChanged();
				}
			}
		};
		this.timer.schedule(timerTask, new Date(), 1000);
	}

}
