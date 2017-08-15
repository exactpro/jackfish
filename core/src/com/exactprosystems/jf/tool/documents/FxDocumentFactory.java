////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.RunnerListener;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.documents.csv.CsvFx;
import com.exactprosystems.jf.tool.documents.csv.CsvFxController;
import com.exactprosystems.jf.tool.documents.text.PlainTextFx;
import com.exactprosystems.jf.tool.documents.text.PlainTextFxController;
import com.exactprosystems.jf.tool.documents.vars.SystemVarsFx;
import com.exactprosystems.jf.tool.documents.vars.SystemVarsFxController;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.MatrixListenerFx;
import com.exactprosystems.jf.tool.matrix.schedule.RunnerScheduler;
import com.exactprosystems.jf.tool.msgdictionary.MessageDictionaryFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class FxDocumentFactory extends DocumentFactory
{
	public FxDocumentFactory(Main mainModel, WizardManager wizardManager) throws Exception
	{
		super();

		this.mainModel = mainModel;
		this.wizardManager = wizardManager;
		this.runnerListener = new RunnerScheduler(this);
	}
	
	@Override
	public void showDocument(Document doc) throws Exception
	{
	    AbstractDocumentController<? extends Document> controller = null;
	    
	    if (doc instanceof SystemVarsFx)
	    {
	        controller = loadController(SystemVarsFx.class, SystemVarsFxController.class);
	    }
		if (doc instanceof PlainTextFx)
		{
			controller = loadController(PlainTextFx.class, PlainTextFxController.class);
		}
		if (doc instanceof CsvFx)
		{
			controller = loadController(CsvFx.class, CsvFxController.class);
		}
		controller.init(doc, this.mainModel.createCustomTab(doc));
		getConfiguration().register(doc);
		AbstractDocumentController<? extends Document> finalController = controller;
		doc.onClose(d -> finalController.close());
	    super.showDocument(doc);
	}
	
   private static <V extends Document, T extends AbstractDocumentController<V>> T loadController(Class<V> docClass, Class<T> controlllerClass) throws Exception
    {
        ControllerInfo info = controlllerClass.getAnnotation(ControllerInfo.class);
        if (info == null)
        {
            throw new Exception("ControllerInfo attribute is not found for " + controlllerClass);
        }

        URL resource = FxDocumentFactory.class.getResource(info.resourceName());
        FXMLLoader loader = new FXMLLoader(resource);
        Parent parent = loader.load();
        T controller = loader.getController();
        controller.setParent(parent);
        return controller;
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
		return new PlainTextFx(fileName, this, fileName.endsWith(Configuration.matrixExt) ? Highlighter.Matrix : Highlighter.None);
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
	public void 				error(Exception exception)
	{
		if (exception != null)
		{
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError(exception.getMessage());
		}
	}
	
	@Override
	public void 				popup(String message, Notifier notifier)
	{
		DialogsHelper.showNotifier(message, notifier);
	}

	@Override
	public void showWaits(long ms, Matrix matrix)
	{
		if (matrix instanceof MatrixFx)
		{
			((MatrixFx) matrix).displayTimer(ms, this.mainModel.isShowWaits());
		}
	}

	@Override
    public boolean editTable(AbstractEvaluator evaluator, String title, Table table, Map<String, Boolean> columns)
    {
        Boolean result = DialogsHelper.showUserTable(evaluator, title, table, columns);
        return result;
    }


	@Override
	public Object input(AbstractEvaluator evaluator, String title, Object defaultValue, HelpKind helpKind, List<ReadableValue> dataSource, int timeout)
	{
	    Object value = DialogsHelper.showUserInput(evaluator, title, defaultValue, helpKind, dataSource, timeout);
		return value;
	}

	@Override
	public RunnerListener getRunnerListener()
	{
		return this.runnerListener;
	}

	@Override
	public WizardManager getWizardManager()
	{
		return this.wizardManager;
	}

	private RunnerListener runnerListener;
	
	private Main mainModel;
	private WizardManager wizardManager;

	private static final Logger logger = Logger.getLogger(FxDocumentFactory.class);

}