////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
import com.exactprosystems.jf.documents.matrix.parser.listeners.DummyRunnerListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.MatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.service.ServicePool;
import com.exactprosystems.jf.sql.DataBasePool;
import com.exactprosystems.jf.tool.Common;
import org.apache.log4j.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.math.MathContext;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.NONE)

@DocumentInfo(
        kind = DocumentKind.CONFIGURATION,
		newName = "NewConfiguration",
		extentioin = "xml",
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

	public static final MutableArrayList<MutableString> DEFAULT_IMPORTS = new MutableArrayList<>();
	static
	{
        DEFAULT_IMPORTS.add(new MutableString(List.class.getCanonicalName()));
        DEFAULT_IMPORTS.add(new MutableString(Map.class.getCanonicalName()));
        DEFAULT_IMPORTS.add(new MutableString(VersionInfo.class.getPackage().getName()));

        DEFAULT_IMPORTS.add(new MutableString(Instant.class.getPackage().getName()));
        DEFAULT_IMPORTS.add(new MutableString(MathContext.class.getPackage().getName()));
        DEFAULT_IMPORTS.add(new MutableString(List.class.getPackage().getName()));
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

	public static final MutableArrayList<MutableString> DEFAULT_FORMATS = new MutableArrayList<>();
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

	//endregion

	@XmlElement(name = time)
	protected MutableString timeValue;

	@XmlElement(name = date)
	protected MutableString dateValue;

	@XmlElement(name = dateTime)
	protected MutableString dateTimeValue;

	@XmlElement(name = formats)
	protected MutableArrayList<MutableString> formatsValue;

	@XmlElement(name = reports)
	protected MutableString reportsValue;

    @XmlElement(name = version)
    protected MutableString versionValue;

	@XmlElement(name = imports)
	protected MutableArrayList<MutableString> importsValue;

	@XmlElement(name = vars)
	protected MutableString varsValue;

	@XmlElement(name = userVars)
	protected MutableArrayList<MutableString> userVarsValue;

	@XmlElement(name = matrix)
	protected MutableArrayList<MutableString> matricesValue;

	@XmlElement(name = appDict)
	protected MutableArrayList<MutableString> appDictionariesValue;

	@XmlElement(name = clientDict)
	protected MutableArrayList<MutableString> clientDictionariesValue;

	@XmlElement(name = library)
	protected MutableArrayList<MutableString> librariesValue;

	@XmlElement(name = globalHandler)
	public GlobalHandler globalHandlerValue;

	@XmlElement(name = sqlEntry)
	public MutableArrayList<SqlEntry> sqlEntriesValue;

	@XmlElement(name = clientEntry)
	public MutableArrayList<ClientEntry> clientEntriesValue;

	@XmlElement(name = serviceEntry)
	public MutableArrayList<ServiceEntry> serviceEntriesValue;

	@XmlElement(name = appEntry)
	public MutableArrayList<AppEntry> appEntriesValue;

	public Configuration(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);

		this.changed 					= false;

		this.timeValue					= new MutableString();
		this.dateValue					= new MutableString();
		this.dateTimeValue				= new MutableString();
		this.formatsValue				= new MutableArrayList<MutableString>();
		this.reportsValue				= new MutableString();
        this.versionValue               = new MutableString();

		this.globalHandlerValue			= new GlobalHandler();

		this.sqlEntriesValue 			= new MutableArrayList<SqlEntry>();
		this.clientEntriesValue			= new MutableArrayList<ClientEntry>();
		this.serviceEntriesValue		= new MutableArrayList<ServiceEntry>();
		this.appEntriesValue 			= new MutableArrayList<AppEntry>();
		this.importsValue				= new MutableArrayList<MutableString>();
		this.varsValue					= new MutableString();
		this.userVarsValue				= new MutableArrayList<MutableString>();
		this.matricesValue				= new MutableArrayList<MutableString>();
		this.appDictionariesValue		= new MutableArrayList<MutableString>();
		this.clientDictionariesValue	= new MutableArrayList<MutableString>();
		this.librariesValue				= new MutableArrayList<MutableString>();

		this.globals 					= new HashMap<String, Object>();
		this.clients 					= new ClientsPool(factory);
		this.services 					= new ServicePool(factory);
		this.applications 				= new ApplicationPool(factory);
		this.databases 					= new DataBasePool(factory);

		this.libs 						= new HashMap<String, Matrix>();
		this.documentsActuality 		= new HashMap<String, Date>();
		this.systemVars					= new HashSet<SystemVars>();
	}

	public Configuration()
	{
		this("unknown", null);
	}

	public static Configuration createNewConfiguration(String pathToConfig, DocumentFactory factory)
	{
		Configuration config = new Configuration(pathToConfig, factory);

		config.varsValue = new MutableString("vars.ini");
		config.timeValue = DEFAULT_TIME;
		config.dateValue = DEFAULT_DATE;
		config.dateTimeValue = DEFAULT_DATE_TIME;
		config.importsValue = DEFAULT_IMPORTS;
		config.formatsValue = DEFAULT_FORMATS;
		config.matricesValue.add(new MutableString(MATRIX_FOLDER));
		config.librariesValue.add(new MutableString(LIBRARY_FOLDER));
		config.userVarsValue.add(new MutableString(USER_VARS_FOLDER + File.separator + USER_VARS_FILE));
		config.reportsValue.set(REPORTS_FOLDER);
		config.clientDictionariesValue.add(new MutableString(CLIENT_DIC_FOLDER));
		config.appDictionariesValue.add(new MutableString(APP_DIC_FOLDER));

		return config;
	}

	public MutableString getTime()
	{
		return this.timeValue;
	}

	public MutableString getDate()
	{
		return this.dateValue;
	}

	public MutableString getDateTime()
	{
		return this.dateTimeValue;
	}

	public MutableString getReports()
	{
		return this.reportsValue;
	}

    public MutableString getVersion()
    {
        return this.versionValue;
    }

	public MutableString getVars()
	{
		return this.varsValue;
	}

	public MutableArrayList<MutableString> getUserVars()
	{
		return this.userVarsValue;
	}

	public GlobalHandler getGlobalHandler()
	{
		return globalHandlerValue;
	}

	public void addSubcaseFromLibs(List<ReadableValue> list)
	{
		for (Map.Entry<String, Matrix> entry : getLibs().entrySet())
		{
			final String name = entry.getKey();
			Matrix lib = entry.getValue();

			if (lib != null)
			{
				MatrixItem mitem = lib.getRoot().find(false, NameSpace.class, name);
				if(mitem != null)
				{
					mitem.bypass(it ->
					{
						if (it instanceof SubCase)
						{
							list.add(new ReadableValue(name + "." + it.getId(), ((SubCase) it).getName()));
						}
					});
				}
			}
		}
	}

	public MutableArrayList<MutableString> getAppDictionariesValue()
	{
		return appDictionariesValue;
	}

	public MutableArrayList<MutableString> getClientDictionariesValue()
	{
		return clientDictionariesValue;
	}

	public MutableArrayList<MutableString> getLibrariesValue()
	{
		return librariesValue;
	}

	public MutableArrayList<MutableString> getMatricesValue()
	{
		return matricesValue;
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

	public Object restoreGlobal(String name)
	{
		synchronized (this.globals)
		{
			return this.globals.get(name);
		}
	}

	public AbstractEvaluator createEvaluator() throws Exception
	{
		if (Str.IsNullOrEmpty(this.evaluatorValue))
		{
			throw new Exception("Empty evaluator class name.");
		}

		AbstractEvaluator evaluator	= objectFromClassName(this.evaluatorValue, AbstractEvaluator.class);
		evaluator.addImports(toStringList(DEFAULT_IMPORTS));
		evaluator.addImports(toStringList(this.importsValue));

		for (SystemVars vars : this.systemVars)
		{
			vars.injectVariables(evaluator);
		}
		evaluator.reset("" + getVersion());

		return evaluator;
	}

	public Date getLastUpdateDate()
	{
	    return this.lastUpdate;
	}

	public void refresh()  throws Exception
	{
		refreshVars();
		refreshLibs();
		refreshMatrices();
		refreshAppDictionaries();
		refreshClientDictionaries();
		refreshReport();

		this.lastUpdate = new Date();
		
		display();
	}


	protected void refreshLibs()
	{
		IMatrixListener checker = new MatrixListener();
		if (this.librariesValue == null)
		{
			return;
		}

		for (MutableString folder : this.librariesValue)
		{
			File folderFile = new File(MainRunner.makeDirWithSubstitutions(folder.get()));
			if (folderFile.exists() && folderFile.isDirectory())
			{
				File[] libFiles = folderFile.listFiles((dir, name) -> name != null && name.endsWith(matrixExt));

				for (File libFile : libFiles)
				{
					Date fileTime = new Date(libFile.lastModified());
					Date previousTime = this.documentsActuality.put(libFile.getAbsolutePath(), fileTime);

					if (previousTime != null && previousTime.compareTo(fileTime) >= 0)
					{
						continue;
					}

					try (Reader reader = CommonHelper.readerFromFile(libFile))
					{
						Context context = getFactory().createContext();
						MatrixRunner runner = context.createRunner(libFile.getName(), null, new Date(), null);
						Matrix matrix = getFactory().createLibrary(libFile.getAbsolutePath(), runner);
						if (!checker.isOk())
						{
							logger.error("Library load error: [" + libFile.getName() + "] " + checker.getExceptionMessage());
							continue;
						}
						matrix.load(reader);
						List<String> namespaces = matrix.listOfIds(NameSpace.class);
						if (namespaces.isEmpty())
						{
							matrix.close(this.getFactory().getSettings());
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
			}
			else
			{
				this.libs.entrySet().removeIf(entry -> entry.getValue().getName().contains(folderFile.getAbsolutePath()));
			}
		}
	}

	protected void refreshVars()
	{
		try
		{
			this.systemVars.clear();

			setUserVariablesFromMask(this.varsValue.get());
			for (MutableString userVars : this.userVarsValue)
			{
				setUserVariablesFromMask(userVars.get());
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	protected void refreshMatrices()
	{}

	protected void refreshAppDictionaries()
	{
		for (AppEntry entry : this.appEntriesValue)
		{
			String name = entry.entryNameValue;
			String dicPath = entry.appDicPathValue;
			if (Str.IsNullOrEmpty(dicPath))
			{
				continue;
			}

			File dicFile = new File(MainRunner.makeDirWithSubstitutions(dicPath));
			Date currentTime  = new Date(dicFile.lastModified());
			Date previousTime = this.documentsActuality.get(dicFile.getAbsolutePath());

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
					this.documentsActuality.put(dicFile.getAbsolutePath(), currentTime);
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

    //------------------------------------------------------------------------------------------------------------------
    // interface Document
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void load(Reader reader) throws Exception
    {
    	super.load(reader);
    	try
    	{
			this.valid = false;

	        jaxbContextClasses[0] = this.getClass();
	        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);


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

	        Configuration config = (Configuration) unmarshaller.unmarshal(reader);
	        config.factory = getFactory();

	        setAll(config);

			this.reportFactoryObj		= objectFromClassName(reportFactoryValue, ReportFactory.class);

			DateTime.setFormats(this.timeValue.get(), this.dateValue.get(), this.dateTimeValue.get());
			Converter.setFormats(toStringList(this.formatsValue));

			refresh();

			this.valid = true;
    	}
		catch (UnmarshalException e)
		{
			throw new Exception(e.getCause().getMessage(), e.getCause());
		}
	}

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

    @Override
	public void close(Settings settings) throws Exception
	{
		super.close(settings);

		Set<Document> copy;
		synchronized (this.subordinates)
		{
			copy = new HashSet<>();
			copy.addAll(this.subordinates);
		}

		// save list of all opened documents ...
		settings.removeAll(Settings.MAIN_NS, Settings.OPENED);
		settings.saveIfNeeded();

		for (Document doc : copy)
		{
			try
			{
				DocumentKind kind = DocumentKind.byDocument(doc);
				if (doc.hasName())
				{
					settings.setValue(Settings.MAIN_NS, Settings.OPENED, doc.getName(), kind.toString());
				}
				doc.close(settings);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage());
			}
		}
		settings.saveIfNeeded();

		this.services.stopAllServices();
		this.applications.stopAllApplications();
    }

    @Override
    public void save(String fileName) throws Exception
    {
    	super.save(fileName);

        try(OutputStream os = new FileOutputStream(new File(fileName)))
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, os);

			saved();
        }
        catch (FileNotFoundException e)
        {
            logger.error(String.format("File '%s' is not found.",fileName));
            throw e;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // interface Mutable
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}

		return this.timeValue.isChanged()
				|| this.dateValue.isChanged()
				|| this.dateTimeValue.isChanged()
				|| this.formatsValue.isChanged()
                || this.versionValue.isChanged()
				|| this.reportsValue.isChanged()
				|| this.sqlEntriesValue.isChanged()
				|| this.clientEntriesValue.isChanged()
				|| this.serviceEntriesValue.isChanged()
				|| this.appEntriesValue.isChanged()
				|| this.importsValue.isChanged()
				|| this.varsValue.isChanged()
				|| this.userVarsValue.isChanged()
				|| this.matricesValue.isChanged()
				|| this.appDictionariesValue.isChanged()
				|| this.clientDictionariesValue.isChanged()
				|| this.librariesValue.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();

		this.changed = false;
		this.timeValue.saved();
		this.dateValue.saved();
		this.dateTimeValue.saved();
		this.formatsValue.saved();
        this.versionValue.saved();
		this.reportsValue.saved();
		this.appEntriesValue.saved();
		this.clientEntriesValue.saved();
		this.serviceEntriesValue.saved();
		this.sqlEntriesValue.saved();
		this.importsValue.saved();
		this.varsValue.saved();
		this.userVarsValue.saved();
		this.matricesValue.saved();
		this.appDictionariesValue.saved();
		this.clientDictionariesValue.saved();
		this.librariesValue.saved();
	}

	//------------------------------------------------------------------------------------------------------------------
    public void forEachFile(BiConsumer<File, DocumentKind> applier, DocumentKind... kinds)
    {
        for (DocumentKind kind : kinds)
        {
            switch (kind)
            {
            case CONFIGURATION:
                applier.accept(new File(this.getName()), kind);
                break;
                
            case SYSTEM_VARS:
                this.systemVars.stream().map(v -> new File(v.getName())).forEach(file -> applier.accept(file, kind));
                break;
                
            case GUI_DICTIONARY:
                this.appDictionariesValue.forEach(ms ->
                {
                    File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
                    applyToAllFile(folderFile, applier, kind);
                });
                break;
                
            case MESSAGE_DICIONARY:
                this.clientDictionariesValue.forEach(ms ->
                {
                    File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
                    applyToAllFile(folderFile, applier, kind);
                });
                break;
                
            case LIBRARY:
                this.libs.values().stream().map(v -> new File(v.getName())).forEach(file -> applier.accept(file, kind));
                break;
                
            case MATRIX:
                this.matricesValue.forEach(ms -> 
                {
                    File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
                    applyToAllFile(folderFile, applier, kind);
                });
                break;

            case PLAIN_TEXT:
            	applyToAllFile(new File("."), applier, kind);
            	break;
            default:

            }
        }
        
    }

	
	
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
				this.appDictionariesValue.forEach(ms ->
				{
					File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
					applyToAll(folderFile, kind, consoleFactory, applier);
				});
				break;
				
			case MESSAGE_DICIONARY:
				this.clientDictionariesValue.forEach(ms ->
				{
					File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
					applyToAll(folderFile, kind, consoleFactory, applier);
				});
				break;
				
			case LIBRARY:
				this.libs.values().forEach(applier);
				break;
				
			case MATRIX:
				this.matricesValue.forEach(ms -> 
				{
					File folderFile = new File(MainRunner.makeDirWithSubstitutions(ms.get()));
					applyToAll(folderFile, kind, consoleFactory, applier);
				});
				break;

			default:

			}
		}
	}
	
	public SqlEntry getSqlEntry(String name) throws Exception
	{
		return getEntry(name, this.sqlEntriesValue);
	}

	public List<SqlEntry> getSqlEntries()
	{
		return this.sqlEntriesValue;
	}

	public ClientEntry getClientEntry(String name) throws Exception
	{
		return getEntry(name, this.clientEntriesValue);
	}

	public List<ClientEntry> getClientEntries()
	{
		return this.clientEntriesValue;
	}

	public ServiceEntry getServiceEntry(String name) throws Exception
	{
		return getEntry(name, this.serviceEntriesValue);
	}

	public List<ServiceEntry> getServiceEntries()
	{
		return this.serviceEntriesValue;
	}

	public AppEntry getAppEntry(String name) throws Exception
	{
		return getEntry(name, this.appEntriesValue);
	}


	public RunnerListener getRunnerListener()
	{
		return this.runnerListener;
	}

	public List<AppEntry> getAppEntries()
	{
		return this.appEntriesValue;
	}

	public final ReportFactory getReportFactory()
	{
		return this.reportFactoryObj;
	}

	public final Collection<String> getClients() throws Exception
	{
		return this.clientEntriesValue.stream().map(entry -> entry.toString()).collect(Collectors.toList());
	}

	public final Collection<String> getServices() throws Exception
	{
		return this.serviceEntriesValue.stream().map(entry -> entry.toString()).collect(Collectors.toList());
	}

	public final Collection<String> getApplications() throws Exception
	{
		return this.appEntriesValue.stream().map(entry -> entry.toString()).collect(Collectors.toList());
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

	public final void register(Document doc)
	{
		synchronized (this.subordinates)
		{
			this.subordinates.add(doc);
		}
	}

	public final void unregister(Document doc)
	{
		synchronized (this.subordinates)
		{
			this.subordinates.remove(doc);
		}
	}

	public Map<String, Object> getStoreMap()
	{
		return new LinkedHashMap<>(this.globals);
	}

	public void storeMap(Map<String, Object> map)
	{
		this.globals = map;
	}

	public static List<String> toStringList(MutableArrayList<MutableString> str)
	{
		return str.stream().map(a -> MainRunner.makeDirWithSubstitutions(a.get())).collect(Collectors.toList());
	}

	public List<Document> getSubordinates()
	{
		return subordinates;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Entry> T getEntry(String name, List<T> entries) throws Exception
	{
		if (entries == null)
		{
			return null;
		}
		for (Entry entry : entries)
		{
			if (entry.toString().equals(name))
			{
				return (T)entry;
			}
		}
		return null;
	}



	@SuppressWarnings("unchecked")
	private <T> T objectFromClassName(String name, Class<T> baseType) 	throws Exception
	{
		try
		{
			Class<?> type = null;
			try
			{
				type = Class.forName(name);
			}
			catch (ClassNotFoundException e)
			{
				type = Class.forName(baseType.getPackage().getName() + "." + name);
			}

			if (!baseType.isAssignableFrom(type))
			{
				throw new Exception("class '" + name + "' is not assignable from " + baseType.getName());
			}

			return (T)type.newInstance();
		}
		catch (Exception e)
		{
			logger.error(String.format("objectFromClassName(%s, %s)", name, baseType));
			throw e;
		}
	}

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
				SystemVars vars = getFactory().createVars(userVariablesFileName);
				vars.load(reader);
				this.systemVars.add(vars);
			}
		}
	}

    private void setAll(Configuration config)
	{
		this.timeValue.set(config.timeValue);
		this.dateValue.set(config.dateValue);
		this.dateTimeValue.set(config.dateTimeValue);
		this.formatsValue.from(config.formatsValue);
        this.versionValue.set(config.versionValue);
		this.globalHandlerValue.setValue(config.globalHandlerValue);
		this.reportsValue.set(config.reportsValue);
		this.appEntriesValue.from(config.appEntriesValue);
		this.clientEntriesValue.from(config.clientEntriesValue);
		this.serviceEntriesValue.from(config.serviceEntriesValue);
		this.sqlEntriesValue.from(config.sqlEntriesValue);
		this.importsValue.from(config.importsValue);
		this.varsValue.set(config.varsValue);
		this.userVarsValue.from(config.userVarsValue);
		this.matricesValue.from(config.matricesValue);
		this.appDictionariesValue.from(config.appDictionariesValue);
		this.clientDictionariesValue.from(config.clientDictionariesValue);
		this.librariesValue.from(config.librariesValue);

		this.changed = false;
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
		} else
		{
			try
			{
				Document doc = factory.createDocument(kind, Common.getRelativePath(path.getAbsolutePath()));
                try (Reader reader = CommonHelper.readerFromFileName(doc.getName()))
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
        if (path.isDirectory())
        {
            File[] files = path.listFiles();
            if (files != null)
            {
                Arrays.stream(files).forEach(file -> applyToAllFile(file, applier, kind));
            }
        } else
        {
            applier.accept(path, kind);
        }
    }

	private static final Class<?>[] jaxbContextClasses =
		{
			Configuration.class,
			SqlEntry.class,
			ClientEntry.class,
			ServiceEntry.class,
			AppEntry.class,
			GlobalHandler.class,
		};

	protected String 				reportFactoryValue = HTMLReportFactory.class.getSimpleName();
	protected String 				evaluatorValue = MvelEvaluator.class.getSimpleName();
	protected Map<File, Long> 		timestampMap 	= new HashMap<>();
	protected RunnerListener 		runnerListener 	= new DummyRunnerListener();
	protected boolean 				changed;
	protected ReportFactory			reportFactoryObj;
	protected Map<String, Matrix>	libs;
	protected Map<String, Date>		documentsActuality;
	protected Map<String, Object>	globals;
	protected Set<SystemVars>		systemVars;

	protected ClientsPool			clients;
	protected ServicePool			services;
	protected ApplicationPool		applications;
	protected DataBasePool			databases;
	protected Date                  lastUpdate = new Date();

	protected final List<Document> 	subordinates = new ArrayList<>();

	protected boolean valid = false;

	private static final Logger logger = Logger.getLogger(Configuration.class);

}