package com.exactprosystems.jf.documents;

import javafx.scene.layout.BorderPane;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.tool.csv.CsvFx;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.MatrixListenerFx;
import com.exactprosystems.jf.tool.msgdictionary.MessageDictionaryFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.systemvars.SystemVarsFx;
import com.exactprosystems.jf.tool.text.PlainTextFx;

public class FxDocumentFactory extends DocumentFactory
{
	public FxDocumentFactory()
	{
		super();
	}
	

	@Override
	public ConfigurationFx createConfig(String fileName, Settings settings) throws Exception
	{
		return new ConfigurationFx(fileName, this.runnerListener, settings, this.mainModel, this.pane);
	}

	@Override
	public MatrixFx createMatrix(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		return new MatrixFx(fileName, configuration, matrixListener);
	}

	@Override
	public MessageDictionaryFx createClientDictionary(String fileName, Configuration configuration) throws Exception
	{ 
		return new MessageDictionaryFx(fileName, configuration);  
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
	
	@Override
	protected IMatrixListener createMatrixListener()
	{
		return new MatrixListenerFx();
	}

	@Override
	protected void 				print(String message)
	{
		System.out.println(message); // TODO
	}
	
	@Override
	protected void 				error(String message, Exception exeption)
	{
		if (exeption != null)
		{
			DialogsHelper.showError(exeption.getMessage() + "\n" + message);
		}
		else
		{
			DialogsHelper.showError(message);
		}
	}
	
	@Override
	protected void 				popup(Notifier notifier, String message)
	{
		DialogsHelper.showNotifier(message, notifier);
	}

	private RunnerListener runnerListener;
	
	private Main mainModel;
	
	private BorderPane pane;

}
