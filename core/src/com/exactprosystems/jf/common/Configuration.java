////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.client.ClientsPool;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.MvelEvaluator;
import com.exactprosystems.jf.common.evaluator.SystemVars;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.MatrixRoot;
import com.exactprosystems.jf.common.parser.items.MutableArrayList;
import com.exactprosystems.jf.common.parser.items.SubCase;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.parser.listeners.MatrixListener;
import com.exactprosystems.jf.common.report.HTMLReportFactory;
import com.exactprosystems.jf.common.report.ReportFactory;
import com.exactprosystems.jf.common.xml.schema.Xsd;
import com.exactprosystems.jf.service.ServicePool;
import com.exactprosystems.jf.sql.DataBasePool;
import com.exactprosystems.jf.tool.AbstractDocument;
import com.exactprosystems.jf.tool.main.DocumentKind;
import com.exactprosystems.jf.tool.main.Main;

import org.apache.log4j.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)

@DocumentInfo(
		newName = "NewConfiguration", 
		extentioin = "xml", 
		description = "Jackfish configuration"
)
public class Configuration extends AbstractDocument
{
	public static final String 	projectName 		= "JackFish";
	public static final String 	matrixExt 			= ".jf";
	public static final String 	matrixFilter		= "*.jf";
	public static final char  	matrixDelimiter		= ';';
	public static final String 	unicodeDelimiter	= String.valueOf("\\\\u" + Integer.toHexString(Configuration.matrixDelimiter | 0x10000).substring(1));


	public static final String timeFormat			= "timeFormat";
	public static final String dateFormat			= "dateFormat";
	public static final String dateTimeFormat		= "dateTimeFormat";
	public static final String additionFormats		= "additionFormats";
	public static final String evaluatorImports 	= "evaluatorImports";
	public static final String outputPath 			= "outputPath";
	public static final String variables 			= "variables";
	public static final String userVariables 		= "userVariables";

	public static final String entryName			= "name";
	
	public static final String libEntry				= "libEntry";
	public static final String libPath				= "libPath";

	public static final String sqlEntry				= "sqlEntry";
	public static final String sqlJar				= "sqlJar";
	public static final String sqlConnection 		= "sqlConnection";
	
	public static final String clientEntry			= "clientEntry";
	public static final String clientDescription 	= "clientDescription";
	public static final String clientJar				= "clientJar";
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

	@XmlElement(name = timeFormat)
	protected String timeFormatValue;
	
	@XmlElement(name = dateFormat)
	protected String dateFormatValue;
	
	@XmlElement(name = dateTimeFormat)
	protected String dateTimeFormatValue;
	
	@XmlElement(name = additionFormats)
	protected String additionFormatsValue;
	
	private String reportFactoryValue = HTMLReportFactory.class.getSimpleName();
	
	private String evaluatorValue = MvelEvaluator.class.getSimpleName();
	
	@XmlElement(name = evaluatorImports)
	protected String evaluatorImportsValue;
	
	@XmlElement(name = outputPath)
	protected String outputPathValue;
	
	@XmlElement(name = variables)
	protected String variablesValue;
	
	@XmlElement(name = userVariables)
	protected String userVariablesValue;
	
	//------------------------------------------------------------------------------------------------------------------
	// new technology
	//------------------------------------------------------------------------------------------------------------------
	
	@XmlAccessorType(XmlAccessType.NONE)
	public static abstract class Entry implements Mutable
	{
		@XmlElement(name = entryName)
		protected String entryNameValue;

		@XmlElement(name = parametersEntry)
		protected MutableArrayList<Parameter> parameters;

		@Override
		public String toString()
		{
			return this.entryNameValue == null ? "" : this.entryNameValue;
		}

		@Override
		public boolean isChanged()
		{
			if (this.changed)
			{
				return true;
			}
			if (this.parameters != null)
			{
				if (this.parameters.isChanged())
				{
					return true;
				}
				for (Parameter parameter : this.parameters)
				{
					if (parameter.isChanged())
					{
						return true;
					}
				}
			}

			return false;
		}

		@Override
		public void saved()
		{
			if (this.parameters != null)
			{
				this.parameters.saved();
				for (Parameter parameter : this.parameters)
				{
					parameter.saved();
				}
			}
			changed = false;
		}
		
		public String get(String name) throws Exception
		{
			Object res = Configuration.get(getClass(), this, name);
			return res == null ? "" : res.toString();
		}
		
		public void set(String name, Object value) throws Exception
		{
			Configuration.set(getClass(), this, name, value);
			changed = true;
		}

		public String getParameter(String key)
		{
			if (this.parameters != null)
			{
				for (Parameter param : this.parameters)
				{
					if (param.key.equals(key))
					{
						return param.value;
					}
				}
			}
			
			return null;
		}

