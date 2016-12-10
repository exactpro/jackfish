package com.exactprosystems.jf.documents;

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
import com.exactprosystems.jf.documents.matrix.parser.listeners.DummyRunnerListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.MatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;

import java.util.Collection;

public class ConsoleDocumentFactory extends DocumentFactory
{
	public ConsoleDocumentFactory(VerboseLevel verboseLevel)
	{
		super();
		
		this.verboseLevel = verboseLevel;
	}

	@Override
	protected Context createContext(Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		return new Context(this, matrixListener, System.out, rep -> {});
	}
	
	@Override
	protected Configuration createConfig(String fileName, Settings settings)
	{
		return new Configuration(fileName, this);
	}

	@Override
	protected Matrix createLibrary(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		return new Matrix(fileName, this, matrixListener, true);
	}

	@Override
	protected Matrix createMatrix(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		return new Matrix(fileName, this, matrixListener, false);
	}

	@Override
	protected MessageDictionary createClientDictionary(String fileName, Configuration configuration) throws Exception
	{
		return new MessageDictionary(fileName, this);
	}

	@Override
	protected GuiDictionary createAppDictionary(String fileName, Configuration configuration) throws Exception
	{
		return new GuiDictionary(fileName, this);
	}

	@Override
	protected Csv createCsv(String fileName, Configuration configuration) throws Exception
	{
		return new Csv(fileName, this);
	}

	@Override
	protected PlainText createPlainText(String fileName, Configuration configuration) throws Exception
	{
		return new PlainText(fileName, this);
	}

	@Override
	protected SystemVars createVars(String fileName, Configuration configuration) throws Exception
	{
		return new SystemVars(fileName, this);
	}

	@Override
	protected IMatrixListener createMatrixListener()
	{
		IMatrixListener matrixListener 	= null;
		switch (this.verboseLevel)
		{
			case None:
				matrixListener 	= new MatrixListener();
				break;
			case Errors:
				matrixListener 	= new ConsoleErrorMatrixListener();
				break;
			case All:
				matrixListener 	= new ConsoleMatrixListener(true);
				break;
		}

		return matrixListener;
	}
	
	@Override
	public void 				error(String message, Exception exeption)
	{
		if (message != null)
		{
			System.err.println(message);
		}
		if (exeption != null)
		{
			exeption.printStackTrace(System.err);
		}
	}
	
	@Override
	public void 				popup(String message, Notifier notifier)
	{
		System.out.printf("[%s] %s %n", notifier, message);
	}

	@Override
	public Object input(AbstractEvaluator evaluator, String title, Object defaultValue, HelpKind helpKind, Collection<?> dataSource)
	{
		return defaultValue;
	}

	@Override
	public RunnerListener getRunnerListener()
	{
		return new DummyRunnerListener();
	}


	private VerboseLevel verboseLevel;
}
