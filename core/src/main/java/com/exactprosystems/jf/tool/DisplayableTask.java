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
package com.exactprosystems.jf.tool;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.Optional;
import java.util.function.Consumer;

public class DisplayableTask<T> extends Service<T>
{
	private final ITask<T> mainTask;
	private Consumer<T> onSuccess;
	private Consumer<Throwable> onFailed;

	public interface ITask<T>
	{
		T apply() throws Exception;
	}

	public DisplayableTask(ITask<T> task)
	{
		this.mainTask = task;
	}

	public DisplayableTask onSuccess(Consumer<T> onSucceed)
	{
		this.onSuccess = onSucceed;
		return this;
	}

	public DisplayableTask onFailded(Consumer<Throwable> onFailed)
	{
		this.onFailed = onFailed;
		return this;
	}

	public Optional<Consumer<T>> getOnSuccess()
	{
		return Optional.ofNullable(this.onSuccess);
	}

	public Optional<Consumer<Throwable>> getOnFail()
	{
		return Optional.ofNullable(this.onFailed);
	}

	@Override
	protected Task<T> createTask()
	{
		return new Task<T>()
		{
			@Override
			protected T call() throws Exception
			{
				return mainTask.apply();
			}
		};
	}
}
