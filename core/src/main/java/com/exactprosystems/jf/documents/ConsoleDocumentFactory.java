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

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.wizard.EmptyWizardManager;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.VerboseLevel;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ConsoleErrorMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ConsoleMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.MatrixListener;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.Table;

import java.util.List;
import java.util.Map;

/**
 * A simple implementation of DocumentFactory.
 * This class used only on running in console mode
 */
public class ConsoleDocumentFactory extends DocumentFactory
{
	private VerboseLevel verboseLevel;

	public ConsoleDocumentFactory(VerboseLevel verboseLevel)
	{
		super();
		
		this.verboseLevel = verboseLevel;
	}

	@Override
	protected Context createContext(Configuration configuration, IMatrixListener matrixListener)
	{
		return new Context(this, matrixListener, System.out, rep -> {});
	}
	
	@Override
	protected Configuration createConfig(String fileName, Settings settings)
	{
		return new Configuration(fileName, this);
	}

	@Override
	protected Matrix createLibrary(String fileName, Configuration configuration, IMatrixListener matrixListener)
	{
		return new Matrix(fileName, this, matrixListener, true);
	}

	@Override
	protected Matrix createMatrix(String fileName, Configuration configuration, IMatrixListener matrixListener)
	{
		return new Matrix(fileName, this, matrixListener, false);
	}

	@Override
	protected MessageDictionary createClientDictionary(String fileName, Configuration configuration)
	{
		return new MessageDictionary(fileName, this);
	}

	@Override
	protected GuiDictionary createAppDictionary(String fileName, Configuration configuration)
	{
		return new GuiDictionary(fileName, this);
	}

	@Override
	protected Csv createCsv(String fileName, Configuration configuration)
	{
		return new Csv(fileName, this);
	}

	@Override
	protected PlainText createPlainText(String fileName, Configuration configuration)
	{
		return new PlainText(fileName, this);
	}

	@Override
	protected SystemVars createVars(String fileName, Configuration configuration)
	{
		return new SystemVars(fileName, this);
	}

	@Override
	protected IMatrixListener createMatrixListener()
	{
		IMatrixListener matrixListener = null;
		switch (this.verboseLevel)
		{
			case None:
				matrixListener = new MatrixListener();
				break;
			case Errors:
				matrixListener = new ConsoleErrorMatrixListener();
				break;
			case All:
				matrixListener = new ConsoleMatrixListener(true);
				break;
		}

		return matrixListener;
	}

	@Override
	public void error(Exception exception)
	{
		if (exception != null)
		{
			exception.printStackTrace();
		}
	}

	@Override
	public void popup(String message, Notifier notifier)
	{
		System.out.printf("[%s] %s %n", notifier, message);
	}

	@Override
	public void showWaits(long ms, Matrix matrix)
	{
		//nothing
	}

	@Override
    public boolean editTable(AbstractEvaluator evaluator, String title, Table table, Map<String, Boolean> columns)
    {
        return true;
    }

	@Override
	public Object input(AbstractEvaluator evaluator, String title, Object defaultValue, HelpKind helpKind, List<ReadableValue> dataSource, int timeout)
	{
		return defaultValue;
	}

	@Override
	public WizardManager getWizardManager()
	{
		return new EmptyWizardManager();
	}

	@Override
	public void showMatrixScheduler()
	{

	}
}