		public List<Parameter> getParameters()
		{
			if (this.parameters == null)
			{
				this.parameters = new MutableArrayList<Parameter>();
			}
			return this.parameters;
		}
		
		private boolean changed = false;
	}

	@XmlAccessorType(XmlAccessType.NONE)
	public static class Parameter implements Mutable
	{
		@XmlElement(name = parametersKey)
		protected String key;
		
		@XmlElement(name = parametersValue)
		protected String value;

        @Override
        public String toString()
        {
            return Parameter.class.getSimpleName() + "{" + parametersKey + "=" + key + " " + parametersValue + "=" + value + "}";
        }

		@Override
		public boolean isChanged()
		{
			return this.changed;
		}

		@Override
		public void saved()
		{
			this.changed = false;
		}

        public String get(String name) throws Exception
		{
			Object res = Configuration.get(Parameter.class, this, name);
			return res == null ? "" : res.toString();
		}

		public void set(String name, Object value) throws Exception
		{
			Configuration.set(Parameter.class, this, name, value);
			changed = true;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Parameter parameter = (Parameter) o;

			if (key != null ? !key.equals(parameter.key) : parameter.key != null)
				return false;

			return true;
		}

		@Override
		public int hashCode()
		{
			return key != null ? key.hashCode() : 0;
		}

		private boolean changed = false;

		//this getters / setters need to tableView. Not Delete this methods in future
		public void setKey(String key)
		{
			this.key = key;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		public String getKey()
		{
			return this.key;
		}

		public String getValue()
		{
			return this.value;
		}

	}

	
	@XmlElement(name = libEntry)
	public MutableArrayList<LibEntry> libEntriesValue;

	@XmlAccessorType(XmlAccessType.NONE)
	public static class LibEntry extends Entry
	{
		@XmlElement(name = libPath)
		protected String libPathValue;
	}

	
	@XmlElement(name = sqlEntry)
	public MutableArrayList<SqlEntry> sqlEntriesValue;

	@XmlAccessorType(XmlAccessType.NONE)
	public static class SqlEntry extends Entry
	{
		@XmlElement(name = sqlJar)
		protected String sqlJarNameValue;

		@XmlElement(name = sqlConnection)
		protected String sqlConnectionStringValue;
	}

	@XmlElement(name = clientEntry)
	public MutableArrayList<ClientEntry> clientEntriesValue;

	@XmlAccessorType(XmlAccessType.NONE)
	public static class ClientEntry extends Entry
	{
		@XmlElement(name = clientDescription)
		protected String descriptionValue;
		
		@XmlElement(name = clientJar)
		protected String clientJarNameValue;

		@XmlElement(name = clientLimit)
		protected int clientLimitValue;

		@XmlElement(name = clientDictionary)
		protected String clientDictionaryValue;

	}
	
	@XmlElement(name = serviceEntry)
	public MutableArrayList<ServiceEntry> serviceEntriesValue;

	@XmlAccessorType(XmlAccessType.NONE)
	public static class ServiceEntry extends Entry
	{
		@XmlElement(name = serviceDescription)
		protected String descriptionValue;
		
		@XmlElement(name = serviceJar)
		protected String serviceJarNameValue;
	}

	@XmlElement(name = appEntry)
	public MutableArrayList<AppEntry> appEntriesValue;

	@XmlAccessorType(XmlAccessType.NONE)
	public static class AppEntry extends Entry
	{
		@XmlElement(name = appDescription)
		protected String descriptionValue;
		
		@XmlElement(name = appDicPath)
		protected String appDicPathValue;
		
		@XmlElement(name = appJar)
		protected String appJarNameValue;

		@XmlElement(name = appWorkDir)
		protected String appWorkDirValue;
		
		@XmlElement(name = appStartPort)
		protected String appStartPortValue;
	}
	

	public Configuration(String fileName, UpdateLibsListener listener, Settings settings)
	{
		super(fileName);

		this.listener 					= listener;
		this.settings 					= settings;
		this.changed 					= false;

		this.libEntriesValue			= new MutableArrayList<LibEntry>();
		this.sqlEntriesValue 			= new MutableArrayList<SqlEntry>();
		this.clientEntriesValue			= new MutableArrayList<ClientEntry>();
		this.serviceEntriesValue		= new MutableArrayList<ServiceEntry>();
		this.appEntriesValue 			= new MutableArrayList<AppEntry>();
		
		this.globals 					= new HashMap<String, Object>();
		
		this.clients 					= new ClientsPool(this);
		this.services 					= new ServicePool(this);
		this.applications 				= new ApplicationPool(this);
		this.databases 					= new DataBasePool(this);

		this.libs 						= new HashMap<String, Matrix>();
	}

	public Configuration()
	{
		this("unknown", new ConsoleUpdateLibsListener(), new Settings());
	}

	public Settings getSettings()
	{
		return this.settings;
	}
	
