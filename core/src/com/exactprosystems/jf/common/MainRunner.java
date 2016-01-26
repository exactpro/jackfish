////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.api.common.ApiVersionInfo;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.parser.listeners.*;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.common.xml.gui.GuiDictionary;
import com.exactprosystems.jf.common.xml.messages.MessageDictionary;
import com.exactprosystems.jf.tool.main.Main;

import javafx.application.Application;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

public class MainRunner
{
	@SuppressWarnings("static-access")
	public static void main(String[] args)
	{
		int exitCode = 0;
		try
		{
			logger.info("Tool version: " + VersionInfo.getVersion());
			logger.info("API version:  " + ApiVersionInfo.majorVersion() + "." + ApiVersionInfo.minorVersion());
			logger.debug("args: " + Arrays.toString(args));
			
			Option startAtName = OptionBuilder
					.withArgName("time")
					.hasArg()
					.withDescription("Specify start matrix time.")
					.create("time");

			Option inputName = OptionBuilder
					.withArgName("file")
					.hasArg()
					.withDescription("Specify input path name.")
					.create("input");

			Option outputName = OptionBuilder
					.withArgName("file")
					.hasArg()
					.withDescription("Specify output path name.")
					.create("output");

			Option configName = OptionBuilder
					.withArgName("file")
					.hasArg()
					.withDescription("Specify configuration file name.")
					.create("config");

			Option traceLevel = OptionBuilder
					.withArgName("level")
					.hasArg()
					.withDescription("Specify verbose level. " + Arrays.toString(VerboseLevel.values()))
					.create("verbose");

			Option saveSchema 	= new Option("schema", 	"Save the config schema." );
			Option help 		= new Option("help", 	"Print this message." );
			Option versionOut 	= new Option("version", "Print version only.");
			Option shortPaths 	= new Option("short", 	"Show only short paths in tracing." );
            Option gui          = new Option("gui",     "Open GUI. Deprecated. It is behavior by default.");
            Option console      = new Option("console",	"Do not open GUI. Batch mode.");
			
			Options options = new Options();

			options.addOption(startAtName);
			options.addOption(inputName);
			options.addOption(outputName);
			options.addOption(configName);
			options.addOption(traceLevel);
			options.addOption(versionOut);
			options.addOption(saveSchema);
			options.addOption(help);
			options.addOption(shortPaths);
            options.addOption(gui);
            options.addOption(console);

			
			CommandLineParser parser = new GnuParser();
			
			CommandLine line = null;
			
		    try 
		    {
		        // parse the command line arguments
		        line = parser.parse( options, args );
		    }
		    catch( ParseException exp ) 
		    {
		        // oops, something went wrong
		        System.out.println( "Incorrect parameters: " + exp.getMessage() );
		        
		        printHelp(options);
		        
		        System.exit(1);
		        return;
		    }

			String configString = line.getOptionValue(configName.getOpt());

			if (!line.hasOption(console.getOpt()))
			{
				String[] guiArgs = null;
				if (configString != null)
				{
					guiArgs = new String[]{configString};
				}
				else
				{
					guiArgs = new String[]{};
				}

				Application.launch(Main.class, guiArgs);
				return;
			}

            if (line.hasOption(saveSchema.getOpt()))
		    {
				saveSchema(Configuration.class, 		"schema_conf.xsd");
				saveSchema(MessageDictionary.class, 	"schema_mess.xsd");
				saveSchema(GuiDictionary.class, 		"schema_gui.xsd");
				
		    	System.exit(0);
		    }

		    if (line.hasOption(versionOut.getOpt()))
		    {
				printVersion();
				
		    	System.exit(0);
		    }
		    
		    if (line.hasOption(help.getOpt()))
		    {
		    	printHelp(options);
		    	
		    	System.exit(0);
		    }
		    
			String verboseString = line.getOptionValue(traceLevel.getOpt());
			VerboseLevel verboseLevel = VerboseLevel.All;
			if (verboseString != null)
			{
				verboseLevel = VerboseLevel.valueOf(verboseString);
			}

			boolean showShortPaths = line.hasOption(shortPaths.getOpt()); 
			
			printVersion();

			Configuration configuration = new Configuration(configString, new Settings());
			if (!Str.IsNullOrEmpty(configString))
			{
		    	try (BufferedReader reader = new BufferedReader(new FileReader(configString)))
		        {
		    		configuration.load(reader);
				}
				if (!configuration.isValid())
				{
					System.out.println("Configuration is invalid! See the logs for details.");
					System.exit(2);
				}
			}
			
			if (!line.hasOption(inputName.getOpt()))
			{
                System.out.println(String.format("Error: need %s parameter.", inputName.getOpt()));
                return;
			}
			
			String input = line.getOptionValue(inputName.getOpt());
			String outputString = line.getOptionValue(outputName.getOpt());
			if (outputString != null)
			{
				configuration.set(Configuration.outputPath, outputString);
			}

			Date startAt = new Date();
			String timeString = line.getOptionValue(startAtName.getOpt());
			if (timeString != null)
			{
				startAt = DateTime.date(timeString);
			}
			
			boolean error = false;

			File inputFile = new File(input); 
			if (!inputFile.exists())
			{
                System.out.println(String.format("Error: input file %s does not exist", input));
				error = true;
			}
			
			File outputDir = new File(configuration.get(Configuration.outputPath)); 
			if (!outputDir.exists())
			{
                System.out.println(String.format("Error: output directory %s does not exist", outputDir.getPath()));
				error = true;
			}

			if (error)
			{
				return;
			}

			processMatrix(configuration, inputFile, startAt, verboseLevel, showShortPaths);
		} 
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			System.out.println("Error: " + e.getMessage());
			exitCode = 1;
		}
		finally
		{
		}

	
		System.exit(exitCode);
	}

	private static void printHelp(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(Configuration.projectName, options );
	}

	private static void printVersion()
	{
		System.out.println(Configuration.projectName + "  ver." + VersionInfo.getVersion());
		System.out.println("API ver. " + ApiVersionInfo.majorVersion() + "." + ApiVersionInfo.minorVersion());
	}

	private static void processMatrix(Configuration configuration, File matrix,  
			Date startAt, VerboseLevel verboseLevel, boolean showShortPaths)
	{
		try
		{
			IMatrixListener matrixListener 	= null;
			RunnerListener runnerListener = new DummyRunnerListener();
			switch (verboseLevel)
			{
				case None:
					matrixListener 	= new MatrixListener();
					break;
				case Errors:
					matrixListener 	= new ConsoleErrorMatrixListener();
					break;
				case All:
					matrixListener 	= new ConsoleMatrixListener(showShortPaths);
					break;
			}
			
			logger.info(String.format("Processing '%s' start at '%s'", matrix.getName(), startAt.toString()));

			try(Context context = configuration.createContext(matrixListener, System.out);
				MatrixRunner runner = new MatrixRunner(context, matrix, startAt, null))
			{
				runner.start();
				runner.join(0);
			}
			catch (Exception e)
			{
				System.out.println(String.format("Error in matrix '%s' : %s", matrix.getName(), e.getMessage()));
				logger.error(e.getMessage(), e);
			}
		} 
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			logger.error(e.getMessage(), e);
		}

		System.out.println(MainRunner.class.getSimpleName() + " finished");
	}
	
	private static void saveSchema(Class<?> clazz, final String fileName)
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { clazz});
			
			SchemaOutputResolver sor = new SchemaOutputResolver()
			{
				
				@Override
				public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException
				{
					System.out.println(fileName);
					
					File file = new File(fileName);
			        
					StreamResult result = new StreamResult(file);
			        result.setSystemId(file.toURI().toURL().toString());
			        return result;
				}
			}; 
			
			
			try
			{
				jaxbContext.generateSchema(sor);
			} 
			catch (IOException e)
			{
				logger.error(e.getMessage(), e);
			}
		} 
		catch (JAXBException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	
	private static final Logger logger = Logger.getLogger(MainRunner.class);

	static String logFileName = ".log.xml";
	static
	{
		if (!new File(logFileName).exists())
		{
			try (	BufferedReader reader = new BufferedReader(new InputStreamReader(MainRunner.class.getResourceAsStream(logFileName)));
					BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName)))
			{
				String line = null;

				while ((line = reader.readLine()) != null)
				{
					writer.append(line);
					writer.newLine();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}

		DOMConfigurator.configure(logFileName);
	}
	
}
