package net.sf.testium.plugins;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.PropertyConfigurator;
import net.sf.testium.Testium;
import net.sf.testium.configuration.ConfigurationException;
import net.sf.testium.configuration.SoapuiConfiguration;
import net.sf.testium.configuration.SoapuiConfigurationXmlHandler;
import net.sf.testium.executor.TestCaseSoapuiExecutor;
import org.testtoolinterfaces.testresultinterface.TestCaseResultWriter;
import org.testtoolinterfaces.utils.RunTimeData;
import org.testtoolinterfaces.utils.Trace;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Arjan Kranenburg
 *
 */
public final class SoapuiPlugin implements Plugin
{
	public SoapuiPlugin()
	{
		// nop
	}
	
	public void loadPlugIn(PluginCollection aPluginCollection,
			RunTimeData aRtData) throws ConfigurationException
	{
		File pluginsDir = aRtData.getValueAsFile(Testium.PLUGINSDIR);
		File soapUILibs = new File( pluginsDir, "SoapUILibs" );
		try
		{
			PluginClassLoader.addDirToClassLoader( soapUILibs );
		}
		catch (MalformedURLException e)
		{
			throw new ConfigurationException( e );
		}

		SoapuiConfiguration config = readConfigFiles( aRtData );
		
		File log4jFile = config.getLog4jFile();
		PropertyConfigurator.configure( log4jFile.getAbsolutePath() );

		// Executors
		TestCaseResultWriter tcResultWriter = aPluginCollection.getTestCaseResultWriter();
		TestCaseSoapuiExecutor tcSoapuiExecutor = new TestCaseSoapuiExecutor( config, tcResultWriter );
		aPluginCollection.addTestCaseExecutor( tcSoapuiExecutor );
	}
	
	public SoapuiConfiguration readConfigFiles( RunTimeData anRtData ) throws ConfigurationException
	{
		Trace.println(Trace.UTIL);

		File configDir = (File) anRtData.getValue(Testium.CONFIGDIR);
		File configFile = new File( configDir, "soapUI.xml" );
		SoapuiConfiguration globalConfig = readConfigFile( configFile, anRtData );
		
		File userConfigDir = (File) anRtData.getValue(Testium.USERCONFIGDIR);
		File userConfigFile = new File( userConfigDir, "soapUI.xml" );
		SoapuiConfiguration userConfig = new SoapuiConfiguration( null, null, null, null);
		if ( userConfigFile.exists() )
		{
			userConfig = readConfigFile( userConfigFile, anRtData );
		}

		File project = userConfig.getProject();
		if (project == null)
		{
			project = globalConfig.getProject();
		}

		String soapInterface = userConfig.getSoapInterface();
		if (soapInterface == null || soapInterface.isEmpty() )
		{
			soapInterface = globalConfig.getSoapInterface();
		}
		
		File soapUILibsDir = userConfig.getSoapUILibsDir();
		if ( soapUILibsDir == null ||
			 soapUILibsDir.equals( SoapuiConfigurationXmlHandler.getDefaultSoapUILibsDir(anRtData) ) )
		{
			soapUILibsDir = globalConfig.getSoapUILibsDir();
		}
		
		File log4jFile = userConfig.getLog4jFile();
		if ( log4jFile == null ||
				log4jFile.equals( SoapuiConfigurationXmlHandler.getDefaultLog4jFile(anRtData) ) )
		{
			log4jFile = globalConfig.getLog4jFile();
		}
		
		SoapuiConfiguration config = new SoapuiConfiguration( project,
		                                                      soapInterface,
		                                                      soapUILibsDir,
		                                                      log4jFile );

		return config;
	}
	
	public SoapuiConfiguration readConfigFile( File aConfigFile, RunTimeData aRtData ) throws ConfigurationException
	{
		Trace.println(Trace.UTIL, "readConfigFile( " + aConfigFile.getName() + " )", true );
        // create a parser
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(false);
        SAXParser saxParser;
        SoapuiConfigurationXmlHandler handler = null;
		try
		{
			saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();

	        // create a handler
			handler = new SoapuiConfigurationXmlHandler(xmlReader, aRtData);

	        // assign the handler to the parser
	        xmlReader.setContentHandler(handler);

	        // parse the document
	        xmlReader.parse( aConfigFile.getAbsolutePath() );
		}
		catch (ParserConfigurationException e)
		{
			Trace.print(Trace.UTIL, e);
			throw new ConfigurationException( e );
		}
		catch (SAXException e)
		{
			Trace.print(Trace.UTIL, e);
			throw new ConfigurationException( e );
		}
		catch (IOException e)
		{
			Trace.print(Trace.UTIL, e);
			throw new ConfigurationException( e );
		}
		
		SoapuiConfiguration configuration = handler.getConfiguration();
		
		return configuration;
	}
}