	public void storeGlobal(String name, Object value)
	{
		synchronized (this.globals)
		{
			this.globals.put(name, value);
		}
	}

	public Object restoreGlobal(String name)
	{
		synchronized (this.globals)
		{
			return this.globals.get(name);
		}
	}
	
	public void updateLibs()
	{
		this.libs.clear();
		Map<File, Long> newMap = new HashMap<File, Long>();
		for (LibEntry lib : this.libEntriesValue)
		{
			try
			{
				String name = lib.toString();
				String path = lib.get(libPath);
				IMatrixListener checker = new MatrixListener();
				Matrix matrix = new Matrix(path, checker);
				File file = new File(path);
				Long timestamp = timestampMap.get(file);
				newMap.put(file, timestamp);
				if (timestamp == null)
				{
					timestampMap.put(file, file.lastModified());
					this.libs.put(name, loadMatrix(file, matrix, checker, name));
				}
				else
				{
					if (!timestamp.equals(file.lastModified()))
					{
						this.libs.put(name, loadMatrix(file, matrix, checker, name));
					}
					else
					{
						this.libs.put(name, matrix);
					}

				}
			}
			catch (Exception e)
			{
				String message = "Error on update lib : " + lib + " .\n" + e.getMessage();
				logger.error(message);
				logger.error(e.getMessage(), e);
				listener.onException(message);
			}
		}
		timestampMap.clear();
		timestampMap.putAll(newMap);
	}

	private Matrix loadMatrix(File file, Matrix matrix, IMatrixListener checker, String name) throws Exception
	{
		try (Reader libReader = new FileReader(file))
		{
			matrix.load(libReader);
		}
		if (!checker.isOk())
		{
			logger.error(checker.getExceptionMessage());
			throw new Exception("Library '" + name + "' is invalid. See the log.");
		}
		return matrix;
	}


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
	        unmarshaller.setEventHandler(new ValidationEventHandler()
	        {
	            @Override
	            public boolean handleEvent(ValidationEvent event)
	            {
	                System.out.println("Error in configuration : " + event);
	
	                return false;
	            }
	        });
	
	        Configuration config = (Configuration) unmarshaller.unmarshal(reader);
	        setAll(config);
	        
			this.reportFactoryObj		= objectFromClassName(reportFactoryValue, ReportFactory.class);
			DateTime.SetFormats(get(timeFormat), 
					get(dateFormat), 
					get(dateTimeFormat));
	
			Converter.setFormats(get(additionFormats));
	
			updateLibs();
	
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
    public void close() throws Exception 
    {
    	super.close();
    	
    	Set<Document> copy = null;
		synchronized (this.subordinates)
		{
	    	copy = new HashSet<Document>();
	    	copy.addAll(this.subordinates);
		}
		
		// save list of all opened documents ...
		this.settings.removeAll(Main.MAIN_NS, Main.OPENED);
		this.settings.saveIfNeeded();

		for (Document doc : copy)
		{
			try
			{
				DocumentKind kind = DocumentKind.byDocument(doc);
				if (doc.hasName())
				{
					this.settings.setValue(Main.MAIN_NS, Main.OPENED, doc.getName(), kind.toString());
				}
				doc.close();
			}
			catch (Exception e)
			{
				logger.error(e.getMessage());
			}
		}
		this.settings.saveIfNeeded();

		this.services.stopAllServices();
		this.applications.stopAllApplications();
    }
    
    @Override
    public void save(String fileName) throws Exception
    {
    	super.save(fileName);
    	
        try(OutputStream os = new FileOutputStream(new File(fileName)))
        {
            File file = new File(fileName);

            JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, file);
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

		return this.libEntriesValue.isChanged()
				|| this.sqlEntriesValue.isChanged()
				|| this.clientEntriesValue.isChanged()
				|| this.serviceEntriesValue.isChanged()
				|| this.appEntriesValue.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		
		this.changed = false;
		this.libEntriesValue.saved();
		this.appEntriesValue.saved();
		this.clientEntriesValue.saved();
		this.serviceEntriesValue.saved();
		this.sqlEntriesValue.saved();
	}

	//------------------------------------------------------------------------------------------------------------------
	public final AbstractEvaluator createEvaluator() throws Exception
	{
		String evaluatorClassName = evaluatorValue;
		if (Str.IsNullOrEmpty(evaluatorClassName))
		{
			throw new Exception("Empty evaluator class name.");
		}
		
		AbstractEvaluator evaluatorObj	= objectFromClassName(evaluatorClassName, AbstractEvaluator.class);
		evaluatorObj.addImports(get(evaluatorImports).split(","));
		setUserVariablesFromMask(get(variables), evaluatorObj);
		setUserVariablesFromMask(get(userVariables), evaluatorObj);

		return evaluatorObj;
	}

