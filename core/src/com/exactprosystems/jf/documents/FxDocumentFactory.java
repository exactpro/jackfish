package com.exactprosystems.jf.documents;

import javafx.scene.layout.BorderPane;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.tool.csv.CsvFx;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.systemvars.SystemVarsFx;
import com.exactprosystems.jf.tool.text.PlainTextFx;

public class FxDocumentFactory extends DocumentFactory
{
	public FxDocumentFactory()
	{
		super(DialogsHelper::showError);
	}
	

	@Override
	public ConfigurationFx createConfig(String fileName, Settings settings) throws Exception
	{
		return new ConfigurationFx(fileName, this.runnerListener, settings, this.mainModel, this.pane);
	}

	@Override
	public MatrixFx createMatrix(String fileName, Configuration configuration) throws Exception
	{
		return new MatrixFx(fileName, configuration, this.matrixListener);
	}

	@Override
	public MessageDictionary createClientDictionary(String fileName, Configuration configuration) throws Exception
	{ 
		return new MessageDictionary(fileName, configuration); // TODO implement MessageDictionaryFx 
	}

	@Override
	public DictionaryFx createAppDictionary(String fileName, Configuration configuration) throws Exception
	{
		return new DictionaryFx(fileName, configuration);
	}

	@Override
	public CsvFx createCsv(String fileName, Configuration configuration) throws Exception
	{
		return new CsvFx(fileName, super.settings, configuration);
	}

	@Override
	public PlainTextFx createPlainText(String fileName, Configuration configuration) throws Exception
	{
		return new PlainTextFx(fileName, super.settings, configuration);
	}

	@Override
	public SystemVarsFx createVars(String fileName, Configuration configuration) throws Exception
	{
		return new SystemVarsFx(fileName, configuration);
	}
	
	private IMatrixListener matrixListener;
	
	private RunnerListener runnerListener;
	
	private Main mainModel;
	
	private BorderPane pane;
}
