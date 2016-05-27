package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.documents.vars.SystemVars;

public class ConsoleDocumentFactory extends DocumentFactory
{
	public ConsoleDocumentFactory(IMatrixListener matrixListener)
	{
		super();
		
		this.matrixListener = matrixListener;
	}

	@Override
	protected Configuration createConfig(String fileName, Settings settings)
	{
		return new Configuration(fileName, settings);
	}

	@Override
	protected Matrix createMatrix(String fileName, Configuration configuration) throws Exception
	{
		return new Matrix(fileName, configuration, this.matrixListener);
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

	private IMatrixListener matrixListener;
}