	public SubCase referenceToSubcase(String name, MatrixItem item)
	{
		MatrixItem ref = item.findParent(MatrixRoot.class).find(true, SubCase.class, name);

		if (ref != null && ref instanceof SubCase)
		{
			return (SubCase) ref;
		}
		if (name == null)
		{
			return null;
		}
		String[] parts = name.split("\\.");
		if (parts.length < 2)
		{
			return null;
		}
		String ns = parts[0];
		String id = parts[1];

		Matrix matrix = this.libs.get(ns);
		if (matrix == null)
		{
			matrix = getLib(ns);

			if (matrix == null)
			{
				return null;
			}
			try
			{
				matrix = matrix.clone();
			}
			catch (CloneNotSupportedException e)
			{
				logger.error(e.getMessage(), e);
			}
			this.libs.put(ns, matrix);
		}

		return (SubCase) matrix.getRoot().find(true, SubCase.class, id);
	}


	
	public String get(String name) throws Exception
	{
		Object res = get(Configuration.class, this, name);
		return res == null ? "" : res.toString();
	}
	
	public void set(String name, Object value) throws Exception
	{
		set(Configuration.class, this, name, value);
		this.changed = true;
	}
	

	public LibEntry getLibEntry(String name) throws Exception
	{
		return getEntry(name, this.libEntriesValue);
	}

	public List<LibEntry> getLibEntries()
	{
		return this.libEntriesValue;
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

	private void setUserVariablesFromMask(String userVariablesFileName, AbstractEvaluator evaluator)  throws Exception
	{
		if (Str.IsNullOrEmpty(userVariablesFileName))
		{
			return;
		}
		
		final File file = new File(userVariablesFileName);
		if (file.exists())
		{
			try (Reader reader = new FileReader(file))
			{
				SystemVars vars = new SystemVars(userVariablesFileName);
				vars.load(reader);
				vars.injectVariables(evaluator);
			}
		}
		else
		{
			final Path path = Paths.get(userVariablesFileName);
			
			String reg = path.getFileName().toString();
			reg = reg.replace("?", ".{1}");
			reg = reg.replace(".", "\\.");
			reg = reg.replace("*", ".*");
			final String regex = reg;
			
			File dir = path.subpath(0, path.getNameCount() - 1).toFile();
			File[] files = dir.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					return pathname.getName().matches(regex);
				}
			});
			 
			for(File one : files)
			{
				try (Reader reader = new FileReader(one))
				{
					SystemVars vars = new SystemVars(one.getPath());
					vars.load(reader);
					vars.injectVariables(evaluator);
				}
			}
		}
	}

	private static Object get(Class<?> clazz, Object object, String name) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields)
		{
			XmlElement attr = field.getAnnotation(XmlElement.class);
			if (attr == null)
			{
				continue;
			}
			if (attr.name().equals(name))
			{
				return field.get(object);
			}
		}
		
		if (clazz.getSuperclass() != null )
		{
			return get(clazz.getSuperclass(), object, name);
		}
		
		return null;
	}
    
	private static void set(Class<?> clazz, Object object, String name, Object value) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields)
		{
			XmlElement attr = field.getAnnotation(XmlElement.class);
			if (attr == null)
			{
				continue;
			}
			if (attr.name().equals(name))
			{
				field.set(object, value);
				return;
			}
		}

		if (clazz.getSuperclass() != null )
		{
			set(clazz.getSuperclass(), object, name, value);
		}
	}
	
    private void setAll(Configuration config)
	{
		Field[] fields = Configuration.class.getDeclaredFields();
		
		for (Field field : fields)
		{
			XmlElement attr = field.getAnnotation(XmlElement.class);
			if (attr == null)
			{
				continue;
			}
			Object value = null;
			try
			{
				value = field.get(config);
				set(attr.name(), value);
			}
			catch (Exception e)
			{
				logger.error("name = " + attr.name() + "  value = " + value);
				logger.error(e.getMessage(), e);
			}
		}
		this.changed = false;
	}

	private static final Class<?>[] jaxbContextClasses = 
		{ 
			Configuration.class,
			LibEntry.class,
			SqlEntry.class,
			ClientEntry.class,
			ServiceEntry.class,
			AppEntry.class,
		};

	protected Map<File, Long> timestampMap = new HashMap<>();
	protected UpdateLibsListener listener;
	protected boolean changed;
	protected ReportFactory	reportFactoryObj;
	protected Map<String, Matrix>	libs;
	protected Map<String, Object>	globals;
	protected Settings settings;

	protected ClientsPool			clients;
	protected ServicePool			services;
	protected ApplicationPool		applications;
	protected DataBasePool			databases;

	protected final Set<Document> subordinates = new HashSet<Document>();
	
	protected boolean valid = false;

	private static final Logger logger = Logger.getLogger(Configuration.class);

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
}