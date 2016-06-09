package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.VerboseLevel;
import com.exactprosystems.jf.documents.config.Configuration;
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

public class ConsoleDocumentFactory extends DocumentFactory
{
	public ConsoleDocumentFactory()
	{
		super(System.err::println);
	}

	@Override
	protected Configuration createConfig(String fileName, Settings settings)
	{
		return new Configuration(fileName, settings);
	}

	@Override
	protected Matrix createMatrix(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		return new Matrix(fileName, configuration, matrixListener);
	}

	@Override
	protected MessageDictionary createClientDictionary(String fileName, Configuration configuration) throws Exception
	{
		return new MessageDictionary(fileName, configuration);
	}

	@Override
	protected GuiDictionary createAppDictionary(String fileName, Configuration configuration) throws Exception
	{
		return new GuiDictionary(fileName, configuration);
	}

	@Override
	protected Csv createCsv(String fileName, Configuration configuration) throws Exception
	{
		return new Csv(fileName, configuration);
	}

	@Override
	protected PlainText createPlainText(String fileName, Configuration configuration) throws Exception
	{
		return new PlainText(fileName, configuration);
	}

	@Override
	protected SystemVars createVars(String fileName, Configuration configuration) throws Exception
	{
		return new SystemVars(fileName, configuration);
	}

	@Override
	protected IMatrixListener createMatrixListener()
	{
		VerboseLevel verboseLevel = VerboseLevel.All;
		IMatrixListener matrixListener 	= null;
		switch (verboseLevel)
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
}
