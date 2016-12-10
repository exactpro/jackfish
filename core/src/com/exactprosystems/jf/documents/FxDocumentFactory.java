package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.csv.CsvFx;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.MatrixListenerFx;
import com.exactprosystems.jf.tool.matrix.schedule.RunnerScheduler;
import com.exactprosystems.jf.tool.msgdictionary.MessageDictionaryFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.systemvars.SystemVarsFx;
import com.exactprosystems.jf.tool.text.PlainTextFx;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;

public class FxDocumentFactory extends DocumentFactory
{
	public FxDocumentFactory(Main mainModel) throws Exception
	{
		super();

		this.mainModel = mainModel;
		this.runnerListener = new RunnerScheduler();
	}
	
	@Override
	protected Context createContext(Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		return new Context(this, matrixListener, System.out, name ->
	        Common.tryCatch(() -> this.mainModel.openReport(new File(name)), "Error on show report") );
	}

	@Override
	protected Configuration createConfig(String fileName, Settings settings) throws Exception
	{
		return new ConfigurationFx(this, fileName, this.runnerListener, this.mainModel);
	}

	@Override
	protected Matrix createLibrary(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		return new MatrixFx(fileName, this, matrixListener, true);
	}

	@Override
	protected Matrix createMatrix(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		return new MatrixFx(fileName, this, matrixListener, false);
	}

	@Override
	protected MessageDictionary createClientDictionary(String fileName, Configuration configuration) throws Exception
	{ 
		return new MessageDictionaryFx(fileName, this);  
	}

	@Override
	protected GuiDictionary createAppDictionary(String fileName, Configuration configuration) throws Exception
	{
		return new DictionaryFx(fileName, this);
	}

	@Override
	protected Csv createCsv(String fileName, Configuration configuration) throws Exception
	{
		return new CsvFx(fileName, this);
	}

	@Override
	protected PlainText createPlainText(String fileName, Configuration configuration) throws Exception
	{
		return new PlainTextFx(fileName, this);
	}

	@Override
	protected SystemVars createVars(String fileName, Configuration configuration) throws Exception
	{
		return new SystemVarsFx(fileName, this);
	}
	
	@Override
	protected IMatrixListener createMatrixListener()
	{
		return new MatrixListenerFx();
	}

	@Override
	public void 				error(String message, Exception exeption)
	{
		if (exeption != null)
		{
			logger.error(">> " + exeption.getMessage() + "\n" + message, exeption);
			DialogsHelper.showError(exeption.getMessage() + "\n" + message);
		}
		else
		{
			DialogsHelper.showError(message);
		}
	}
	
	@Override
	public void 				popup(String message, Notifier notifier)
	{
		DialogsHelper.showNotifier(message, notifier);
	}

	@Override
	public Object input(AbstractEvaluator evaluator, String title, Object defaultValue, Integer timeout, ActionItem.HelpKind helpKind, Collection<?> dataSource)
	{
		String result = DialogsHelper.showUserInput(evaluator, title, defaultValue, timeout, helpKind, dataSource);
		Object value;
		try
		{
			value = evaluator.evaluate(result);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			value = new MatrixError(e.getMessage(), ErrorKind.EXPRESSION_ERROR, null);
		}
		return value;
	}

	@Override
	public RunnerListener getRunnerListener()
	{
		return this.runnerListener;
	}

	private RunnerListener runnerListener;
	
	private Main mainModel;

	private static final Logger logger = Logger.getLogger(FxDocumentFactory.class);

}
