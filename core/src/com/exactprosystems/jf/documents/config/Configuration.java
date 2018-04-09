/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.config;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.Do;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.client.AbstractClient;
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.client.ClientsPool;
import com.exactprosystems.jf.common.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.MvelEvaluator;
import com.exactprosystems.jf.common.report.HTMLReportFactory;
import com.exactprosystems.jf.common.report.ReportFactory;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.common.xml.schema.Xsd;
import com.exactprosystems.jf.documents.*;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.service.ServicePool;
import com.exactprosystems.jf.sql.DataBasePool;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import org.apache.log4j.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.*;
import java.math.MathContext;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DocumentInfo(
        kind = DocumentKind.CONFIGURATION,
		newName = "NewConfiguration",
		extension = "xml",
		description = "Jackfish configuration"
)
public class Configuration extends AbstractDocument
{
	public static final String	SEPARATOR			= ",";

	public static final String 	projectName 		= "JackFish";
	public static final String 	varExt 				= ".ini";
	public static final String 	dictExt 			= ".xml";
	public static final String 	matrixExt 			= ".jf";
	public static final String 	matrixFilter		= "*.jf";
	public static final char  	matrixDelimiter		= ';';
	public static final String 	unicodeDelimiter	= String.valueOf("\\\\u" + Integer.toHexString(Configuration.matrixDelimiter | 0x10000).substring(1));

	public static final String time					= "time";
	public static final String date					= "date";
	public static final String dateTime				= "dateTime";
	public static final String formats				= "formats";
	public static final String imports 				= "import";
	public static final String reports 				= "reports";
	public static final String vars 				= "vars";
	public static final String matrix 				= "matrix";
	public static final String appDict 				= "appDict";
	public static final String clientDict			= "clientDict";
	public static final String library 				= "library";
	public static final String userVars 			= "userVars";
    public static final String version              = "version";

	//region Global handlers
	public static final String globalHandler		= "globalHandler";
	public static final String globalHandlerEnable	= "enable";
	public static final String onTestCaseStart		= "onTestCaseStart";
	public static final String onTestCaseFinish		= "onTestCaseFinish";
	public static final String onTestCaseError		= "onTestCaseError";
	public static final String onStepStart			= "onStepStart";
	public static final String onStepFinish			= "onStepFinish";
	public static final String onStepError			= "onStepError";
	//endregion

	public static final String entryName			= "name";

	public static final String sqlEntry				= "sqlEntry";
	public static final String sqlJar				= "sqlJar";
	public static final String sqlConnection 		= "sqlConnection";

	public static final String clientEntry			= "clientEntry";
	public static final String clientDescription 	= "clientDescription";
	public static final String clientJar			= "clientJar";
	public static final String clientDictionary		= "clientDictionary";
	public static final String clientLimit			= "clientLimit";

	public static final String serviceEntry			= "serviceEntry";
	public static final String serviceDescription 	= "serviceDescription";
	public static final String serviceJar			= "serviceJar";

	public static final String appEntry				= "appEntry";
	public static final String appDescription 		= "appDescription";
	public static final String appDicPath			= "appDicPath";
	public static final String appJar				= "appJar";
	public static final String appWorkDir			= "appWorkDir";
	public static final String appStartPort			= "appStartPort";

	public static final String parametersEntry		= "parameters";
	public static final String parametersKey		= "key";
	public static final String parametersValue		= "value";

	//region default values for new project
	public static final String MATRIX_FOLDER		= "matrices";
	public static final String LIBRARY_FOLDER		= "libs";
	public static final String APP_DIC_FOLDER		= "appDic";
	public static final String CLIENT_DIC_FOLDER	= "clientDic";
	public static final String USER_VARS_FOLDER		= "myVars";
	public static final String USER_VARS_FILE		= "myVars.ini";
	public static final String REPORTS_FOLDER		= "reports";

