/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
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

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The abstract factory for all documents.
 * All actions with documents ( e.g. create, display and etc) should call methods from the factory
 */
public abstract class DocumentFactory 
{
	protected       Configuration      configuration;
	protected final Settings           settings;
	protected       MatrixStateChanged listener;

	@FunctionalInterface
	public interface MatrixStateChanged
	{
		void changed(Matrix matrix, MatrixState oldState, MatrixState newState);
	}

	public DocumentFactory()
	{
		this.settings = Settings.load(Settings.SETTINGS_PATH);
	}

	public final void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
	}
	
	public final Configuration getConfiguration()
	{
		return this.configuration;
	}

	public final Settings getSettings()
	{
		return this.settings;
	}
	
	public final void setMatrixChangeListener(MatrixStateChanged listener)
	{
	    this.listener = listener;
	}

	/**
	 * Create a evaluator. If configuration doesn't exists, null will return.
	 */
	public final AbstractEvaluator createEvaluator()
	{
		try
		{
			this.checkConfiguration();
			return this.configuration.createEvaluator();
		}
		catch (Exception e)
		{
			this.error(e);
		}
		return null;
	}

	/**
	 * Create a context for a matrix. If configuration doesn't exists, null will return
	 */
	public final Context createContext()
	{
		try
		{
			this.checkConfiguration();
			return this.createContext(this.configuration, this.createMatrixListener());
		}
		catch (Exception e)
		{
			this.error(e);
		}
		return null;
	}

	/**
	 * Create a simple document for a matrix. For work with the document in future, need call {@link Document#create()} or {@link Document#load(Reader)}
	 * <p>
	 * If the parameter kind is {@link DocumentKind#CONFIGURATION}, a new configuration will return.
	 * <p>
	 * If configuration doesn't exists, null will return
	 */
	public final Document createDocument(DocumentKind kind, String fileName)
	{
		try
		{
			if (kind != DocumentKind.CONFIGURATION)
			{
				this.checkConfiguration();
			}
			switch (kind)
			{
				case CONFIGURATION:
					return this.createConfig(fileName, this.settings);

				case MATRIX:
					Matrix matrix = this.createMatrix(fileName, this.configuration, this.createMatrixListener());
					matrix.getStateProperty().setOnChangeListener((oldState, newState) -> Optional.ofNullable(this.listener).ifPresent(l -> l.changed(matrix, oldState, newState)));
					return matrix;

				case LIBRARY:
					return this.createLibrary(fileName, this.configuration, this.createMatrixListener());

				case GUI_DICTIONARY:
					return this.createAppDictionary(fileName, this.configuration);

				case MESSAGE_DICTIONARY:
					return this.createClientDictionary(fileName, this.configuration);

				case SYSTEM_VARS:
					return this.createVars(fileName, this.configuration);

				case CSV:
					return this.createCsv(fileName, this.configuration);

				case PLAIN_TEXT:
					return this.createPlainText(fileName, this.configuration);

				case REPORTS:
					return null;
			}
		}
		catch (Exception e)
		{
			error(e);
		}
		return null;
	}

	/**
	 * Used for displaying document
	 *
	 * @param doc the document, which will be displayed
	 *
	 * @throws Exception if displaying was failed
	 */
	public void showDocument(Document doc) throws Exception
	{
		doc.display();
	}

	public abstract void showMatrixScheduler();

	//region public abstract methods

	/**
	 * @return a instance of the WizardManager, for working with any wizard
	 *
	 * @see WizardManager
	 */
	public abstract WizardManager getWizardManager();

	/**
	 * Display a simple notification. The notification will has the passed message and the passed type of the notifier
	 *
	 * @param message the message, which will used for displaying
	 * @param notifier the type of notification.
	 */
	public abstract void popup(String message, Notifier notifier);

	/**
	 * Display a countdown for the passed matrix.
	 * This used on the action {@link com.exactprosystems.jf.actions.system.Wait}
	 *
	 * @param ms the millis for the displaying
	 * @param matrix the matrix, which waited
	 */
	public abstract void showWaits(long ms, Matrix matrix);

	/**
	 * Display a simple input dialog for interaction with a user.
	 * User can activate this dialog via the action {@link com.exactprosystems.jf.actions.system.Input}
	 *
	 * @param evaluator the evaluator for evaluating user values
	 * @param title the title for the input dialog
	 * @param defaultValue the default value for the dialog ( on {@link ConsoleDocumentFactory} just default value will return)
	 * @param helpKind the kind of help for fill the dialog
	 * @param dataSource the list of possible value for the dialog ( this parameter used only if helpKind is {@link HelpKind#ChooseFromList}
	 * @param timeout the timeout, after which the dialog should be closed
	 *
	 * @return a user value, or default value.
	 */
	public abstract Object input(AbstractEvaluator evaluator, String title, Object defaultValue, HelpKind helpKind, List<ReadableValue> dataSource, int timeout);

	/**
	 * Display a simple dialog with a table, which user can edit.
	 *
	 * @param evaluator the evaluator for evaluating values
	 * @param title the title of the dialog
	 * @param table the table, which used should edit
	 * @param columns a map, where key - a header name from the table, value - if value is true, user can edit this column. Otherwise - no.
	 *
	 * @return true, if user success edited the table, otherwise false will return
	 */
	public abstract boolean editTable(AbstractEvaluator evaluator, String title, Table table, Map<String, Boolean> columns);
	//endregion

	//region protected abstract methods
	protected abstract void error(Exception exception);

	protected abstract Context createContext(Configuration configuration, IMatrixListener matrixListener) throws Exception;

	protected abstract Configuration createConfig(String fileName, Settings settings) throws Exception;

	protected abstract Matrix createLibrary(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception;

	protected abstract Matrix createMatrix(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception;

	protected abstract MessageDictionary createClientDictionary(String fileName, Configuration configuration) throws Exception;

	protected abstract GuiDictionary createAppDictionary(String fileName, Configuration configuration) throws Exception;

	protected abstract Csv createCsv(String fileName, Configuration configuration) throws Exception;

	protected abstract PlainText createPlainText(String fileName, Configuration configuration) throws Exception;

	protected abstract SystemVars createVars(String fileName, Configuration configuration) throws Exception;

	protected abstract IMatrixListener createMatrixListener();
	//endregion

	//region private methods
	private void checkConfiguration() throws EmptyConfigurationException
	{
		if (this.configuration == null)
		{
			throw new EmptyConfigurationException(R.EMPTY_CONFIGURATION_EXCEPTION.get());
		}
	}
	//endregion
}
