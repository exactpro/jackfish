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

package com.exactprosystems.jf.tool.documents;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentKind;
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
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.documents.guidic.DictionaryFx;
import com.exactprosystems.jf.tool.documents.csv.CsvFx;
import com.exactprosystems.jf.tool.documents.text.PlainTextFx;
import com.exactprosystems.jf.tool.documents.vars.SystemVarsFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.MatrixListenerFx;
import com.exactprosystems.jf.tool.matrix.schedule.MatrixScheduler;
import com.exactprosystems.jf.tool.msgdictionary.MessageDictionaryFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class FxDocumentFactory extends DocumentFactory
{
	private MatrixScheduler runnerListener;

	private Main          mainModel;
	private WizardManager wizardManager;

	private static final Logger logger = Logger.getLogger(FxDocumentFactory.class);

	public FxDocumentFactory(Main mainModel, WizardManager wizardManager) throws Exception
	{
		super();

		this.mainModel = mainModel;
		this.wizardManager = wizardManager;
		this.runnerListener = new MatrixScheduler(this);
	}

	@Override
	public void showDocument(Document doc) throws Exception
	{
		CustomTab docTab = Common.checkDocument(doc);
		if (docTab != null)
		{
			//this need for reload document.
			docTab.getController().init(doc, docTab);
			super.showDocument(doc);
			return;
		}

		DocumentKind documentKind = DocumentKind.byDocument(doc);
		//it's always true on this step
		if (documentKind.isUseNewMVP())
		{
			AbstractDocumentController<? extends Document> controller = loadController(documentKind.getClazz());
			CustomTab customTab = this.mainModel.createCustomTab(doc);
			controller.init(doc, customTab);
			controller.restoreSettings(this.settings);
			this.mainModel.selectTab(customTab);
			getConfiguration().register(doc);
			doc.onClose(d -> controller.close());
			super.showDocument(doc);
		}
	}

	@Override
	protected Context createContext(Configuration configuration, IMatrixListener matrixListener)
	{
		return new Context(this, matrixListener, System.out, name -> this.mainModel.openReport(new File(name)));
	}

	@Override
	protected Configuration createConfig(String fileName, Settings settings)
	{
		return new ConfigurationFx(this, fileName, this.mainModel);
	}

	@Override
	protected Matrix createLibrary(String fileName, Configuration configuration, IMatrixListener matrixListener)
	{
		return new MatrixFx(fileName, this, matrixListener, true);
	}

	@Override
	protected Matrix createMatrix(String fileName, Configuration configuration, IMatrixListener matrixListener)
	{
		return new MatrixFx(fileName, this, matrixListener, false);
	}

	@Override
	protected MessageDictionary createClientDictionary(String fileName, Configuration configuration)
	{
		return new MessageDictionaryFx(fileName, this);
	}

	@Override
	protected GuiDictionary createAppDictionary(String fileName, Configuration configuration)
	{
		return new DictionaryFx(fileName, this);
	}

	@Override
	protected Csv createCsv(String fileName, Configuration configuration)
	{
		return new CsvFx(fileName, this);
	}

	@Override
	protected PlainText createPlainText(String fileName, Configuration configuration)
	{
		return new PlainTextFx(fileName, this, fileName.endsWith(Configuration.matrixExt) ? Highlighter.Matrix : Highlighter.None);
	}

	@Override
	protected SystemVars createVars(String fileName, Configuration configuration)
	{
		return new SystemVarsFx(fileName, this);
	}

	@Override
	protected IMatrixListener createMatrixListener()
	{
		return new MatrixListenerFx();
	}

	@Override
	public void error(Exception exception)
	{
		if (exception != null)
		{
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError(exception.getMessage());
		}
	}

	@Override
	public void popup(String message, Notifier notifier)
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
		return DialogsHelper.showUserTable(evaluator, title, table, columns);
	}

	@Override
	public Object input(AbstractEvaluator evaluator, String title, Object defaultValue, HelpKind helpKind, List<ReadableValue> dataSource, int timeout)
	{
		return DialogsHelper.showUserInput(evaluator, title, defaultValue, helpKind, dataSource, timeout);
	}

	public void showMatrixScheduler()
	{
		this.runnerListener.show(Common.node);
	}

	@Override
	public WizardManager getWizardManager()
	{
		return this.wizardManager;
	}

	//region private methods
	private static <T extends AbstractDocumentController<? extends Document>> T loadController(Class<T> controllerClass) throws Exception
	{
		ControllerInfo info = controllerClass.getAnnotation(ControllerInfo.class);
		if (info == null)
		{
			throw new Exception(String.format(R.FX_DOC_FACTORY_ERROR_INFO.get(), controllerClass));
		}

		URL resource = FxDocumentFactory.class.getResource(info.resourceName());
		FXMLLoader loader = new FXMLLoader(resource);
		Parent parent = loader.load();
		T controller = loader.getController();
		controller.setParent(parent);
		return controller;
	}
	//endregion
}