	private static final MutableArrayList<MutableString> DEFAULT_IMPORTS = new MutableArrayList<>();
	static
	{
		DEFAULT_IMPORTS.add(new MutableString(List.class.getCanonicalName()));
		DEFAULT_IMPORTS.add(new MutableString(Map.class.getCanonicalName()));
		DEFAULT_IMPORTS.add(new MutableString(VersionInfo.class.getPackage().getName()));

		DEFAULT_IMPORTS.add(new MutableString(Instant.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(MathContext.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(Matcher.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(File.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(String.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(Color.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(ActionEvent.class.getPackage().getName()));

		DEFAULT_IMPORTS.add(new MutableString(Table.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(Do.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(Condition.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(AbstractClient.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(ErrorKind.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(Result.class.getPackage().getName()));
		DEFAULT_IMPORTS.add(new MutableString(Sys.class.getPackage().getName()));

		DEFAULT_IMPORTS.add(new MutableString(org.mvel2.MVEL.class.getPackage().getName()));
	}

	private static final MutableArrayList<MutableString> DEFAULT_FORMATS = new MutableArrayList<>();
	static
	{
		DEFAULT_FORMATS.add(new MutableString("dd.MM.yyyy HH:mm:ss"));
		DEFAULT_FORMATS.add(new MutableString("yyyy/MM/dd HH:mm:ss.SSS"));
		DEFAULT_FORMATS.add(new MutableString("yyyyMMdd-HH:mm:ss.SSS"));

		DEFAULT_FORMATS.add(new MutableString("dd/MM/yyyy"));

		DEFAULT_FORMATS.add(new MutableString("HH.mm"));
		DEFAULT_FORMATS.add(new MutableString("HH:mm:ss"));
		DEFAULT_FORMATS.add(new MutableString("HH:mm:ss Z"));
		DEFAULT_FORMATS.add(new MutableString("HH:mm:ss:SSS"));
	}
	public static final MutableString DEFAULT_TIME = new MutableString("HH:mm:ss.SSS");
	public static final MutableString DEFAULT_DATE = new MutableString("dd.MM.yyyy");
	public static final MutableString DEFAULT_DATE_TIME = new MutableString("dd.MM.yyyy HH:mm:ss.SSS");

	protected ConfigurationBean bean;

	protected String reportFactoryValue = HTMLReportFactory.class.getSimpleName();

	protected       boolean             changed;
	protected       ReportFactory       reportFactoryObj;
	protected       Map<String, Matrix> libs;
	protected       Map<String, Date>   documentsActuality;
	protected final Map<String, Object> globals;
	protected       Set<SystemVars>     systemVars;
	protected       ClientsPool         clients;
	protected       ServicePool         services;
	protected       ApplicationPool     applications;
	protected       DataBasePool        databases;

	protected       Date           lastUpdate   = new Date();
	protected final List<Document> subordinates = new ArrayList<>();
	protected       boolean        valid        = false;

	private static final Logger logger = Logger.getLogger(Configuration.class);

	//endregion

	public Configuration(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);

		this.bean = new ConfigurationBean();

		this.changed = false;

		this.globals = new HashMap<>();
		this.clients = new ClientsPool(factory);
		this.services = new ServicePool(factory);
		this.applications = new ApplicationPool(factory);
		this.databases = new DataBasePool(factory);

		this.libs = new HashMap<>();
		this.documentsActuality = new HashMap<>();
		this.systemVars = new HashSet<>();
	}

	public Configuration()
	{
		this("unknown", null);
	}

	public static Configuration createNewConfiguration(String pathToConfig, DocumentFactory factory)
	{
		Configuration config = new Configuration(pathToConfig, factory);

		config.bean.varsValue = new MutableString("vars.ini");
		config.bean.timeValue = DEFAULT_TIME;
		config.bean.dateValue = DEFAULT_DATE;
		config.bean.dateTimeValue = DEFAULT_DATE_TIME;
		config.bean.importsValue = DEFAULT_IMPORTS;
		config.bean.formatsValue = DEFAULT_FORMATS;
		config.bean.matricesValue.add(new MutableString(MATRIX_FOLDER));
		config.bean.librariesValue.add(new MutableString(LIBRARY_FOLDER));
		config.bean.userVarsValue.add(new MutableString(USER_VARS_FOLDER + File.separator + USER_VARS_FILE));
		config.bean.reportsValue.set(REPORTS_FOLDER);
		config.bean.clientDictionariesValue.add(new MutableString(CLIENT_DIC_FOLDER));
		config.bean.appDictionariesValue.add(new MutableString(APP_DIC_FOLDER));

		return config;
	}

	//region public getters
	public MutableString getTime()
	{
		return this.bean.timeValue;
	}

	public MutableString getDate()
	{
		return this.bean.dateValue;
	}

	public MutableString getDateTime()
	{
		return this.bean.dateTimeValue;
	}

	public MutableString getReports()
	{
		return this.bean.reportsValue;
	}

	public MutableString getVersion()
	{
		return this.bean.versionValue;
	}

	public MutableString getVars()
	{
		return this.bean.varsValue;
	}

	public MutableArrayList<MutableString> getImports()
	{
		return this.bean.importsValue;
	}

	public MutableArrayList<MutableString> getUserVars()
	{
		return this.bean.userVarsValue;
	}

	public GlobalHandler getGlobalHandler()
	{
		return this.bean.globalHandlerValue;
	}

	public MutableArrayList<MutableString> getFormatsValue()
	{
		return this.bean.formatsValue;
	}

	public MutableArrayList<MutableString> getAppDictionariesValue()
	{
		return this.bean.appDictionariesValue;
	}

	public MutableArrayList<MutableString> getClientDictionariesValue()
	{
		return this.bean.clientDictionariesValue;
	}

	public MutableArrayList<MutableString> getLibrariesValue()
	{
		return this.bean.librariesValue;
	}

	public MutableArrayList<MutableString> getMatricesValue()
	{
		return this.bean.matricesValue;
	}

	public IClientsPool getClientPool()
	{
		return this.clients;
	}

	public IApplicationPool getApplicationPool()
	{
		return this.applications;
	}

	public IServicesPool getServicesPool()
	{
		return this.services;
	}

	public DataBasePool getDataBasesPool()
	{
		return this.databases;
	}

	public SqlEntry getSqlEntry(String name)
	{
		return this.getEntry(name, this.bean.sqlEntriesValue);
	}

	public List<SqlEntry> getSqlEntries()
	{
		return this.bean.sqlEntriesValue;
	}

	public ClientEntry getClientEntry(String name)
	{
		return this.getEntry(name, this.bean.clientEntriesValue);
	}

	public List<ClientEntry> getClientEntries()
	{
		return this.bean.clientEntriesValue;
	}

	public ServiceEntry getServiceEntry(String name)
	{
		return this.getEntry(name, this.bean.serviceEntriesValue);
	}

	public List<ServiceEntry> getServiceEntries()
	{
		return this.bean.serviceEntriesValue;
	}

	public AppEntry getAppEntry(String name)
	{
		return this.getEntry(name, this.bean.appEntriesValue);
	}

	public List<AppEntry> getAppEntries()
	{
		return this.bean.appEntriesValue;
	}

	public final ReportFactory getReportFactory()
	{
		return this.reportFactoryObj;
	}

	public final Collection<String> getClients()
	{
		return this.bean.clientEntriesValue.stream().map(Entry::toString).collect(Collectors.toList());
	}

	public final Collection<String> getServices()
	{
		return this.bean.serviceEntriesValue.stream().map(Entry::toString).collect(Collectors.toList());
	}

	public final Collection<String> getApplications()
	{
		return this.bean.appEntriesValue.stream().map(Entry::toString).collect(Collectors.toList());
	}

	public Matrix getLib(String name)
	{
		return this.libs.get(name);
	}

	public Map<String, Matrix> getLibs()
	{
		return this.libs;
	}

	public final boolean isValid()
	{
		return this.valid;
	}

	/**
	 * Create evaluator
	 * @throws Exception if some of system variables failed to evaluating
	 *
	 * @see AbstractEvaluator
	 * @see MvelEvaluator
	 */
	public AbstractEvaluator createEvaluator() throws Exception
	{
		AbstractEvaluator evaluator = new MvelEvaluator();
		evaluator.addImports(toStringList(DEFAULT_IMPORTS));
		evaluator.addImports(toStringList(this.bean.importsValue));

		for (SystemVars sysVars : this.systemVars)
		{
			sysVars.injectVariables(evaluator);
		}
		evaluator.reset(this.getVersion().toString());

		return evaluator;
	}

	public Date getLastUpdateDate()
	{
		return new DateTime(this.lastUpdate);
	}

	/**
	 * @return <b>copy</b> of stored map
	 */
	public Map<String, Object> getStoreMap()
	{
		return new LinkedHashMap<>(this.globals);
	}

	/**
	 * @return List of registered documents, which was registered via {@link Configuration#register(Document)}
	 */
	public List<Document> getSubordinates()
	{
		return subordinates;
	}
	//endregion

	/**
	 * Add to the passed list all subcases from all libraries ( which has namespace parent item)
	 *
	 * @see NameSpace
	 * @see SubCase
	 */
	public void addSubcaseFromLibs(List<ReadableValue> list)
	{
		for (Map.Entry<String, Matrix> entry : this.getLibs().entrySet())
		{
			final String name = entry.getKey();
			Matrix lib = entry.getValue();

			if (lib != null)
			{
				MatrixItem nameSpaceItem = lib.getRoot().find(false, NameSpace.class, name);
				if(nameSpaceItem != null)
				{
					nameSpaceItem.stream()
							.filter(item -> item instanceof SubCase && !list.contains(new ReadableValue(item.getId())))
							.map(item -> (SubCase) item)
							.map(sc -> new ReadableValue(name + "." + sc.getId(), sc.getName()))
							.forEach(list::add);
				}
			}
		}
	}

	/**
	 * Store a object in stored map.<br>
	 * If name is null, the object via passed name will removed from the stored map. <br>
	 * Otherwise for the stored map will added new entry.
	 *
	 * @param name name stored value
	 * @param value stored value
	 */
	public void storeGlobal(String name, Object value)
	{
		synchronized (this.globals)
		{
			if (value == null)
			{
				this.globals.remove(name);
			}
			else
			{
				this.globals.put(name, value);
			}
		}
	}

	/**
	 * @return a object from a stored map by the passed name
	 */
	public Object restoreGlobal(String name)
	{
		synchronized (this.globals)
		{
			return this.globals.get(name);
		}
	}

	/**
	 * Refresh configuration.
	 * @throws Exception if something went wrong
	 */
	public void refresh()  throws Exception
	{
		this.refreshVars();
		this.refreshLibs();
		this.refreshMatrices();
		this.refreshAppDictionaries();
		this.refreshClientDictionaries();
		this.refreshReport();

		this.lastUpdate = new Date();
		
		this.display();
	}

	/**
	 * Register the passed {@link Document} into the configuration ( it mean add the passed document to the {@link Configuration#subordinates}
	 * @param doc registered document
	 *
	 * @see Document
	 * @see Configuration#getSubordinates()
	 */
	public final void register(Document doc)
	{
		synchronized (this.subordinates)
		{
			this.subordinates.add(doc);
		}
	}

	/**
	 * Unregister the passed {@link Document} into the configuration (remove the passed document from the collection {@link Configuration#subordinates} <br>
	 * @param doc registered document
	 *
	 * @see Document
	 */
	public final void unregister(Document doc)
	{
		synchronized (this.subordinates)
		{
			this.subordinates.remove(doc);
		}
	}

	/**
	 * For each registered document (as file) by passed {@link DocumentKind} array will apply {@link BiConsumer} applier.
	 *
	 * @param applier {@link java.util.function.BiFunction}
	 * @param kinds array of kinds, which need be get around
	 */
	public void forEachFile(BiConsumer<File, DocumentKind> applier, DocumentKind... kinds)
	{
		for (DocumentKind kind : kinds)
		{
			switch (kind)
			{
				case CONFIGURATION:
					applier.accept(new File(this.getNameProperty().get()), kind);
					break;

				case SYSTEM_VARS:
					this.systemVars.stream()
							.map(v -> new File(v.getNameProperty().get()))
							.forEach(file -> applier.accept(file, kind));
					break;

				case GUI_DICTIONARY:
					this.bean.appDictionariesValue.forEach(ms ->
					{
						File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
						this.applyToAllFile(folderFile, applier, kind);
					});
					break;

				case MESSAGE_DICTIONARY:
					this.bean.clientDictionariesValue.forEach(ms ->
					{
						File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
						this.applyToAllFile(folderFile, applier, kind);
					});
					break;

				case LIBRARY:
					this.libs.values().stream()
							.map(v -> new File(v.getNameProperty().get()))
							.forEach(file -> applier.accept(file, kind));
					break;

				case MATRIX:
					this.bean.matricesValue.forEach(ms ->
					{
						File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
						this.applyToAllFile(folderFile, applier, kind);
					});
					break;

				case REPORTS:
					this.applyToAllFile(new File(this.bean.reportsValue.get()), applier, kind);
					break;

				case PLAIN_TEXT:
					List<File> excludeFiles = new ArrayList<>();
					excludeFiles.add(new File(this.getNameProperty().get()));

					excludeFiles.addAll(files(this.systemVars.stream(), ms -> ms.getNameProperty().get()));
					excludeFiles.addAll(files(this.bean.appDictionariesValue.stream(), ms -> MainRunner.makeDirWithSubstitutions(ms.get())));
					excludeFiles.addAll(files(this.bean.clientDictionariesValue.stream(), ms -> MainRunner.makeDirWithSubstitutions(ms.get())));
					excludeFiles.addAll(files(this.libs.values().stream(), ms -> getNameProperty().get()));
					excludeFiles.addAll(files(this.bean.matricesValue.stream(), ms -> MainRunner.makeDirWithSubstitutions(ms.get())));
					excludeFiles.add(new File(this.bean.reportsValue.get()));
					List<String> ex = excludeFiles.stream()
							.map(ConfigurationFx::path)
							.collect(Collectors.toList());
					this.applyToAllFile(new File("."), applier, DocumentKind.PLAIN_TEXT, file -> ex.contains(ConfigurationFx.path(file)));
					break;

				default:

			}
		}
	}

	/**
	 * For each registered document by passed {@link DocumentKind} array will apply {@link Consumer} applier
	 * @param applier {@link Consumer}
	 * @param kinds array of document kinds, which need be get around
	 */
	public void forEach(Consumer<Document> applier, DocumentKind... kinds)
	{
		DocumentFactory consoleFactory = new ConsoleDocumentFactory(VerboseLevel.None);
		consoleFactory.setConfiguration(this);

		for (DocumentKind kind : kinds)
		{
			switch (kind)
			{
				case CONFIGURATION:
					applier.accept(this);
					break;

				case SYSTEM_VARS:
					this.systemVars.forEach(applier);
					break;

				case GUI_DICTIONARY:
					this.bean.appDictionariesValue.forEach(ms ->
					{
						File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
						this.applyToAll(folderFile, kind, consoleFactory, applier);
					});
					break;

				case MESSAGE_DICTIONARY:
					this.bean.clientDictionariesValue.forEach(ms ->
					{
						File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
						this.applyToAll(folderFile, kind, consoleFactory, applier);
					});
					break;

				case LIBRARY:
					this.libs.values().forEach(applier);
					break;

				case MATRIX:
					this.bean.matricesValue.forEach(ms ->
					{
						File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
						this.applyToAll(folderFile, kind, consoleFactory, applier);
					});
					break;

				default:

			}
		}
	}

	/**
	 * Replace all entries from the {@link Configuration#globals} storedMap to entries from the passed map
	 */
	public void storeMap(Map<String, Object> map)
	{
		this.globals.clear();
		this.globals.putAll(map);
	}

	//region protected methods, may be overrided

	/**
	 * Refresh all libraries.<br>
	 * If some of a library was changed, the library will reloaded.
	 */
	protected void refreshLibs()
	{
		if (this.bean.librariesValue == null)
		{
			return;
		}

		for (MutableString folder : this.bean.librariesValue)
		{
			File folderFile = new File(MainRunner.makeDirWithSubstitutions(folder.get()));
			if (folderFile.exists() && folderFile.isDirectory())
			{
				File[] libFiles = folderFile.listFiles((dir, name) -> name != null && name.endsWith(matrixExt));

				for (File libFile : Optional.ofNullable(libFiles).orElse(new File[0]))
				{
					Date fileTime = new Date(libFile.lastModified());
					Date previousTime = this.documentsActuality.put(libFile.getAbsolutePath(), fileTime);

					if (previousTime != null && previousTime.compareTo(fileTime) >= 0)
					{
						continue;
					}

					loadLibrary(libFile);
				}
			}
			else
			{
				this.libs.entrySet().removeIf(entry -> entry.getValue().getNameProperty().get().contains(folderFile.getAbsolutePath()));
			}
		}
	}

	/**
	 * Reload all variables
	 */
	protected void refreshVars()
	{
		try
		{
			this.systemVars.clear();

			this.setUserVariablesFromMask(this.bean.varsValue.get());
			for (MutableString userVariables : this.bean.userVarsValue)
			{
				this.setUserVariablesFromMask(userVariables.get());
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	protected void refreshMatrices()
	{}

	/**
	 * Refresh all app dictionaries<br>
	 * If a dictionary was changed, the dictionary will reloaded.
	 */
	protected void refreshAppDictionaries()
	{
		for (AppEntry entry : this.bean.appEntriesValue)
		{
			String name = entry.entryNameValue;
			String dicPath = entry.appDicPathValue;
			if (Str.IsNullOrEmpty(dicPath))
			{
				continue;
			}

			File dicFile = new File(MainRunner.makeDirWithSubstitutions(dicPath));
			Date currentTime  = new Date(dicFile.lastModified());
			Date previousTime = this.documentsActuality.get(name + dicFile.getAbsolutePath());

			if (previousTime != null && !currentTime.after(previousTime))
			{
				continue;
			}

			try
			{
				if (this.applications.isLoaded(name))
				{
					IApplicationFactory factory = this.applications.loadApplicationFactory(name);
					IGuiDictionary dictionary = this.applications.getDictionary(entry);
					factory.init(dictionary);
					this.documentsActuality.put(name + dicFile.getAbsolutePath(), currentTime);
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	protected void refreshClientDictionaries()
	{}

	protected void refreshReport()
	{}
	//endregion

	//region interface Document

	/**
	 * Load a configuration from a passed {@link Reader} object
	 * @throws Exception if configuration can't be loaded
	 */
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		try
		{
			this.valid = false;

			JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationBean.jaxbContextClasses);

			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaFile = new StreamSource(Xsd.class.getResourceAsStream("Configuration.xsd"));
			Schema schema = schemaFactory.newSchema(schemaFile);


			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			unmarshaller.setSchema(schema);
			unmarshaller.setEventHandler(event ->
			{
				System.out.println("Error in configuration : " + event);
				return false;
			});

			this.bean = (ConfigurationBean) unmarshaller.unmarshal(reader);
			this.changed = false;
			this.reportFactoryObj = new HTMLReportFactory();

			DateTime.setFormats(this.bean.timeValue.get(), this.bean.dateValue.get(), this.bean.dateTimeValue.get());
			Converter.setFormats(toStringList(this.bean.formatsValue));

			this.valid = true;
		}
		catch (UnmarshalException e)
		{
			logger.error(e.getCause().getMessage(), e.getCause());
			throw new Exception(e.getCause().getMessage(), e.getCause());
		}
	}

	/**
	 * Check, can we close the configuration or not <br>
	 * Checked configuration and all subordinates from the configuration
	 *
	 * @see Configuration#subordinates
	 *
	 * @return true, if config can be closed and false otherwise.
	 * @throws Exception if something went wrong
	 */
	@Override
	public boolean canClose() throws Exception
	{
		boolean res = true;

		synchronized (this.subordinates)
		{
			for (Document doc : this.subordinates)
			{
				res = res && doc.canClose();
			}
		}
		return res;
	}

	/**
	 * Close the configuration. <br>
	 * Before closing the configuration, will all subordinates saved into the setting as opened file and will closed.<br>
	 * This method should be invoked only after check {@link Configuration#canClose()}
	 * @throws Exception if something went wrong
	 *
	 * @see Configuration#getSubordinates()
	 * @see Configuration#canClose()
	 */
	@Override
	public void close() throws Exception
	{
		super.close();
		Settings settings = super.getFactory().getSettings();

		Set<Document> copy;
		synchronized (this.subordinates)
		{
			copy = new HashSet<>(this.subordinates);
		}

		// save list of all opened documents ...
		settings.removeAll(Settings.MAIN_NS, Settings.OPENED);
		settings.saveIfNeeded();

		for (Document doc : copy)
		{
			try
			{
				DocumentKind kind = DocumentKind.byDocument(doc);
				if (!doc.getNameProperty().isNullOrEmpty() && new File(doc.getNameProperty().get()).exists())
				{
					settings.setValue(Settings.MAIN_NS, Settings.OPENED, doc.getNameProperty().get(), kind.toString());
				}
				doc.close();
			}
			catch (Exception e)
			{
				logger.error(e.getMessage());
			}
		}
		settings.saveIfNeeded();

		this.services.stopAllServices();
		this.applications.stopAllApplications(true);
	}

	/**
	 * Save the configuration to file with path fileName
	 * @param fileName file path to save the configuration
	 * @throws Exception if file for saving not found
	 */
	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);

		try (OutputStream os = new FileOutputStream(new File(fileName)))
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationBean.jaxbContextClasses);

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this.bean, os);

			this.saved();
		}
		catch (FileNotFoundException e)
		{
			logger.error(String.format("File '%s' is not found.", fileName));
			throw e;
		}
	}

	//endregion

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.changed || this.bean.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();

		this.bean.saved();
		this.changed = false;
	}

	//endregion

	/**
	 * Convert the passed MutableList of MutableString to a simple List of String
	 *
	 * @see MutableArrayList
	 * @see MutableString
	 */
	public static List<String> toStringList(MutableArrayList<MutableString> str)
	{
		return str.stream().map(a -> MainRunner.makeDirWithSubstitutions(a.get())).collect(Collectors.toList());
	}

	//region private methods

	/**
	 * Get entry by passed name from entries list
	 * @param name of entry, which should be returned
	 * @param entries list of entries
	 * @param <T> type of entry ( child of {@link Entry} class)
	 * @return a entry or null, if entry by passed name not found in the passed list of entries
	 */
	private <T extends Entry> T getEntry(String name, List<T> entries)
	{
		if (entries == null)
		{
			return null;
		}
		return entries.stream()
				.filter(e -> name.equals(e.toString()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Load a library by passed file name and stored into {@link Configuration#libs}
	 */
	private void loadLibrary(File libFile)
	{
		try (Reader reader = CommonHelper.readerFromFile(libFile))
		{
			Matrix matrix = (Matrix) getFactory().createDocument(DocumentKind.LIBRARY, libFile.getAbsolutePath());
			matrix.load(reader);
			List<String> namespaces = matrix.listOfIds(NameSpace.class);
			if (namespaces.isEmpty())
			{
				matrix.close();
			}
			else
			{
				for (String ns : namespaces)
				{
					this.libs.put(ns, matrix);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private <T> List<File> files(Stream<T> stream, Function<T, String> map)
	{
		return stream.map(map).map(File::new).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private <T> T objectFromClassName(String name, Class<T> baseType) 	throws Exception
	{
		try
		{
			Class<?> type = classForName(name, baseType);

			if (!baseType.isAssignableFrom(type))
			{
				throw new Exception(String.format(R.CONFIGURATION_OBJECT_FROM_CLASS_NAME_EXCEPTION.get(), name, baseType.getName() ));
			}

			return (T)type.newInstance();
		}
		catch (Exception e)
		{
			logger.error(String.format("objectFromClassName(%s, %s)", name, baseType));
			throw e;
		}
	}

	private <T> Class<?> classForName(String name, Class<T> baseType) throws ClassNotFoundException
	{
		Class<?> type;
		try
		{
			type = Class.forName(name);
		}
		catch (ClassNotFoundException e)
		{
			type = Class.forName(baseType.getPackage().getName() + "." + name);
		}
		return type;
	}

	/**
	 * Load a userVariables file and stored into {@link Configuration#systemVars}
	 * @param userVariablesFileName a name of file, which will load as SystemVariables
	 * @throws Exception if load a SystemFile was failed
	 */
	private void setUserVariablesFromMask(String userVariablesFileName)  throws Exception
	{
		if (Str.IsNullOrEmpty(userVariablesFileName))
		{
			return;
		}

		final File file = new File(MainRunner.makeDirWithSubstitutions(userVariablesFileName));
		if (file.exists())
		{
			try (Reader reader = CommonHelper.readerFromFile(file))
			{
				SystemVars vars = (SystemVars) getFactory().createDocument(DocumentKind.SYSTEM_VARS, userVariablesFileName);
				vars.load(reader);
				this.systemVars.add(vars);
			}
		}
	}

	private void applyToAll(File path, DocumentKind kind, DocumentFactory factory, Consumer<Document> applier)
	{
		if (path.isDirectory())
		{
			File[] files = path.listFiles();
			if (files != null)
			{
				Arrays.stream(files).forEach(file -> applyToAll(file, kind, factory, applier));
			}
		}
		else
		{
			try
			{
				Document doc = factory.createDocument(kind, Common.getRelativePath(path.getAbsolutePath()));
				try (Reader reader = CommonHelper.readerFromFileName(doc.getNameProperty().get()))
				{
					doc.load(reader);
				}
				applier.accept(doc);
			} 
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
	
    private void applyToAllFile(File path, BiConsumer<File, DocumentKind> applier, DocumentKind kind)
	{
		this.applyToAllFile(path, applier, kind, file -> false);
	}

	private void applyToAllFile(File path, BiConsumer<File, DocumentKind> applier, DocumentKind kind, Predicate<File> filter)
	{
		if (filter.test(path))
		{
			return;
		}
		if (path.isDirectory())
		{
			File[] files = path.listFiles();
			if (files != null)
			{
				Arrays.stream(files).forEach(file -> this.applyToAllFile(file, applier, kind, filter));
			}
		}
		else
		{
			applier.accept(path, kind);
		}
	}
	//endregion
}