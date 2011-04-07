package org.testium.plugins;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.testium.Testium;
import org.testium.configuration.ConfigurationException;
import org.testium.configuration.SoapuiConfiguration;
import org.testium.configuration.SoapuiConfigurationXmlHandler;
import org.testium.executor.TestCaseSoapuiExecutor;
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
	
	@Override
	public void loadPlugIn(PluginCollection aPluginCollection,
			RunTimeData aRtData) throws ConfigurationException
	{
		SoapuiConfiguration config = readConfigFiles( aRtData );

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
		SoapuiConfiguration userConfig = new SoapuiConfiguration( null, null, null);
		if ( userConfigFile.exists() )
		{
			userConfig = readConfigFile( userConfigFile, anRtData );
		}

		File executor = userConfig.getExecutor();
		if (executor == null)
		{
			executor = globalConfig.getExecutor();
		}

		File project = userConfig.getProject();
		if (project == null)
		{
			project = globalConfig.getProject();
		}

		String soapInterface = userConfig.getSoapInterface();
		if (soapInterface == null)
		{
			soapInterface = globalConfig.getSoapInterface();
		}
		
		SoapuiConfiguration config = new SoapuiConfiguration( executor, project, soapInterface );

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
		
		SoapuiConfiguration myConfiguration = handler.getConfiguration();
		
		return myConfiguration;
	}
}
